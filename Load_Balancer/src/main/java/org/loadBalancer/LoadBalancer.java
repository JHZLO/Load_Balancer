package org.loadBalancer;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadBalancer {
    private List<String> serverList;
    private AtomicInteger currentIndex;

    public LoadBalancer() {
        this.serverList = new ArrayList<>();
        this.currentIndex = new AtomicInteger(-1);
    }

    // 서버를 추가하는 메서드
    public void addServer(String server) {
        serverList.add(server);
    }

    // 서버에서 하나를 선택하는 라운드 로빈 방식
    public String getNextServer() {
        if (serverList.isEmpty()) {
            throw new IllegalStateException("No servers available");
        }

        int index = (currentIndex.incrementAndGet()) % serverList.size();
        return serverList.get(index);
    }

    // 모든 서버 목록을 반환하는 메서드
    public List<String> getAllServers() {
        return serverList;
    }

    // 간단한 HTTP 서버를 추가하여 로드밸런서 엔드포인트 정의
    public void startHttpServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/balance", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("GET".equals(exchange.getRequestMethod())) {
                    try {
                        String targetServer = getNextServer();
                        String response = "Redirecting to server: " + targetServer;
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    } catch (IllegalStateException e) {
                        exchange.sendResponseHeaders(503, -1); // 503 Service Unavailable
                    }
                } else {
                    exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
                }
            }
        });
        server.setExecutor(null);
        server.start();
        System.out.println("Load Balancer started on port: " + port);
    }
}
