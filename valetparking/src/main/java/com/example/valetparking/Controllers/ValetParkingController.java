package com.example.valetparking.Controllers;

import com.example.valetparking.Helpers.AuthHelper;
import com.example.valetparking.Helpers.ControlHelper;
import com.example.valetparking.Helpers.Phase;
import com.example.valetparking.Helpers.Task;
import com.example.valetparking.Models.Occupancy;
import com.example.valetparking.Models.Requests;
import com.example.valetparking.Repositories.OccupancyRepository;
import com.example.valetparking.Repositories.RequestRepository;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/valetparking")
public class ValetParkingController {

	private static final Logger logger = LoggerFactory.getLogger(ValetParkingController.class);

    @Autowired
    private ControlHelper controlHelper;

    @Autowired
    private AuthHelper authHelper;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private OccupancyRepository occupancyRepository;

    @PostMapping("/park")
    public ResponseEntity<Object> patk(@RequestBody Map<String, Object> payload) {
        logger.info("Park request received");
        logger.debug("Park request payload received");
        
        String token = (String) payload.get("token");
        if(token==null || token.isEmpty()) {
            logger.warn("Park request failed: No token provided");
            return ResponseEntity.status(401).body("Unauthorized: No token provided");
        }

        logger.debug("Validating token for park request");
        Map<String, Object> ugvDetails = authHelper.validateToken(token);
        if(ugvDetails==null) {
            logger.warn("Park request failed: Invalid token");
            return ResponseEntity.status(401).body("Unauthorized: Invalid token");
        }
        
        String ugvId = (String) ugvDetails.get("id");
        logger.info("Processing park request for UGV: {}", ugvId);

        try {
            logger.debug("Checking if UGV is valid: {}", ugvId);
            if (!controlHelper.checkIfUGVIsValid(ugvId)) {
                logger.warn("Park request failed: UGV ID invalid or UGV not online - {}", ugvId);
                return ResponseEntity.badRequest().body("UGV ID invalid or UGV not online");
            }

            logger.debug("Checking if UGV is parkable: {}", ugvId);
            if (!controlHelper.checkIfUGVIsParkable(ugvId)) {
                logger.warn("Park request failed: UGV is not parkable at the moment - {}", ugvId);
                return ResponseEntity.badRequest().body("UGV is not retrieveable at the moment");
            }

        } catch (Exception e) {
            logger.error("Error validating UGV for park request - UGV ID: {}", ugvId, e);
            return ResponseEntity.internalServerError().body("Internal server error......please contact developer");
        }

        logger.debug("Creating park request entity for UGV: {}", ugvId);
        // Create a requests entity
        Requests request = new Requests();
        request.setUgvId(ugvId);
        request.setCurrentPhase(Phase.WAITING);
        request.setCurrentTask(Task.PARK);
        request.setSchedulerFlag(true);
        request.setFinished(false);

        request = requestRepository.save(request);
        logger.info("Park request created successfully - Request ID: {}, UGV ID: {}", request.getId(), ugvId);

        Map<String, Object> response = Map.of(
            "requestId", request.getId(),
            "status", "queued"
        );

        return ResponseEntity.ok().body(response);

    }

