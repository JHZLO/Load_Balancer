package org.tcpServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java TcpServer <Port>");
            return;
        }

        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("TCP Server is running on port " + port);

            while (true) {
                // 클라이언트 연결 대기
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // 클라이언트로부터 데이터 읽기
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String clientMessage = reader.readLine();
                System.out.println("Received from client: " + clientMessage);

                // 클라이언트로 응답
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                writer.println("Echo: " + clientMessage);

                // 연결 종료
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
