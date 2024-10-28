package org.udpServer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDPServer {
    private int port;
    private final ExecutorService executorService;

    public UDPServer(int port) {
        this.port = port;
        this.executorService = Executors.newFixedThreadPool(10); // 필요에 따라 스레드 풀 크기 조절
    }

    public void start() {
        try (DatagramSocket serverSocket = new DatagramSocket(port)) {
            System.out.println("UDP 서버가 포트 " + port + "에서 시작되었습니다.");

            byte[] receiveBuffer = new byte[1024];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                serverSocket.receive(receivePacket);

                // 비동기로 클라이언트의 요청 처리
                executorService.submit(() -> handleRequest(serverSocket, receivePacket));

                // 버퍼 초기화
                receiveBuffer = new byte[1024];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(DatagramSocket serverSocket, DatagramPacket receivePacket) {
        try {
            String receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("클라이언트로부터 받은 데이터: " + receivedData);

            // 응답 생성
            String responseMessage = "OK";
            byte[] sendBuffer = responseMessage.getBytes();

            // 클라이언트의 주소와 포트 가져오기
            InetAddress clientAddress = receivePacket.getAddress();
            int clientPort = receivePacket.getPort();

            // 응답 패킷 전송
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, clientAddress, clientPort);
            serverSocket.send(sendPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            return;
        }

        int serverPort = Integer.parseInt(args[0]);
        UDPServer server = new UDPServer(serverPort);
        server.start();
    }
}
