package org.loadBalancer.util;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import org.loadBalancer.model.BackendServer;

public class HealthChecker {
    private static final int CHECK_INTERVAL_MS = 5000; // 5초 간격으로 헬스 체크

    public static void startHealthCheck(List<BackendServer> backendServers) {
        new Thread(() -> {
            while (true) {
                for (BackendServer server : backendServers) {
                    boolean isAvailable = checkServerHealth(server.getIp(), server.getPort());
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

    // 서버가 가용한지 확인하는 메서드
    private static boolean checkServerHealth(String ip, int port) {
        try (Socket socket = new Socket(ip, port)) {
            return true; // 서버에 정상적으로 연결되면 가용한 상태
        } catch (IOException e) {
            return false; // 서버에 연결할 수 없으면 가용하지 않은 상태
        }
    }
}
