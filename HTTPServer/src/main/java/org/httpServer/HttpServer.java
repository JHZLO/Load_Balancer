package org.httpServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java ApiServer <Port>");
            return;
        }

        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("HTTP API Server is running on port " + port);

            while (true) {
                // 클라이언트 연결 대기
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // HTTP 요청 읽기
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    System.out.println(line);  // HTTP 요청 헤더 출력
                }

                // HTTP 응답 전송
                String httpResponse =
                    "HTTP/1.1 200 OK\r\n" +
                        "Content-Length: 13\r\n" +
                        "\r\n" +
                        "Hello, World!";
                clientSocket.getOutputStream().write(httpResponse.getBytes());

                // 연결 종료
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
