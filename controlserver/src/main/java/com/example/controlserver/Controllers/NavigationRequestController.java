package com.example.controlserver.Controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.controlserver.DTOs.CreateNavigationRequestDTO;
import com.example.controlserver.Misc.JobStatus;
import com.example.controlserver.Models.NavigationRequest;
import com.example.controlserver.Models.UGV;
import com.example.controlserver.Services.NavigationRequestService;
import com.example.controlserver.Services.UGVService;


@RestController
@RequestMapping("/api/navigation-request")
public class NavigationRequestController {

    private static final Logger logger = LoggerFactory.getLogger(NavigationRequestController.class);

    @Autowired
    private NavigationRequestService navigationRequestService;

    @Autowired
    private UGVService ugvService;

    @PostMapping
    public ResponseEntity<Object> createNavigationRequest(@RequestBody CreateNavigationRequestDTO dto) {
        logger.info("Create navigation request received - UGV ID: {}", dto.getUgvId());
        try {
            // Find the UGV by ID
            UGV ugv = ugvService.getUGVById(dto.getUgvId());

            if(ugv == null) {
                logger.warn("Create navigation request failed: Invalid UGV ID - {}", dto.getUgvId());
                return ResponseEntity.badRequest().body("Invalid UGV ID. UGV not found");
            } 

            // Create NavigationRequest
            NavigationRequest navigationRequest = new NavigationRequest();
            navigationRequest.setUgv(ugv);
            navigationRequest.setTargetPose(dto.getTargetPose());
            navigationRequest.setCallbackURL(dto.getCallbackURL());
            logger.debug("Navigation request created - UGV: {}, Callback URL: {}", dto.getUgvId(), dto.getCallbackURL());

            // Save to DB
            NavigationRequest createdNavigationRequest = navigationRequestService.saveNavigationRequest(navigationRequest);
            logger.info("Navigation request created successfully - ID: {}, UGV: {}", 
                    createdNavigationRequest.getId(), dto.getUgvId());

            return ResponseEntity.ok(createdNavigationRequest);
        } catch (Exception e) {
            logger.error("Error creating navigation request - UGV ID: {}", dto.getUgvId(), e);
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<NavigationRequest>> getAllNavigationRequests() {
        logger.debug("Get all navigation requests received");
        try {
            List<NavigationRequest> navReqs = navigationRequestService.getAllNavigationRequests();
            logger.info("Retrieved {} navigation request(s)", navReqs.size());
            return ResponseEntity.ok(navReqs);
        } catch (Exception e) {
            logger.error("Error retrieving all navigation requests", e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<NavigationRequest> getNavigationRequestById(@PathVariable String id) {
        logger.debug("Get navigation request by ID received - ID: {}", id);
        try {
            NavigationRequest navRequest = navigationRequestService.getNavigationRequestById(id);
            logger.info("Navigation request retrieved successfully - ID: {}", id);
            return ResponseEntity.ok(navRequest);
        } catch (Exception e) {
            logger.error("Error retrieving navigation request - ID: {}", id, e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<NavigationRequest> updateNavigationRequest(@PathVariable String id,
            @RequestBody NavigationRequest navDetails) {
        logger.info("Update navigation request received - ID: {}", id);
        try {
            NavigationRequest updatedNavRequest = navigationRequestService.updateNavigationRequest(id, navDetails);
            logger.info("Navigation request updated successfully - ID: {}", id);
            return ResponseEntity.ok(updatedNavRequest);
        } catch (Exception e) {
            logger.error("Error updating navigation request - ID: {}", id, e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNavigationRequest(@PathVariable String id) {
        logger.info("Delete navigation request received - ID: {}", id);
        try {
            navigationRequestService.deleteNavigationRequest(id);
            logger.info("Navigation request deleted successfully - ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting navigation request - ID: {}", id, e);
            throw e;
        }
    }

    @PostMapping("/{id}/completed")
    public ResponseEntity<Void> completeNavigationRequest(@PathVariable String id) {
        logger.info("Complete navigation request received - ID: {}", id);
        try {
            NavigationRequest navReq = navigationRequestService.getNavigationRequestById(id);
            navReq.setJobStatus(JobStatus.COMPLETED);
            navigationRequestService.updateNavigationRequest(id, navReq);
            logger.info("Navigation request marked as completed - ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error completing navigation request - ID: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/{ugvId}/get-pending-job")
    public ResponseEntity<?> getPendingJob(@PathVariable String ugvId) {
        logger.info("Get pending job request received - UGV ID: {}", ugvId);
        try {
            if (!ugvService.isUGVIDValid(ugvId)) {
                logger.warn("Get pending job failed: Invalid UGV ID - {}", ugvId);
                return ResponseEntity.badRequest().body("Invalid UGV ID");
            }

            List<NavigationRequest> pendingJobs = navigationRequestService.getPendingJobsByUGVId(ugvId);
            if (pendingJobs.isEmpty()) {
                logger.debug("No pending jobs found for UGV - {}", ugvId);
                return ResponseEntity.notFound().build();
            }

            NavigationRequest pendingJob = pendingJobs.get(0);
            pendingJob.setJobStatus(JobStatus.STARTED);
            navigationRequestService.updateNavigationRequest(pendingJob.getId(), pendingJob);
            logger.info("Pending job retrieved and marked as started - Job ID: {}, UGV: {}", 
                    pendingJob.getId(), ugvId);

            return ResponseEntity.ok(pendingJob);
        } catch (Exception e) {
            logger.error("Error getting pending job - UGV ID: {}", ugvId, e);
            throw e;
        }
    }

}
