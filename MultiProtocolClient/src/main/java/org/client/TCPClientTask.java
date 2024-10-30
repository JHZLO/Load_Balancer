package org.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPClientTask implements Runnable {
    private final String serverAddress;
    private final int serverPort;

    public TCPClientTask(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 30; i++) { // 10번 요청 전송
                try (Socket socket = new Socket(serverAddress, serverPort)) {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String message = "TCP 테스트 메시지 " + i;
                    out.println(message);
                    System.out.println("로드밸런서로 TCP 데이터 전송: " + message);

                    String response = in.readLine();
                    System.out.println("서버로부터 받은 TCP 응답: " + response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
