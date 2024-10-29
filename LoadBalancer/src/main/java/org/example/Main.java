package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<ServerInfo> serverList = loadServerInfoFromJson("servers.json");

        if (serverList == null || serverList.isEmpty()) {
            System.out.println("서버 정보를 불러오지 못했습니다.");
            return;
        }

        HealthCheckService healthCheckService = new HealthCheckService(serverList);
        Thread healthCheckThread = new Thread(healthCheckService);
        healthCheckThread.start();

        LoadBalancer loadBalancer = new LoadBalancer(serverList);
        loadBalancer.start(8888, 8999, 9000);
    }

    private static List<ServerInfo> loadServerInfoFromJson(String fileName) {
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("파일을 찾을 수 없습니다: " + fileName);
            }
            // JSON 파일을 읽고 ServerInfo 객체로 매핑
            ServerListWrapper wrapper = objectMapper.readValue(inputStream, ServerListWrapper.class);
            return wrapper.getServers();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // 서버 리스트를 감싸는 래퍼 클래스
    public static class ServerListWrapper {
        private List<ServerInfo> servers;

        public List<ServerInfo> getServers() {
            return servers;
        }

        public void setServers(List<ServerInfo> servers) {
            this.servers = servers;
        }
    }
}
