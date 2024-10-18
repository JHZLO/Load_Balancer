package org.loadBalancer.model;

public class BackendServer {
    private String ip;
    private int port;
    private boolean available;
    private String protocol; // TCP, UDP 등 프로토콜 구분

    // 기본 생성자
    public BackendServer() {}

    // 생성자에서 프로토콜 포함
    public BackendServer(String ip, int port, String protocol) {
        this.ip = ip;
        this.port = port;
        this.protocol = protocol;
        this.available = true; // 기본적으로 서버는 가용한 상태
    }

    // Getter 및 Setter
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

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public String toString() {
        return ip + ":" + port + " (" + protocol + ") (available: " + available + ")";
    }
}
