package com.example.controlserver.Schedulers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.controlserver.Models.NavigationRequest;
import com.example.controlserver.Services.NavigationRequestService;

@Component
public class NavigationCallbackScheduler {

    @Autowired
    NavigationRequestService navigationRequestService;

    private static final Logger logger = LoggerFactory.getLogger(NavigationCallbackScheduler.class);
    
    @Scheduled(fixedDelay = 10000)
    public void completedNavigationCallbacks() {
        logger.debug("Started navigation callback scheduler");
        try {
            // Get all completed navigation requests
            List<NavigationRequest> completedRequests = navigationRequestService.getCompletedJobs();
            logger.info("Found {} completed navigation request(s) to process", completedRequests.size());

            for(NavigationRequest req : completedRequests) {
                logger.debug("Generating callback for navigation request - ID: {}, Callback URL: {}", 
                        req.getId(), req.getCallbackURL());
                navigationRequestService.generateCallBack(req.getId(), req.getCallbackURL());
                logger.info("Callback generated successfully for navigation request - ID: {}", req.getId());
            }
        } catch (Exception e) {
            logger.error("Error processing navigation callbacks", e);
        }
        logger.debug("Completed navigation callback scheduler");
    }
}
