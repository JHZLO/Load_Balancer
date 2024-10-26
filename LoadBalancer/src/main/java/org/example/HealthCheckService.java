package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class HealthCheckService implements Runnable {
    private List<ServerInfo> serverList;
    private int checkInterval = 5000; // 5초마다 헬스 체크

    public HealthCheckService(List<ServerInfo> serverList) {
        this.serverList = serverList;
    }

    @Override
    public void run() {
        while (true) {
            for (ServerInfo server : serverList) {
                boolean isHealthy;
                if (server.getProtocol().equalsIgnoreCase("UDP")) {
                    isHealthy = checkHealthUDP(server);
                } else if (server.getProtocol().equalsIgnoreCase("TCP") || server.getProtocol().equalsIgnoreCase("HTTP")) {
                    isHealthy = checkHealthTCP(server);
                } else {
                    continue;
                }

                server.setActive(isHealthy);
                System.out.println("서버 " + server.getIpAddress() + ":" + server.getPort() + " (" + server.getProtocol() + ") 상태: " + (isHealthy ? "활성" : "비활성"));
            }

            try {
                Thread.sleep(checkInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkHealthUDP(ServerInfo server) {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] sendData = "HealthCheck".getBytes();
            InetAddress serverAddress = InetAddress.getByName(server.getIpAddress());
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, server.getPort());
            socket.send(sendPacket);

            socket.setSoTimeout(2000);
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket);

            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            return response.equals("OK");
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkHealthTCP(ServerInfo server) {
        try (Socket socket = new Socket(server.getIpAddress(), server.getPort());
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("HealthCheck");
            socket.setSoTimeout(2000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
