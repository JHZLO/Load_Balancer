package org.example.balance;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;
import org.example.ServerInfo;
import org.example.balance.LoadBalancer;

public class UDPHandler {
    private final LoadBalancer loadBalancer;

    public UDPHandler(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public void startUDP(int listeningPort) {
        try (DatagramSocket loadBalancerSocket = new DatagramSocket(listeningPort)) {
            System.out.println("UDP 로드밸런서가 포트 " + listeningPort + "에서 시작되었습니다.");

            while (true) {
                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                loadBalancerSocket.receive(receivePacket);

                ServerInfo server = loadBalancer.getNextServer("UDP");
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                CompletableFuture.runAsync(() ->
                                forwardPacketToServer(server, receivePacket.getData(), clientAddress, clientPort),
                        loadBalancer.executorService);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void forwardPacketToServer(ServerInfo server, byte[] data, InetAddress clientAddress, int clientPort) {
        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket forwardPacket = new DatagramPacket(data, data.length,
                    InetAddress.getByName(server.getIpAddress()), server.getPort());
            socket.send(forwardPacket);

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
}
