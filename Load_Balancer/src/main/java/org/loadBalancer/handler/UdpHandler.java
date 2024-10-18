package org.loadBalancer.handler;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.List;
import org.loadBalancer.model.BackendServer;

public class UdpHandler {

    public static void startUdpProxy(int proxyPort, List<BackendServer> servers) {
        try (DatagramSocket udpSocket = new DatagramSocket(proxyPort)) {
            System.out.println("UDP Proxy running on port " + proxyPort);
            byte[] buffer = new byte[4096];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);

                System.out.println("Received UDP request from client: " + packet.getAddress() + ":" + packet.getPort());

                new Thread(() -> handleUdpRequest(udpSocket, packet, servers)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleUdpRequest(DatagramSocket udpSocket, DatagramPacket clientPacket, List<BackendServer> servers) {
        try {
            BackendServer backendServer = servers.get(1); // Round-robin이나 다른 방식으로 가용 서버 선택 가능
            InetSocketAddress backendAddress = new InetSocketAddress(backendServer.getIp(), backendServer.getPort());

            // 백엔드 서버로 전달
            DatagramPacket serverPacket = new DatagramPacket(
                    clientPacket.getData(), clientPacket.getLength(), backendAddress.getAddress(), backendAddress.getPort());
            udpSocket.send(serverPacket);

            System.out.println("Forwarded UDP request to backend server: " + backendServer.getIp() + ":" + backendServer.getPort());

            // 백엔드 서버로부터 응답 받기
            byte[] buffer = new byte[4096];
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
            udpSocket.receive(responsePacket);

            System.out.println("Received response from backend server: " + backendServer.getIp() + ":" + backendServer.getPort());

            // 클라이언트로 응답 전송
            DatagramPacket clientResponsePacket = new DatagramPacket(
                    responsePacket.getData(), responsePacket.getLength(), clientPacket.getAddress(), clientPacket.getPort());
            udpSocket.send(clientResponsePacket);

            System.out.println("Sent response to client: " + clientPacket.getAddress() + ":" + clientPacket.getPort());

        } catch (Exception e) {
            System.out.println("UDP handling error: " + e.getMessage());
        }
    }
}
