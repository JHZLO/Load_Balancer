package org.loadBalancer.config;

import java.io.InputStream;
import java.util.List;
import org.loadBalancer.model.BackendServer;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigManager {
    public static List<BackendServer> loadConfiguration(String fileName) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = ConfigManager.class.getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new IllegalArgumentException("File not found: " + fileName);
        }
        return List.of(mapper.readValue(inputStream, BackendServer[].class));
    }
}
