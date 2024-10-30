package org.example.balance;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.example.ServerInfo;
import org.example.balance.LoadBalancer;

public class HTTPHandler {
    private final LoadBalancer loadBalancer;

    public HTTPHandler(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public void startHttp(int port, ExecutorService executorService) throws IOException {
        System.out.println("HTTP 로드밸런서가 포트 " + port + "에서 시작되었습니다.");
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/balance", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if ("GET".equals(exchange.getRequestMethod())) {
                    String protocol = exchange.getRequestURI().getQuery();
                    if (protocol == null || protocol.isEmpty()) {
                        protocol = "HTTP";
                    }

                    ServerInfo targetServer = loadBalancer.getNextServer(protocol);
                    CompletableFuture.supplyAsync(() -> forwardHttpRequest(targetServer, exchange), executorService)
                            .thenAccept(response -> {
                                try {
                                    exchange.sendResponseHeaders(200, response.getBytes().length);
                                    OutputStream os = exchange.getResponseBody();
                                    os.write(response.getBytes());
                                    os.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            }
        });
        server.setExecutor(executorService);
        server.start();
    }

    private String forwardHttpRequest(ServerInfo server, HttpExchange exchange) {
        try {
            URL url = new URL(
                    "http://" + server.getIpAddress() + ":" + server.getPort() + exchange.getRequestURI().getPath());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(exchange.getRequestMethod());

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } else {
                return "Error: " + responseCode;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "서버 오류: HTTP 서버와의 통신 실패";
        }
    }
}
