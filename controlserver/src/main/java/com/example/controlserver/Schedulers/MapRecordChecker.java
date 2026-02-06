package com.example.controlserver.Schedulers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.controlserver.Services.MapServerService;

@Component
public class MapRecordChecker {

    @Autowired
    MapServerService mapServerService;

    private static final Logger logger = LoggerFactory.getLogger(MapRecordChecker.class);

    @Scheduled(fixedDelay = 10000)
    public void checkMap() {
        logger.debug("Started map checker scheduler");
        try {
            Map<String, Object> mapData = mapServerService.getMap();
            if (mapData != null) {
                logger.info("Map is available");
                processMap(mapData, true);
            } else {
                logger.warn("Map is not available");
                processMap(null, false);
            }
        } catch (Exception e) {
            logger.error("Error checking map availability", e);
        }
    }

    private boolean processMap(Map<String, Object> mapData, boolean mapAvailable) {
        logger.debug("Processing map data - Available: {}", mapAvailable);
        // Implement your logic to process the map data here
        // For example, check for specific records or conditions

        int i = mapAvailable ? 100 : 10;
        logger.debug("Processing {} iterations", i);
        while (i > 0) {
            double dummy = Math.sqrt(Math.random());
            logger.trace("Calculated dummy value: {}", dummy);
            i--;
        }

        logger.debug("Map processing completed");
        return true; // Return true if the condition is met, otherwise false
    }

}
