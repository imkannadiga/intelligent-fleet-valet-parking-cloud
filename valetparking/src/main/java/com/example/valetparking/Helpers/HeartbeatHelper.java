package com.example.valetparking.Helpers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class HeartbeatHelper {

    @Autowired
    RestTemplate restTemplate;

    private final String HEARTBEAT_SERVICE = "http://heartbeat-service:10010/api/ugv/heartbeat";

    public boolean isUGVOnline(String ugvId) {
        ResponseEntity<Map> response = restTemplate.getForEntity(HEARTBEAT_SERVICE + "/" + ugvId, Map.class);
        return response.getStatusCode().value() == 200;
    }

}
