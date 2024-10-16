package org.loadBalancer.config;

import java.io.InputStream;
import java.util.List;
import org.loadBalancer.model.server.ServerInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigurationManager {
    public static List<ServerInfo> loadConfiguration(String fileName) throws Exception {

        InputStream inputStream = ConfigurationManager.class.getClassLoader().getResourceAsStream(fileName);

        if (inputStream == null) {
            throw new IllegalArgumentException("파일을 찾을 수 없습니다: " + fileName);
        }

        ObjectMapper mapper = new ObjectMapper(); // JSON 파일 파싱하는 ObjectMapper
        return List.of(mapper.readValue(inputStream, ServerInfo[].class));
    }
}
