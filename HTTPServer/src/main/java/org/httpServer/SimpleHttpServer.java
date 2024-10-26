package org.httpServer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class SimpleHttpServer {
    private static int port;

    public static void main(String[] args) throws IOException {
        // 포트 번호를 인자로 받습니다. 기본값으로 8080 사용.
        port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;

        // HTTP 서버 생성
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // "/health" 엔드포인트를 처리하는 핸들러 등록
        server.createContext("/", new HealthHandler());

        // 서버 시작
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port: " + port);
    }

    // 헬스체크 엔드포인트에 대한 핸들러
    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String response = "Hello World!" + " port: " + port;
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            }
        }
    }
}
