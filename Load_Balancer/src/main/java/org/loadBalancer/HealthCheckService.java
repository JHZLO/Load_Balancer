package org.loadBalancer;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class HealthCheckService {
    private Map<String, Boolean> serverStatus;
    private int checkInterval; // 체크 주기 (ms)

    public HealthCheckService(int checkInterval) {
        this.serverStatus = new HashMap<>();
        this.checkInterval = checkInterval;
    }

    // 서버 추가
    public void addServer(String server) {
        serverStatus.put(server, false); // 초기 상태를 비정상으로 설정
    }

    // 특정 서버의 헬스체크 수행
    private boolean isServerHealthy(String serverUrl) {
        try {
            URL url = new URL(serverUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000); // 2초 동안 연결 시도
            connection.setReadTimeout(2000); // 2초 동안 응답 대기

            int responseCode = connection.getResponseCode();
            return responseCode == 200;
        } catch (Exception e) {
            return false;
        }
    }

    // 모든 서버의 헬스체크를 주기적으로 수행하는 메서드
    public void startHealthCheck() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (String server : serverStatus.keySet()) {
                    boolean isHealthy = isServerHealthy(server);
                    serverStatus.put(server, isHealthy);
                    System.out.println("Server: " + server + " is " + (isHealthy ? "healthy" : "unhealthy"));
                }
            }
        }, 0, checkInterval); // 처음 실행 후 매 checkInterval 주기로 수행
    }

    // 특정 서버의 상태를 반환
    public boolean isServerActive(String server) {
        return serverStatus.getOrDefault(server, false);
    }

    // 모든 서버 상태를 반환
    public Map<String, Boolean> getAllServerStatus() {
        return serverStatus;
    }
}
