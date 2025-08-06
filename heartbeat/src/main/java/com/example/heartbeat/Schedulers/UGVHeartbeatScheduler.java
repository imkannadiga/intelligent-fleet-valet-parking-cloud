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
        logger.info("Started UGV offline job");

        List<UGV> ugvList = ugvService.getAllUGVs();

        Map<String, Object> request = new HashMap<>();
        request.put("request_type", "heartbeat");
        for(UGV ugv : ugvList) {
            try {
                if(System.currentTimeMillis() - ugv.getLastHeartbeat() > 5000) { 
                    ugv.setStatus(UGVStatus.OFFLINE);
                    ugvService.updateUGV(ugv.getId(), ugv);
                }
            } catch (Exception e) {
                logger.error("Error updating UGV :: "+ugv.getId());
            }
        }
    }
}
