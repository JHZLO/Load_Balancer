package org.tcpServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    private int port;

    public TCPServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("TCP 서버가 포트 " + port + "에서 시작되었습니다.");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String receivedData = in.readLine();
                System.out.println("클라이언트로부터 받은 데이터: " + receivedData);

                // 응답 생성 및 전송
                String responseMessage = "TCP 응답: OK";
                out.println(responseMessage);

                clientSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            return;
        }

        int serverPort = Integer.parseInt(args[0]);
        TCPServer server = new TCPServer(serverPort);
        server.start();
    }
}
