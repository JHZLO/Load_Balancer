package org.example;

import java.util.ArrayList;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        List<ServerInfo> serverList = new ArrayList<>();
        serverList.add(new ServerInfo("UDP", "127.0.0.1", 9001));
        serverList.add(new ServerInfo("UDP", "127.0.0.1", 9002));
        serverList.add(new ServerInfo("TCP", "127.0.0.1", 9003));
        serverList.add(new ServerInfo("TCP", "127.0.0.1", 9004));
        serverList.add(new ServerInfo("HTTP", "127.0.0.1", 8081));
        serverList.add(new ServerInfo("HTTP", "127.0.0.1", 8082));

        HealthCheckService healthCheckService = new HealthCheckService(serverList);
        Thread healthCheckThread = new Thread(healthCheckService);
        healthCheckThread.start();

        LoadBalancer loadBalancer = new LoadBalancer(serverList);
        loadBalancer.start(8888, 9000);
    }
}
