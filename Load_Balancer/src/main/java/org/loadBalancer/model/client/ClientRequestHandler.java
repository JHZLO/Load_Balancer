package org.loadBalancer.model.client;

import java.net.Socket;
import org.loadBalancer.model.server.ServerHandler;

public class ClientRequestHandler implements Runnable {
    private final Socket clientSocket;
    private final ServerHandler server;

    public ClientRequestHandler(Socket clientSocket, ServerHandler server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            System.out.println("Forwarding request to " + server.ip + ":" + server.port);
            server.handleRequest();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
