package org.loadBalancer.handler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import org.loadBalancer.model.BackendServer;

public class TcpHandler {

    public static void startTcpProxy(int proxyPort, List<BackendServer> servers) {
        try (ServerSocket proxySocket = new ServerSocket(proxyPort)) {
            System.out.println("TCP Proxy running on port " + proxyPort);

            while (true) {
                Socket clientSocket = proxySocket.accept();
                System.out.println("New TCP client connected: " + clientSocket.getInetAddress());

                new Thread(() -> handleTcpRequest(clientSocket, servers)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleTcpRequest(Socket clientSocket, List<BackendServer> servers) {
        BackendServer backendServer = getAvailableServer(servers);
        if (backendServer == null) {
            System.out.println("No available backend servers!");
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        try (Socket serverSocket = new Socket(backendServer.getIp(), backendServer.getPort())) {
            forwardData(clientSocket, serverSocket);
            forwardData(serverSocket, clientSocket);
        } catch (IOException e) {
            System.out.println("Error handling TCP request: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 가용한 서버를 라운드 로빈 방식으로 선택
    private static BackendServer getAvailableServer(List<BackendServer> servers) {
        for (BackendServer server : servers) {
            if (server.isAvailable()) {
                return server;
            }
        }
        return null; // 가용한 서버가 없으면 null 반환
    }

    private static void forwardData(Socket fromSocket, Socket toSocket) throws IOException {
        try (InputStream in = fromSocket.getInputStream(); OutputStream out = toSocket.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                out.flush();
            }
        }
    }
}
