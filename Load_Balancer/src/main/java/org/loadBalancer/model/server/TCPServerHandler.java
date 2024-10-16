package org.loadBalancer.model.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.loadBalancer.model.server.ServerHandler;

public class TCPServerHandler extends ServerHandler {
    private final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public TCPServerHandler(String ip, int port) {
        super(ip, port);
    }

    @Override
    public void handleRequest() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("TCP ServerHandler running on " + ip + ":" + port);

            while (true) {
                // 클라이언트 연결 대기 (blocking)
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress());

                // 스레드 풀에서 각 클라이언트 요청을 처리
                threadPool.execute(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 각 클라이언트의 요청을 처리하는 메서드
    private void handleClient(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received: " + inputLine);
                out.write("Echo: " + inputLine + "\n");
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                System.out.println("Connection closed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
