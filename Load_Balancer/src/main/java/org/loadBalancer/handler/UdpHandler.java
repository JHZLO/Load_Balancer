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

                new Thread(() -> handleUdpRequest(udpSocket, packet, servers)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleUdpRequest(DatagramSocket udpSocket, DatagramPacket clientPacket, List<BackendServer> servers) {
        try {
            InetSocketAddress backendServer = new InetSocketAddress(servers.get(0).getIp(), servers.get(0).getPort());

            // 백엔드 서버로 전달
            DatagramPacket serverPacket = new DatagramPacket(
                clientPacket.getData(), clientPacket.getLength(), backendServer.getAddress(), backendServer.getPort());
            udpSocket.send(serverPacket);

            // 백엔드 서버로부터 응답 받기
            byte[] buffer = new byte[4096];
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
            udpSocket.receive(responsePacket);

            // 클라이언트로 응답 전송
            DatagramPacket clientResponsePacket = new DatagramPacket(
                responsePacket.getData(), responsePacket.getLength(), clientPacket.getAddress(), clientPacket.getPort());
            udpSocket.send(clientResponsePacket);

        } catch (Exception e) {
            System.out.println("UDP handling error: " + e.getMessage());
        }
    }
}
