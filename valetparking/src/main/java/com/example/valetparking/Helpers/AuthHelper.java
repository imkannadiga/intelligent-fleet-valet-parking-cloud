package com.example.valetparking.Helpers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthHelper {

    private static final Logger logger = LoggerFactory.getLogger(AuthHelper.class);
    
    @Autowired
    RestTemplate restTemplate;
    
    private final String AUTH_SERVER_ADDRESS = "http://auth-service:10005/api";

    public Map<String, Object> validateToken(String token) {
        logger.debug("Validating token with auth service");
        try {
            String url = AUTH_SERVER_ADDRESS + "/validate";
            logger.trace("Sending token validation request to: {}", url);
            
            ResponseEntity<Map<String, Object>> resp = restTemplate.postForObject(url, Map.of("token", token), ResponseEntity.class);
            if(resp.getStatusCode().is2xxSuccessful()) {
                logger.debug("Token validation successful");
                return resp.getBody();
            } else {
                logger.warn("Token validation failed: HTTP status {}", resp.getStatusCode());
                return null;
            }
        } catch (RestClientException e) {
            logger.error("Error validating token with auth service", e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error during token validation", e);
            return null;
        }
    }
}
