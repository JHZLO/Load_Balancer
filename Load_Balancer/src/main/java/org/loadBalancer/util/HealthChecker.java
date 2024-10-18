package org.loadBalancer.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import org.loadBalancer.model.BackendServer;

public class HealthChecker {
    private static final int CHECK_INTERVAL_MS = 5000; // 5초 간격으로 헬스 체크
    private static final int UDP_TIMEOUT_MS = 1000; // UDP 응답 타임아웃 설정 (1초)
    private static final String UDP_HEALTH_CHECK_MESSAGE = "HEALTH_CHECK"; // UDP 서버로 보낼 메시지

    public static void startHealthCheck(List<BackendServer> backendServers) {
        new Thread(() -> {
            while (true) {
                for (BackendServer server : backendServers) {
                    boolean isAvailable;
                    if (server.getProtocol().equalsIgnoreCase("UDP")) {
                        isAvailable = checkUdpServerHealth(server.getIp(), server.getPort());
                    } else {
                        isAvailable = checkTcpServerHealth(server.getIp(), server.getPort());
                    }
                    server.setAvailable(isAvailable);
                    System.out.println("Health check result for " + server + ": " + isAvailable);
                }

                try {
                    Thread.sleep(CHECK_INTERVAL_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // TCP 서버가 가용한지 확인하는 메서드
    private static boolean checkTcpServerHealth(String ip, int port) {
        try (Socket socket = new Socket(ip, port)) {
            return true; // TCP 서버에 정상적으로 연결되면 가용한 상태
        } catch (IOException e) {
            return false; // TCP 서버에 연결할 수 없으면 가용하지 않은 상태
        }
    }

    // UDP 서버가 가용한지 확인하는 메서드
    private static boolean checkUdpServerHealth(String ip, int port) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(UDP_TIMEOUT_MS); // 응답 타임아웃 설정

            // UDP 헬스 체크 메시지 전송
            InetAddress serverAddress = InetAddress.getByName(ip);
            byte[] sendData = UDP_HEALTH_CHECK_MESSAGE.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, port);
            socket.send(sendPacket);

            // 서버로부터 응답 받기
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(receivePacket); // 응답이 오면 서버는 가용 상태

            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            return response.equals(UDP_HEALTH_CHECK_MESSAGE); // 응답이 정확히 맞으면 가용한 상태로 판단
        } catch (IOException e) {
            return false; // UDP 서버에 응답이 없거나 타임아웃되면 가용하지 않은 상태
        }
    }
}
