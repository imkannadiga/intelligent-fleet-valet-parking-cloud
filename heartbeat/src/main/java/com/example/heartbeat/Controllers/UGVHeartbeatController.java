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
    public ResponseEntity<?> registerHeartbeat(@PathVariable String id, @RequestBody Map<String, String> heartbeatData) {
        try {
            UGV ugv = ugvService.getUGVById(id);
            ugv.setStatus(UGVStatus.ONLINE);
            ugv.setLastHeartbeat(System.currentTimeMillis()); // Update last heartbeat time
            ugvService.updateUGV(id, ugv);
            return ResponseEntity.ok("Heartbeat updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(404).body("UGV not found");
        }
    }
}
