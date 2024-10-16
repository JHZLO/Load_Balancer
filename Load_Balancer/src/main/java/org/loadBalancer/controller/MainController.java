package org.loadBalancer.controller;

import java.util.ArrayList;
import java.util.List;
import org.loadBalancer.config.ConfigurationManager;
import org.loadBalancer.model.server.ServerInfo;

public class MainController {
    public void run(){
        try {
            // 서버 설정 파일 로드
            String configPath = "servers.json";
            List<ServerInfo> serverInfos = ConfigurationManager.loadConfiguration(configPath);

            LoadBalancerController controller = new LoadBalancerController(new ArrayList<>());

            // 서버 정보를 하나씩 등록
            for (ServerInfo serverInfo : serverInfos) {
                controller.startClientRequestHandling(serverInfo.getPort());
                controller.registerServer(serverInfo);
            }

            controller.startHealthChecks();

            controller.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