    @PostMapping("/retrieve")
    public ResponseEntity<Object> retrieve(@RequestBody Map<String, Object> payload) {
        logger.info("Retrieve request received");
        logger.debug("Retrieve request payload received");
        
        String token = (String) payload.get("token");
        if(token==null || token.isEmpty()) {
            logger.warn("Retrieve request failed: No token provided");
            return ResponseEntity.status(401).body("Unauthorized: No token provided");
        }

        logger.debug("Validating token for retrieve request");
        Map<String, Object> ugvDetails = authHelper.validateToken(token);
        if(ugvDetails==null) {
            logger.warn("Retrieve request failed: Invalid token");
            return ResponseEntity.status(401).body("Unauthorized: Invalid token");
        }
        
        String ugvId = (String) ugvDetails.get("id");
        logger.info("Processing retrieve request for UGV: {}", ugvId);

        try {
            logger.debug("Checking if UGV is valid: {}", ugvId);
            if (!controlHelper.checkIfUGVIsValid(ugvId)) {
                logger.warn("Retrieve request failed: UGV ID invalid or UGV not online - {}", ugvId);
                return ResponseEntity.badRequest().body("UGV ID invalid or UGV not online");
            }

            logger.debug("Checking if UGV is retrieveable: {}", ugvId);
            if (!controlHelper.checkIfUGVIsRetrieveable(ugvId)) {
                logger.warn("Retrieve request failed: UGV is not retrieveable at the moment - {}", ugvId);
                return ResponseEntity.badRequest().body("UGV is not retrieveable at the moment");
            }

        } catch (Exception e) {
            logger.error("Error validating UGV for retrieve request - UGV ID: {}", ugvId, e);
            return ResponseEntity.internalServerError().body("Internal server error......please contact developer");
        }

        logger.debug("Fetching occupancy record for UGV: {}", ugvId);
        // Fetch occupancy record
        List<Occupancy> occupancyList = occupancyRepository.findByUgvId(ugvId);

        if (occupancyList.isEmpty()) {
            logger.warn("Retrieve request failed: UGV not parked - {}", ugvId);
            return ResponseEntity.badRequest().body("UGV Not parked");
        }

        logger.debug("Creating retrieve request entity for UGV: {}", ugvId);
        /// Create a requests entity
        Requests request = new Requests();
        request.setUgvId(ugvId);
        request.setCurrentPhase(Phase.WAITING);
        request.setCurrentTask(Task.RETRIEVE);
        request.setSchedulerFlag(true);
        request.setFinished(false);

        request = requestRepository.save(request);
        logger.info("Retrieve request created successfully - Request ID: {}, UGV ID: {}", request.getId(), ugvId);

        Map<String, Object> response = Map.of(
            "requestId", request.getId(),
            "status", "queued"
        );

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{requestId}/status")
    public ResponseEntity<Object> getRequestStatus(@RequestParam String requestId) {
        logger.debug("Status request received for request ID: {}", requestId);
        
        Requests req = requestRepository.findById(requestId).orElse(null);
        if(req==null) {
            logger.warn("Status request failed: Invalid request ID - {}", requestId);
            return ResponseEntity.badRequest().body("Invalid request ID");
        }
        
        logger.debug("Status retrieved for request ID: {}, UGV: {}, Phase: {}, Task: {}, Finished: {}", 
                requestId, req.getUgvId(), req.getCurrentPhase(), req.getCurrentTask(), req.isFinished());
        
        return ResponseEntity.ok().body(Map.of(
            "requestId", req.getId(),
            "ugvId", req.getUgvId(),
            "currentPhase", req.getCurrentPhase(),
            "currentTask", req.getCurrentTask(),
            "finished", req.isFinished()
        ));
    }
    
    @GetMapping("/ugv")
    public ResponseEntity<Object> getAllUGVs() {
        logger.info("Get all UGVs request received");
        try {
            Object ugvs = controlHelper.getAllUGVs();
            logger.info("Successfully retrieved UGVs list");
            return ResponseEntity.ok().body(ugvs);
        } catch (Exception e) {
            logger.error("Error retrieving all UGVs", e);
            throw e;
        }
    }

    @PostMapping("/action-complete")
    public ResponseEntity<Object> handleActionComplete(@RequestBody Map<String, Object> payload) {
        logger.info("Action complete callback received");
        logger.debug("Action complete payload: {}", payload);

        // get navigation id from payload
        String navigationId = (String) payload.get("jobId");
        logger.debug("Processing action complete for navigation ID: {}", navigationId);

        // get curresponding requests entity from database
        List<Requests> requests = requestRepository.findByCurrentJobId(navigationId);
        if (requests == null || requests.isEmpty()) {
            logger.warn("Action complete failed: No request found for navigation ID: {}", navigationId);
            return ResponseEntity.badRequest().body("No request found for navigation ID");
        }
        
        Requests req = requests.get(0);
        logger.info("Action complete for request ID: {}, UGV: {}, Navigation ID: {}", 
                req.getId(), req.getUgvId(), navigationId);

        req.setSchedulerFlag(true);
        requestRepository.save(req);
        logger.debug("Request scheduler flag updated for request ID: {}", req.getId());

        return ResponseEntity.ok().body("OK");
    }
}