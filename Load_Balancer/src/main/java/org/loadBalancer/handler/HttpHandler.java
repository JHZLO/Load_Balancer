package org.loadBalancer.handler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import org.loadBalancer.model.BackendServer;

public class HttpHandler {

    public static void startHttpProxy(int proxyPort, List<BackendServer> servers) {
        try (ServerSocket serverSocket = new ServerSocket(proxyPort)) {
            System.out.println("HTTP Proxy running on port " + proxyPort);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New HTTP client connected: " + clientSocket.getInetAddress());

                new Thread(() -> handleHttpRequest(clientSocket, servers)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleHttpRequest(Socket clientSocket, List<BackendServer> servers) {
        try (Socket serverSocket = new Socket(servers.get(0).getIp(), servers.get(0).getPort())) {
            // 클라이언트와 백엔드 서버 간 데이터 전달
            forwardHttpData(clientSocket, serverSocket);
            forwardHttpData(serverSocket, clientSocket);
        } catch (IOException e) {
            System.out.println("HTTP handling error: " + e.getMessage());
        }
    }

    private static void forwardHttpData(Socket fromSocket, Socket toSocket) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fromSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(toSocket.getOutputStream(), true)) {

            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                writer.println(line);
            }
        }
    }
}
