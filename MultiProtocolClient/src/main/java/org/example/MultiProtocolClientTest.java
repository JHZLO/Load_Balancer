package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;

public class MultiProtocolClientTest {

    public static void main(String[] args) {
        int udpPort = 8888; // 로드밸런서의 UDP 포트
        int tcpPort = 8888; // 로드밸런서의 TCP 포트
        int httpPort = 9000; // 로드밸런서의 HTTP 포트
        String serverAddress = "localhost";

        // UDP 테스트 스레드
        Thread udpThread = new Thread(new UDPClientTask(serverAddress, udpPort));
        // TCP 테스트 스레드
        Thread tcpThread = new Thread(new TCPClientTask(serverAddress, tcpPort));
        // HTTP 테스트 스레드
        Thread httpThread = new Thread(new HTTPClientTask(serverAddress, httpPort));

        // 스레드 시작
        udpThread.start();
        tcpThread.start();
        httpThread.start();

        // 스레드 종료 대기
        try {
            udpThread.join();
            tcpThread.join();
            httpThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class UDPClientTask implements Runnable {
    private final String serverAddress;
    private final int serverPort;

    public UDPClientTask(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try (DatagramSocket clientSocket = new DatagramSocket()) {
            for (int i = 0; i < 10; i++) { // 10번 요청 전송
                String message = "UDP 테스트 메시지 " + i;
                byte[] sendBuffer = message.getBytes();

                InetAddress serverInetAddress = InetAddress.getByName(serverAddress);
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, serverInetAddress, serverPort);
                clientSocket.send(sendPacket);
                System.out.println("로드밸런서로 UDP 데이터 전송: " + message);

                // 로드밸런서의 응답 수신
                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                clientSocket.receive(receivePacket);

                String receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("서버로부터 받은 UDP 응답: " + receivedData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class TCPClientTask implements Runnable {
    private final String serverAddress;
    private final int serverPort;

    public TCPClientTask(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 10; i++) { // 10번 요청 전송
                try (Socket socket = new Socket(serverAddress, serverPort)) {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String message = "TCP 테스트 메시지 " + i;
                    out.println(message);
                    System.out.println("로드밸런서로 TCP 데이터 전송: " + message);

                    String response = in.readLine();
                    System.out.println("서버로부터 받은 TCP 응답: " + response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class HTTPClientTask implements Runnable {
    private final String serverAddress;
    private final int serverPort;

    public HTTPClientTask(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 10; i++) { // 10번 요청 전송
                // HTTP 요청 전송
                URL url = new URL("http://" + serverAddress + ":" + serverPort + "/balance");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                System.out.println("HTTP 요청 전송, 응답 코드: " + responseCode);

                if (responseCode == 200) { // 성공적으로 응답을 받은 경우
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    System.out.println("서버로부터 받은 HTTP 응답: " + response);
                } else {
                    System.out.println("서버로부터 받은 HTTP 응답: 실패");
                }
                connection.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
