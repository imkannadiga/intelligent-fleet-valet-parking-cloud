package com.example.controlserver.Services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.controlserver.Misc.JobStatus;
import com.example.controlserver.Models.NavigationRequest;
import com.example.controlserver.Repositories.NavigationRequestRepository;

@Service
public class NavigationRequestService {
    
    private static final Logger logger = LoggerFactory.getLogger(NavigationRequestService.class);
    
    @Autowired
    private NavigationRequestRepository navigationRequestRepository;

    public NavigationRequest saveNavigationRequest(NavigationRequest navRequest) {
        logger.debug("Saving navigation request - UGV: {}", navRequest.getUgv() != null ? navRequest.getUgv().getId() : "unknown");
        NavigationRequest saved = navigationRequestRepository.save(navRequest);
        logger.info("Navigation request saved successfully - ID: {}", saved.getId());
        return saved;
    }

    public List<NavigationRequest> getAllNavigationRequests() {
        logger.debug("Fetching all navigation requests");
        List<NavigationRequest> requests = navigationRequestRepository.findAll();
        logger.info("Retrieved {} navigation request(s)", requests.size());
        return requests;
    }

    public NavigationRequest getNavigationRequestById(String id) {
        logger.debug("Fetching navigation request by ID: {}", id);
        return navigationRequestRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Navigation request not found - ID: {}", id);
                    return new RuntimeException("Navigation request not found");
                });
    }

    public NavigationRequest updateNavigationRequest(String id, NavigationRequest navDetails) {
        logger.debug("Updating navigation request - ID: {}", id);
        NavigationRequest navRequest = this.getNavigationRequestById(id);
        navRequest.setUgv(navDetails.getUgv());
        navRequest.setTargetPose(navDetails.getTargetPose());
        navRequest.setJobStatus(navDetails.getJobStatus());
        navRequest.setCallbackURL(navDetails.getCallbackURL());
        navRequest.setComment(navDetails.getComment());
        NavigationRequest updated = navigationRequestRepository.save(navRequest);
        logger.info("Navigation request updated successfully - ID: {}, Status: {}", id, navDetails.getJobStatus());
        return updated;
    }

    public void deleteNavigationRequest(String id) {
        logger.info("Deleting navigation request - ID: {}", id);
        navigationRequestRepository.deleteById(id);
        logger.info("Navigation request deleted successfully - ID: {}", id);
    }

    public List<NavigationRequest> getPendingJobs() {
        logger.debug("Fetching pending navigation jobs");
        List<NavigationRequest> jobs = navigationRequestRepository.findByJobStatus(JobStatus.PENDING);
        logger.debug("Found {} pending navigation job(s)", jobs.size());
        return jobs;
    }

    public List<NavigationRequest> getCompletedJobs() {
        logger.debug("Fetching completed navigation jobs");
        List<NavigationRequest> jobs = navigationRequestRepository.findByJobStatus(JobStatus.COMPLETED);
        logger.debug("Found {} completed navigation job(s)", jobs.size());
        return jobs;
    }

    public void generateCallBack(String jobId, String callbackURL) {
        logger.info("Generating callback for navigation request - ID: {}, Callback URL: {}", jobId, callbackURL);
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("jobId", jobId);
            requestBody.put("status", "completed");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);
            
            logger.debug("Sending callback POST request to: {}", callbackURL);
            ResponseEntity<String> response = restTemplate.exchange(callbackURL, HttpMethod.POST, requestEntity, String.class);
            logger.info("Callback response received - Status: {}, Body: {}", response.getStatusCode(), response.getBody());
            
            NavigationRequest navReq = this.getNavigationRequestById(jobId);
            navReq.setComment(response.toString());
            navReq.setJobStatus(JobStatus.FINISHED);
            this.updateNavigationRequest(jobId, navReq);
            logger.info("Navigation request marked as FINISHED - ID: {}", jobId);
        } catch (RestClientException e) {
            logger.error("Error generating callback for navigation request - ID: {}, URL: {}", jobId, callbackURL, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error generating callback for navigation request - ID: {}", jobId, e);
            throw e;
        }
    }

    public List<NavigationRequest> getPendingJobsByUGVId(String ugvId) {
        logger.debug("Fetching pending navigation jobs for UGV: {}", ugvId);
        List<NavigationRequest> jobs = navigationRequestRepository.findByUgv_IdAndJobStatus(ugvId, JobStatus.PENDING);
        logger.debug("Found {} pending navigation job(s) for UGV: {}", jobs.size(), ugvId);
        return jobs;
    }

}
