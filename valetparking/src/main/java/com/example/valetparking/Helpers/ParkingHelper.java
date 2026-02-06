package com.example.valetparking.Helpers;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class ParkingHelper {

    private static final Logger logger = LoggerFactory.getLogger(ParkingHelper.class);

    @Autowired
    private RestTemplate restTemplate;

    private final String PARKING_SERVER_ADDRESS = "http://parking-spot-manager:9001/api/parking-spot";

    public Map<String, Object> getAndBlockAvailableParkingSpot() {
        logger.debug("Requesting available parking spot from parking spot manager");
        
        try {
            String resp_str = restTemplate.getForObject(PARKING_SERVER_ADDRESS+"/get-free-space", String.class);
            logger.debug("Received response from parking spot manager");

            JsonObject spot_json = new JsonParser().parse(resp_str).getAsJsonObject();

            if(spot_json.isJsonNull()) {
                logger.warn("No available parking spot found");
                return null;
            }

            String spotId = spot_json.get("id").getAsString();
            logger.info("Found available parking spot: {}", spotId);

            // block parking spot
            logger.debug("Blocking parking spot: {}", spotId);
            restTemplate.postForEntity(PARKING_SERVER_ADDRESS+"/"+spotId+"/block", null, String.class);
            logger.info("Parking spot blocked successfully: {}", spotId);
            
            Map<String, Object> spot = new HashMap<>();
            spot.put("id", spot_json.get("id").getAsString());
            spot.put("x",spot_json.get("x").getAsFloat());
            spot.put("y",spot_json.get("y").getAsFloat());
            spot.put("theta",spot_json.get("theta").getAsFloat());

            logger.debug("Parking spot details - ID: {}, x: {}, y: {}, theta: {}", 
                    spot.get("id"), spot.get("x"), spot.get("y"), spot.get("theta"));
            return spot;

        } catch (RestClientException e) {
            logger.error("Error communicating with parking spot manager", e);
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error while getting parking spot", e);
            return null;
        }
    }

    public void releaseParkingSpot(String parkingSpotId) {
        logger.info("Releasing parking spot: {}", parkingSpotId);
        try {
            restTemplate.postForEntity(PARKING_SERVER_ADDRESS+"/"+parkingSpotId+"/release", null, String.class);
            logger.info("Parking spot released successfully: {}", parkingSpotId);
        } catch (RestClientException e) {
            logger.error("Error releasing parking spot: {}", parkingSpotId, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while releasing parking spot: {}", parkingSpotId, e);
            throw e;
        }
    }
    
}
