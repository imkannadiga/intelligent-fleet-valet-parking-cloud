package com.example.controlserver.Services;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MapServerService {

    private static final String MAP_SERVER_URL = "http://mapserver:10002/api/costmap/";

    public Map<String, Object> getMap() {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> response = restTemplate.getForObject(MAP_SERVER_URL + "/download_map", Map.class);
        if (!(boolean) response.get("available")) {
            return null;
        }
        return response;
    }
}
