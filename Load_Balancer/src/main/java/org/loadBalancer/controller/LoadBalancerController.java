package org.loadBalancer.controller;

import java.util.List;
import org.loadBalancer.config.ConfigManager;
import org.loadBalancer.handler.TcpHandler;
import org.loadBalancer.handler.UdpHandler;
import org.loadBalancer.model.BackendServer;

public class LoadBalancerController {
    public void run(){
        try {
            List<BackendServer> servers = ConfigManager.loadConfiguration("servers.json");

            new Thread(() -> TcpHandler.startTcpProxy(8080, servers)).start();
            new Thread(() -> UdpHandler.startUdpProxy(8081, servers)).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
