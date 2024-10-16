package org.loadBalancer.util;

import org.loadBalancer.model.server.ServerHandler;

public class HealthCheckTask implements Runnable {
    private final ServerHandler server;

    public HealthCheckTask(ServerHandler server) {
        this.server = server;
    }

    @Override
    public void run() {
        while (true) {
            try {
                server.handleRequest(); // 서버에 요청 시도
                System.out.println("Server is healthy: " + server.ip + ":" + server.port);
            } catch (Exception e) {
                System.out.println("Server is down: " + server.ip + ":" + server.port);
            }

            try {
                Thread.sleep(5000); // 5초마다 체크
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
