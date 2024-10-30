package org.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClientTask implements Runnable {
    private final String serverAddress;
    private final int serverPort;

    public UDPClientTask(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try (DatagramSocket clientSocket = new DatagramSocket()) {
            for (int i = 0; i < 30; i++) { // 10번 요청 전송
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
