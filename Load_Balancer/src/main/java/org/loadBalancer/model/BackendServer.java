package org.loadBalancer.model;

public class BackendServer {
    private String ip;
    private int port;
    private boolean available;

    public BackendServer(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.available = true; // 기본적으로 서버는 가용한 상태
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return ip + ":" + port + " (available: " + available + ")";
    }
}
