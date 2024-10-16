package org.loadBalancer.model.server;

public class ServerHandlerFactory {
    public static ServerHandler createServer(ServerInfo info) {
        switch (info.getProtocol().toUpperCase()) {
            case "TCP":
                return new TCPServerHandler(info.getIp(), info.getPort());
            case "UDP":
                return new UDPServerHandler(info.getIp(), info.getPort());
            default:
                throw new IllegalArgumentException("Unsupported protocol: " + info.getProtocol());
        }
    }
}
