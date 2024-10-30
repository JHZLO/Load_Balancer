package org.example.balance;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.example.ServerInfo;

public class ServerSelector {
    public static ServerInfo getNextServer(List<ServerInfo> serverList, String protocol, AtomicInteger currentIndex) {
        // 프로토콜에 맞는 활성화된 서버 목록 필터링
        List<ServerInfo> filteredServers = serverList.stream()
                .filter(server -> server.getProtocol().equalsIgnoreCase(protocol) && server.isActive())
                .collect(Collectors.toList());

        if (filteredServers.isEmpty()) {
            throw new IllegalStateException("No servers available for protocol: " + protocol);
        }

        int index = (currentIndex.incrementAndGet()) % filteredServers.size();
        return filteredServers.get(index);
    }
}
