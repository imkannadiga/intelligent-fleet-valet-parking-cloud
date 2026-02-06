package com.example.valetparking.Helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ControlHelper {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HeartbeatHelper heartbeatHelper;

    private final String CONTROL_SERVER_ADDRESS = "http://controlserver:10001/api";

    private final String CALLBACK_URL = "http://valetparking:9000/api/valetparking/action-complete";

    private static final Logger logger = LoggerFactory.getLogger(ControlHelper.class);

    public String sendRequestToControlServer(String ugvId, Map<String, Object> targetPose) {
        logger.info("Sending navigation request to control server - UGV: {}, targetPose: {}", ugvId, targetPose);

        // Generate payload to send to server
        Map<String, Object> payload = new HashMap<>();
        payload.put("ugvId", ugvId);
        payload.put("targetPose", targetPose);
        payload.put("callbackURL", CALLBACK_URL);
        logger.debug("Navigation request payload created - UGV: {}, callbackURL: {}", ugvId, CALLBACK_URL);

        try {
            // Send message to control server
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> resp = restTemplate.postForEntity(CONTROL_SERVER_ADDRESS + "/navigation-request", payload,
                    Map.class);
            logger.info("Response from control server: {}", resp.getBody());

            String navigationId = (String) resp.getBody().get("id");
            logger.info("Navigation request sent successfully - Navigation ID: {}, UGV: {}", navigationId, ugvId);
            return navigationId;
        } catch (Exception e) {
            logger.error("Error sending navigation request to control server - UGV: {}", ugvId, e);
            throw e;
        }
    }

    public boolean checkIfUGVIsValid(String ugvId) throws Exception {
        logger.debug("Checking if UGV is valid: {}", ugvId);

        try {
            String resp_str = restTemplate.getForObject(CONTROL_SERVER_ADDRESS + "/ugv", String.class);
            logger.debug("Received UGV list from control server");

            JsonObject resp = new JsonParser().parse(resp_str).getAsJsonArray().get(0).getAsJsonObject();

            // check to see if recieved response is empty or invalid
            if (resp.isJsonNull() || !resp.has("id")) {
                logger.warn("UGV validation failed: Invalid response from control server - UGV: {}", ugvId);
                return false;
            }

            // check to see if UGV is online
            boolean isOnline = heartbeatHelper.isUGVOnline(ugvId);
            if (!isOnline) {
                logger.warn("UGV validation failed: UGV is not online - UGV: {}", ugvId);
                return false;
            }

            logger.info("UGV validation successful - UGV: {}", ugvId);
            return true;
        } catch (Exception e) {
            logger.error("Error checking UGV validity - UGV: {}", ugvId, e);
            throw e;
        }
    }

    public boolean checkIfUGVIsParkable(String ugvId) {
        logger.debug("Checking if UGV is parkable: {}", ugvId);
        // TODO: Implement actual validation logic
        logger.debug("UGV parkable check completed - UGV: {}", ugvId);
        return true;
    }

    public boolean checkIfUGVIsRetrieveable(String ugvId) {
        logger.debug("Checking if UGV is retrieveable: {}", ugvId);
        // TODO: Implement actual validation logic
        logger.debug("UGV retrieveable check completed - UGV: {}", ugvId);
        return true;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<Map> getAllUGVs() {
        logger.debug("Fetching all UGVs from control server");
        try {
            ResponseEntity<List> resp = restTemplate.getForEntity(CONTROL_SERVER_ADDRESS + "/ugv", List.class);
            logger.info("Successfully retrieved {} UGV(s) from control server", 
                    resp.getBody() != null ? resp.getBody().size() : 0);
            return resp.getBody();
        } catch (Exception e) {
            logger.error("Error fetching all UGVs from control server", e);
            throw e;
        }
    }
}
