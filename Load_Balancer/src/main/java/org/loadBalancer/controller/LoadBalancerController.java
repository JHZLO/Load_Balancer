package org.loadBalancer.controller;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import org.loadBalancer.model.client.ClientRequestHandler;
import org.loadBalancer.util.HealthCheckTask;
import org.loadBalancer.model.server.ServerHandler;
import org.loadBalancer.model.server.ServerHandlerFactory;
import org.loadBalancer.model.server.ServerInfo;
import org.loadBalancer.util.ThreadPoolManager;

public class LoadBalancerController {
    private final ThreadPoolManager threadPool;
    private final List<ServerHandler> serverHandlers;
    private int currentIndex = 0;

    public LoadBalancerController(List<ServerHandler> serverHandlers) {
        this.serverHandlers = serverHandlers;
        this.threadPool = new ThreadPoolManager(10); // 스레드 풀 크기 설정
    }

    public void registerServer(ServerInfo serverInfo){
        ServerHandler serverHandler = ServerHandlerFactory.createServer(serverInfo);
        serverHandlers.add(serverHandler);
        System.out.println("Registered serverHandler: " + serverInfo.getIp() + ":" + serverInfo.getPort() + " >> PROTOCOL: " + serverInfo.getProtocol());
    }

    public void startHealthChecks() {
        for (ServerHandler server : serverHandlers) {
            threadPool.submitTask(new HealthCheckTask(server));
        }
    }

    public void startClientRequestHandling(int port) throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ServerHandler server = selectServer();
                threadPool.submitTask(new ClientRequestHandler(clientSocket, server));
            }
        }
    }

    private ServerHandler selectServer() {
        ServerHandler server = serverHandlers.get(currentIndex);
        currentIndex = (currentIndex + 1) % serverHandlers.size();
        return server;
    }

    public void shutdown() {
        threadPool.shutdown();
    }
}
