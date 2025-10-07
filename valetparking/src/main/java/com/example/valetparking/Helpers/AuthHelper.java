package com.example.valetparking.Helpers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class AuthHelper {
    
    @Autowired
    RestTemplate restTemplate;
    
    private final String AUTH_SERVER_ADDRESS = "http://auth-service:10005/api";

    public Map<String, Object> validateToken(String token) {
        try {
            String url = AUTH_SERVER_ADDRESS + "/validate";
            ResponseEntity<Map<String, Object>> resp = restTemplate.postForObject(url, Map.of("token", token), ResponseEntity.class);
            if(resp.getStatusCode().is2xxSuccessful()) {
                return resp.getBody();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
