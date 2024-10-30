package org.example.balance;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.example.ServerInfo;
import org.example.balance.LoadBalancer;

public class TCPHandler {
    private final LoadBalancer loadBalancer;

    public TCPHandler(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public void startTCP(int listeningPort) {
        try (ServerSocket loadBalancerSocket = new ServerSocket(listeningPort)) {
            System.out.println("TCP 로드밸런서가 포트 " + listeningPort + "에서 시작되었습니다.");

            while (true) {
                Socket clientSocket = loadBalancerSocket.accept();
                CompletableFuture.runAsync(() -> handleTCPConnection(clientSocket), loadBalancer.executorService);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleTCPConnection(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String receivedData = in.readLine();
            ServerInfo server = loadBalancer.getNextServer("TCP");

            CompletableFuture.supplyAsync(() -> forwardDataToTCPServer(server, receivedData), loadBalancer.executorService)
                    .thenAccept(response -> {
                        out.println(response != null ? response : "서버 오류: 응답 없음");
                        out.flush();
                    }).join();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String forwardDataToTCPServer(ServerInfo server, String data) {
        try (Socket serverSocket = new Socket(server.getIpAddress(), server.getPort());
             PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()))) {

            out.println(data);
            return in.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            return "서버 오류: TCP 서버와의 통신 실패";
        }
    }
}
