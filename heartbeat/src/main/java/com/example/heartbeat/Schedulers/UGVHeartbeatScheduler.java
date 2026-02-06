package com.example.heartbeat.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.heartbeat.Misc.UGVStatus;
import com.example.heartbeat.Models.UGV;
import com.example.heartbeat.Services.UGVService;

@Component
public class UGVHeartbeatScheduler {

    @Autowired
    private UGVService ugvService;

    private static final Logger logger = LoggerFactory.getLogger(UGVHeartbeatScheduler.class);

    @Scheduled(fixedDelay = 10000)
    public void broadcastUGVHeartBeat() {
        logger.debug("Started UGV offline check scheduler");

        try {
            List<UGV> ugvList = ugvService.getAllUGVs();
            logger.debug("Checking heartbeat for {} UGV(s)", ugvList.size());

            long currentTime = System.currentTimeMillis();
            int offlineCount = 0;
            
            Map<String, Object> request = new HashMap<>();
            request.put("request_type", "heartbeat");
            
            for(UGV ugv : ugvList) {
                try {
                    long timeSinceLastHeartbeat = currentTime - ugv.getLastHeartbeat();
                    if(timeSinceLastHeartbeat > 5000) { 
                        logger.info("UGV marked as offline - ID: {}, Time since last heartbeat: {}ms", 
                                ugv.getId(), timeSinceLastHeartbeat);
                        ugv.setStatus(UGVStatus.OFFLINE);
                        ugvService.updateUGV(ugv.getId(), ugv);
                        offlineCount++;
                    } else {
                        logger.trace("UGV is online - ID: {}, Time since last heartbeat: {}ms", 
                                ugv.getId(), timeSinceLastHeartbeat);
                    }
                } catch (Exception e) {
                    logger.error("Error updating UGV status - ID: {}", ugv.getId(), e);
                }
            }
            
            if (offlineCount > 0) {
                logger.info("UGV offline check completed - {} UGV(s) marked as offline", offlineCount);
            } else {
                logger.debug("UGV offline check completed - All UGVs are online");
            }
        } catch (Exception e) {
            logger.error("Error in UGV heartbeat scheduler", e);
        }
    }
}
