package com.example.valetparking.Helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class HeartbeatHelper {

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatHelper.class);

    @Autowired
    RestTemplate restTemplate;

    private final String HEARTBEAT_SERVICE = "http://heartbeat-service:10010/api/ugv/heartbeat";

    public boolean isUGVOnline(String ugvId) {
        logger.debug("Checking if UGV is online: {}", ugvId);
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(HEARTBEAT_SERVICE + "/" + ugvId, Map.class);
            boolean isOnline = response.getStatusCode().value() == 200;
            logger.debug("UGV online status check - UGV: {}, Online: {}", ugvId, isOnline);
            return isOnline;
        } catch (RestClientException e) {
            logger.warn("Error checking UGV online status - UGV: {}", ugvId, e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error checking UGV online status - UGV: {}", ugvId, e);
            return false;
        }
    }

}
