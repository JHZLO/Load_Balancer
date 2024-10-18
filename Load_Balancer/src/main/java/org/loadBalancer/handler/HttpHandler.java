package org.loadBalancer.handler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import org.loadBalancer.model.BackendServer;

public class HttpHandler {

    public static void startHttpProxy(int proxyPort, List<BackendServer> servers) {
        try (ServerSocket serverSocket = new ServerSocket(proxyPort)) {
            System.out.println("HTTP Proxy running on port " + proxyPort);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New HTTP client connected: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

                new Thread(() -> handleHttpRequest(clientSocket, servers)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleHttpRequest(Socket clientSocket, List<BackendServer> servers) {
        BackendServer backendServer = servers.get(2); // Round-robin이나 다른 방식으로 가용 서버 선택 가능
        System.out.println("Forwarding HTTP request to backend server: " + backendServer.getIp() + ":" + backendServer.getPort());

        try (Socket serverSocket = new Socket(backendServer.getIp(), backendServer.getPort())) {
            // 클라이언트 요청을 백엔드 서버로 전달
            forwardHttpData(clientSocket.getInputStream(), serverSocket.getOutputStream());
            System.out.println("Forwarded client request to backend server.");

            // 백엔드 서버 응답을 클라이언트로 전달
            forwardHttpData(serverSocket.getInputStream(), clientSocket.getOutputStream());
            System.out.println("Forwarded backend server response to client.");
        } catch (IOException e) {
            System.out.println("HTTP handling error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("Closed client connection: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 데이터를 전달하는 메서드 (InputStream에서 OutputStream으로 데이터를 복사)
    private static void forwardHttpData(InputStream from, OutputStream to) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(from));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(to));

        String line;
        boolean headersCompleted = false;

        // HTTP 헤더 처리
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            writer.write(line);
            writer.write("\r\n");  // HTTP 헤더 끝에는 CRLF가 필요
            headersCompleted = true;
        }

        if (headersCompleted) {
            // HTTP 헤더 끝에는 빈 줄이 필요
            writer.write("\r\n");
        }

        writer.flush();  // 응답이 완료되면 반드시 flush() 호출
    }
}
