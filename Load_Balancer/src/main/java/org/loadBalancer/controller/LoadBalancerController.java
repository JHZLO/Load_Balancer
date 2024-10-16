package org.loadBalancer.controller;

import java.util.List;
import org.loadBalancer.config.ConfigManager;
import org.loadBalancer.handler.HttpHandler;
import org.loadBalancer.handler.TcpHandler;
import org.loadBalancer.handler.UdpHandler;
import org.loadBalancer.model.BackendServer;
import org.loadBalancer.util.HealthChecker;

public class LoadBalancerController {

    public void run() {
        try {
            List<BackendServer> servers = ConfigManager.loadConfiguration("servers.json");

            // 백엔드 서버 헬스 체크 시작
            HealthChecker.startHealthCheck(servers);

            new Thread(() -> TcpHandler.startTcpProxy(8080, servers)).start();
            new Thread(() -> UdpHandler.startUdpProxy(8081, servers)).start();
            new Thread(() -> HttpHandler.startHttpProxy(8082, servers)).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
