package org.loadBalancer.model.server;

public abstract class ServerHandler {
    public final String ip;
    public final int port;

    public ServerHandler(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public abstract void handleRequest(); // protocol의 종류마다 다른 handler 적용
}
