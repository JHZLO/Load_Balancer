package org.loadBalancer.model.server;

public class ServerInfo { // 서버의 주요 정보를 담고 있는 객체
    private String ip;
    private Integer port;
    private String protocol;

    public ServerInfo(){}

    public ServerInfo(String ip, Integer port, String protocol){
        this.ip = ip;
        this.port = port;
        this.protocol = protocol;
    }

    public String getIp(){
        return this.ip;
    }

    public Integer getPort(){
        return this.port;
    }

    public String getProtocol(){
        return this.protocol;
    }
}
