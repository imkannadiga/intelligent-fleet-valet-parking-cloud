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

    private static final Logger logger = LoggerFactory.getLogger(NavigationCallbackScheduler.class);

    @Scheduled(fixedDelay = 10000)
    public void checkMap() {
        logger.info("Started map checker scheduler");
        Map<String, Object> mapData = mapServerService.getMap();
        if (mapData != null) {
            logger.info("Map is available");
            processMap(mapData, true);
        } else {
            logger.warn("Map is not available.");
            processMap(null, false);
        }
    }

    private boolean processMap(Map<String, Object> mapData, boolean mapAvailable) {
        // Implement your logic to process the map data here
        // For example, check for specific records or conditions

        int i = mapAvailable ? 100 : 10;
        while (i > 0) {
            double dummy = Math.sqrt(Math.random());
            logger.debug("calculated dummy value: " + dummy);
            i--;
        }

        return true; // Return true if the condition is met, otherwise false
    }

}
