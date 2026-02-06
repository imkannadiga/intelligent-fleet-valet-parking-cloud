package com.example.heartbeat.Controllers;

import com.example.heartbeat.Models.UGV;
import com.example.heartbeat.Services.UGVService;
import com.example.heartbeat.Misc.UGVStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ugv")
public class UGVHeartbeatController {

    private static final Logger logger = LoggerFactory.getLogger(UGVHeartbeatController.class);

    @Autowired
    private UGVService ugvService;

    @PutMapping("/{id}/regusterHeartbeat")
    public ResponseEntity<?> registerHeartbeat(@PathVariable String id,
            @RequestBody Map<String, String> heartbeatData) {
        logger.debug("Heartbeat registration received - UGV ID: {}", id);
        try {
            UGV ugv = ugvService.getUGVById(id);
            long currentTime = System.currentTimeMillis();
            ugv.setStatus(UGVStatus.ONLINE);
            ugv.setLastHeartbeat(currentTime); // Update last heartbeat time
            ugvService.updateUGV(id, ugv);
            logger.info("Heartbeat registered successfully - UGV ID: {}, Timestamp: {}", id, currentTime);
            // TODO: Add logic to handle heartbeat data if necessary
            return ResponseEntity.ok("Heartbeat updated successfully");
        } catch (Exception e) {
            logger.warn("Heartbeat registration failed - UGV ID: {}, Error: {}", id, e.getMessage());
            return ResponseEntity.status(404).body("UGV not found");
        }
    }

    @GetMapping("/heartbeat/{id}")
    public ResponseEntity<Map<String, Object>> getHeartbeat(@PathVariable String id) {
        logger.debug("Get heartbeat request received - UGV ID: {}", id);
        try {
            UGV ugv = ugvService.getUGVById(id);
            if(ugv.getStatus() == UGVStatus.ONLINE) {
                logger.debug("Heartbeat retrieved - UGV ID: {}, Last Heartbeat: {}", id, ugv.getLastHeartbeat());
                return ResponseEntity.ok(Map.of("lastHeartbeat", ugv.getLastHeartbeat()));
            } else {
                logger.warn("Heartbeat request failed: UGV is offline - UGV ID: {}", id);
                return ResponseEntity.status(404).body(Map.of("error", "UGV is offline"));
            }
        } catch (Exception e) {
            logger.error("Error retrieving heartbeat - UGV ID: {}", id, e);
            return ResponseEntity.status(404).body(Map.of("error", "UGV not found"));
        }
    }
}
