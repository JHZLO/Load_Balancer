package org.example;

public class ServerInfo {
    private String protocol;
    private String ipAddress;
    private int port;
    private boolean isActive;

    public ServerInfo() {
    }

    public ServerInfo(String protocol, String ipAddress, int port) {
        this.protocol = protocol;
        this.ipAddress = ipAddress;
        this.port = port;
        this.isActive = true;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
