package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadBalancer {
    private List<ServerInfo> serverList;
    private AtomicInteger currentIndex = new AtomicInteger(0);
    private final ExecutorService executorService;

    public LoadBalancer(List<ServerInfo> serverList) {
        this.serverList = serverList;
        this.executorService = Executors.newFixedThreadPool(10); // 필요한 만큼 스레드 풀 크기 설정
    }

    public void start(int TCPListeningPort, int UDPListeningPort,int APIListeningPort) {
        // UDP, TCP, HTTP 처리를 각각의 스레드로 실행
        executorService.submit(() -> startUDP(UDPListeningPort));
        executorService.submit(() -> startTCP(TCPListeningPort));
        executorService.submit(() -> {
            try {
                startHttp(APIListeningPort);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void startUDP(int listeningPort) {
        try (DatagramSocket loadBalancerSocket = new DatagramSocket(listeningPort)) {
            System.out.println("UDP 로드밸런서가 포트 " + listeningPort + "에서 시작되었습니다.");

            while (true) {
                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                loadBalancerSocket.receive(receivePacket);

                ServerInfo server = getNextServer("UDP");
                if (server == null) {
                    System.out.println("활성화된 UDP 서버가 없습니다.");
                    continue;
                }

                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                // 비동기로 패킷 포워딩 처리
                CompletableFuture.runAsync(
                        () -> forwardPacketToServer(server, receivePacket.getData(), clientAddress, clientPort),
                        executorService);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startTCP(int listeningPort) {
        try (ServerSocket loadBalancerSocket = new ServerSocket(listeningPort)) {
            System.out.println("TCP 로드밸런서가 포트 " + listeningPort + "에서 시작되었습니다.");

            while (true) {
                Socket clientSocket = loadBalancerSocket.accept();
                // 비동기로 TCP 연결 처리
                CompletableFuture.runAsync(() -> handleTCPConnection(clientSocket), executorService);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleTCPConnection(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // 클라이언트로부터 데이터 수신
            String receivedData = in.readLine();

            // 다음 서버를 선택
            ServerInfo server = getNextServer("TCP");
            if (server == null) {
                System.out.println("활성화된 TCP 서버가 없습니다.");
                out.println("서버 오류: 활성화된 서버가 없습니다.");
                return;
            }

            // 서버로 데이터 전송 및 응답 수신 (비동기 수행 후 결과를 클라이언트에 전송)
            CompletableFuture.supplyAsync(() -> forwardDataToTCPServer(server, receivedData), executorService)
                    .thenAccept(response -> {
                        if (response != null) {
                            // 서버로부터 받은 응답을 클라이언트에게 전송
                            out.println(response);
                        } else {
                            out.println("서버 오류: 응답 없음");
                        }
                        out.flush();
                    }).join();  // 결과가 완료될 때까지 기다림

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void startHttp(int port) throws IOException {
        System.out.println("HTTP 로드밸런서가 포트 " + port + "에서 시작되었습니다.");
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/balance", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("GET".equals(exchange.getRequestMethod())) {
                    String protocol = exchange.getRequestURI().getQuery(); // 프로토콜을 쿼리로 전달받음
                    if (protocol == null || protocol.isEmpty()) {
                        protocol = "HTTP"; // 기본값으로 HTTP를 사용
                    }

                    try {
                        ServerInfo targetServer = getNextServer(protocol);
                        // 비동기로 HTTP 요청 처리
                        CompletableFuture.supplyAsync(() -> forwardHttpRequest(targetServer, exchange), executorService)
                                .thenAccept(response -> {
                                    try {
                                        exchange.sendResponseHeaders(200, response.getBytes().length);
                                        OutputStream os = exchange.getResponseBody();
                                        os.write(response.getBytes());
                                        os.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });
                    } catch (IllegalStateException e) {
                        exchange.sendResponseHeaders(503, -1); // 503 Service Unavailable
                    }
                } else {
                    exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
                }
            }
        });
        server.setExecutor(executorService);
        server.start();
    }


    private void forwardPacketToServer(ServerInfo server, byte[] data, InetAddress clientAddress, int clientPort) {
        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket forwardPacket = new DatagramPacket(data, data.length,
                    InetAddress.getByName(server.getIpAddress()), server.getPort());
            socket.send(forwardPacket);

            // 서버에서의 응답을 클라이언트로 전달
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);

            DatagramPacket responsePacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
                    clientAddress, clientPort);
            socket.send(responsePacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String forwardDataToTCPServer(ServerInfo server, String data) {
        try (Socket serverSocket = new Socket(server.getIpAddress(), server.getPort());
             PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()))) {

            // 서버로 데이터 전송
            out.println(data);
            out.flush();

            // 서버로부터 응답 수신
            String response = in.readLine();  // 줄바꿈 문자 포함 응답
            if (response == null) {
                return "서버 오류: 응답 없음";
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return "서버 오류: TCP 서버와의 통신 실패";
        }
    }

    private String forwardHttpRequest(ServerInfo server, HttpExchange exchange) {
        try {
            URL url = new URL(
                    "http://" + server.getIpAddress() + ":" + server.getPort() + exchange.getRequestURI().getPath());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(exchange.getRequestMethod());

            // 응답 읽기
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } else {
                return "Error: " + responseCode;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "서버 오류: HTTP 서버와의 통신 실패";
        }
    }

    public ServerInfo getNextServer(String protocol) {
        // 프로토콜에 맞는 활성화된 서버 목록 필터링
        List<ServerInfo> filteredServers = serverList.stream()
                .filter(server -> server.getProtocol().equalsIgnoreCase(protocol) && server.isActive())
                .toList();

        if (filteredServers.isEmpty()) {
            throw new IllegalStateException("No servers available for protocol: " + protocol);
        }

        int index = (currentIndex.incrementAndGet()) % filteredServers.size();
        return filteredServers.get(index);
    }

    // 모든 서버 목록을 반환하는 메서드
    public List<ServerInfo> getAllServers() {
        return serverList;
    }
}
