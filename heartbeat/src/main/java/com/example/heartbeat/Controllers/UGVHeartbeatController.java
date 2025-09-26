package com.example.heartbeat.Controllers;

import com.example.heartbeat.Models.UGV;
import com.example.heartbeat.Services.UGVService;
import com.example.heartbeat.Misc.UGVStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ugv")
public class UGVHeartbeatController {

    @Autowired
    private UGVService ugvService;

    @PutMapping("/{id}/regusterHeartbeat")
    public ResponseEntity<?> registerHeartbeat(@PathVariable String id,
            @RequestBody Map<String, String> heartbeatData) {
        try {
            UGV ugv = ugvService.getUGVById(id);
            ugv.setStatus(UGVStatus.ONLINE);
            ugv.setLastHeartbeat(System.currentTimeMillis()); // Update last heartbeat time
            ugvService.updateUGV(id, ugv);
            // TODO: Add logic to handle heartbeat data if necessary
            return ResponseEntity.ok("Heartbeat updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(404).body("UGV not found");
        }
    }

    @GetMapping("/heartbeat/{id}")
    public ResponseEntity<Map<String, Object>> getHeartbeat(@PathVariable String id) {
        try {
            UGV ugv = ugvService.getUGVById(id);
            if(ugv.getStatus() == UGVStatus.ONLINE) {
                return ResponseEntity.ok(Map.of("lastHeartbeat", ugv.getLastHeartbeat()));
            } else {
                return ResponseEntity.status(404).body(Map.of("error", "UGV is offline"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", "UGV not found"));
        }
    }
}
