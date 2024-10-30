package org.example.balance;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.example.ServerInfo;

public class LoadBalancer {
    private List<ServerInfo> serverList;
    private AtomicInteger currentIndex = new AtomicInteger(0);
    final ExecutorService executorService;
    private final TCPHandler tcpHandler;
    private final UDPHandler udpHandler;
    private final HTTPHandler httpHandler;

    public LoadBalancer(List<ServerInfo> serverList) {
        this.serverList = serverList;
        this.executorService = Executors.newFixedThreadPool(10);
        this.tcpHandler = new TCPHandler(this);
        this.udpHandler = new UDPHandler(this);
        this.httpHandler = new HTTPHandler(this);
    }

    public void start(int TCPListeningPort, int UDPListeningPort, int APIListeningPort) {
        executorService.submit(() -> udpHandler.startUDP(UDPListeningPort));
        executorService.submit(() -> tcpHandler.startTCP(TCPListeningPort));
        executorService.submit(() -> {
            try {
                httpHandler.startHttp(APIListeningPort, executorService);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public ServerInfo getNextServer(String protocol) {
        return ServerSelector.getNextServer(serverList, protocol, currentIndex);
    }

    public List<ServerInfo> getAllServers() {
        return serverList;
    }
}
