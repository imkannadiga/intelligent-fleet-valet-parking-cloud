package com.example.controlserver.Services;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class MapServerService {

    private static final Logger logger = LoggerFactory.getLogger(MapServerService.class);
    private static final String MAP_SERVER_URL = "http://mapserver:10002/api/costmap/";

    public Map<String, Object> getMap() {
        logger.debug("Fetching map from map server");
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> response = restTemplate.getForObject(MAP_SERVER_URL + "/download_map", Map.class);
            
            if (response == null) {
                logger.warn("Map server returned null response");
                return null;
            }
            
            if (!(boolean) response.get("available")) {
                logger.warn("Map is not available according to map server");
                return null;
            }
            
            logger.info("Map fetched successfully from map server");
            return response;
        } catch (RestClientException e) {
            logger.error("Error fetching map from map server", e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error while fetching map", e);
            return null;
        }
    }
}
