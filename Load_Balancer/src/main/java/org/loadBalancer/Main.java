package org.loadBalancer;


public class Main {
    public static void main(String[] args) throws Exception {
        LoadBalancer loadBalancer = new LoadBalancer();
        org.example.loadbalancer.HealthCheckService healthCheckService = new org.example.loadbalancer.HealthCheckService(5000); // 5초마다 헬스 체크

        // 서버 추가
        loadBalancer.addServer("http://localhost:8081/health");
        loadBalancer.addServer("http://localhost:8082/health");
        loadBalancer.addServer("http://localhost:8083/health");

        // 헬스 체크 서비스에 서버 등록
        for (String server : loadBalancer.getAllServers()) {
            healthCheckService.addServer(server);
        }

        // 헬스 체크 시작
        healthCheckService.startHealthCheck();

        // 로드밸런서 HTTP 서버 시작
        loadBalancer.startHttpServer(9000); // 로드밸런서 서버 포트 9000번으로 시작
    }
}
