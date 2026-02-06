package com.example.controlserver.Controllers;

import com.example.controlserver.Models.UGV;
import com.example.controlserver.Services.UGVService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/ugv")
public class UGVController {

    private static final Logger logger = LoggerFactory.getLogger(UGVController.class);

    @Autowired
    private UGVService ugvService;

    @PostMapping
    public ResponseEntity<UGV> createUGV(@RequestBody UGV ugv) {
        logger.info("Create UGV request received - Name: {}, Type: {}", ugv.getName(), ugv.getType());
        try {
            UGV createdUGV = ugvService.saveUGV(ugv);
            logger.info("UGV created successfully - ID: {}, Name: {}", createdUGV.getId(), createdUGV.getName());
            return ResponseEntity.ok(createdUGV);
        } catch (Exception e) {
            logger.error("Error creating UGV - Name: {}", ugv.getName(), e);
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<UGV>> getAllUGVs() {
        logger.debug("Get all UGVs request received");
        try {
            List<UGV> ugvs = ugvService.getAllUGVs();
            logger.info("Retrieved {} UGV(s)", ugvs.size());
            return ResponseEntity.ok(ugvs);
        } catch (Exception e) {
            logger.error("Error retrieving all UGVs", e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UGV> getUGVById(@PathVariable String id) {
        logger.debug("Get UGV by ID request received - ID: {}", id);
        try {
            UGV ugv = ugvService.getUGVById(id);
            logger.info("UGV retrieved successfully - ID: {}, Name: {}", id, ugv.getName());
            return ResponseEntity.ok(ugv);
        } catch (Exception e) {
            logger.error("Error retrieving UGV - ID: {}", id, e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UGV> updateUGV(@PathVariable String id, @RequestBody UGV ugvDetails) {
        logger.info("Update UGV request received - ID: {}", id);
        try {
            UGV updatedUGV = ugvService.updateUGV(id, ugvDetails);
            logger.info("UGV updated successfully - ID: {}, Name: {}", id, updatedUGV.getName());
            return ResponseEntity.ok(updatedUGV);
        } catch (Exception e) {
            logger.error("Error updating UGV - ID: {}", id, e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUGV(@PathVariable String id) {
        logger.info("Delete UGV request received - ID: {}", id);
        try {
            ugvService.deleteUGV(id);
            logger.info("UGV deleted successfully - ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting UGV - ID: {}", id, e);
            throw e;
        }
    }

    @PutMapping("/{id}/registerSession")
    public ResponseEntity<?> setSessionId(@PathVariable String id, @RequestBody Map<String, String> sessionId) {
        logger.info("Register session request received - UGV ID: {}", id);
        try {
            ugvService.getUGVById(id);
            UGV updated = ugvService.setUGVSessionID(id, sessionId.get("sessionId"));
            logger.info("Session registered successfully - UGV ID: {}, Session ID: {}", id, sessionId.get("sessionId"));
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.warn("Failed to register session - UGV ID: {}, Error: {}", id, e.getMessage());
            return ResponseEntity.status(404).body("Invalid UGV ID");
        }
    }
    

    /*
    
    @PostMapping("/{id}/navigateToGoal")
    public ResponseEntity<String> postGoalPose(@PathVariable String id, @RequestBody Map<String, Object> body) {

        UGV ugv = ugvService.getUGVById(id);

        String sockerSessionId = ugv.getSessionId();

        

        // System.out.println("coordinates "+body.get("point").toString());

        ObjectMapper objectMapper = new ObjectMapper();


        try {
            String mappedCoordinates = objectMapper.writeValueAsString(body.get("point"));

            // System.out.println("MappedCoordinates :::: "+mappedCoordinates);

            socketConnectionHandler.sendMessageToClient(sockerSessionId, new TextMessage(mappedCoordinates));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error ::: "+e.getMessage());
        }
        
        return ResponseEntity.status(200).body("Sent message successfully");
    }
    
    */
}
