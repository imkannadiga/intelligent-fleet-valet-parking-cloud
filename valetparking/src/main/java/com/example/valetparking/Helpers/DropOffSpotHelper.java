package com.example.valetparking.Helpers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.valetparking.Models.DropOffSpot;
import com.example.valetparking.Repositories.DropOffSpotRepository;

@Service
public class DropOffSpotHelper {

    private static final Logger logger = LoggerFactory.getLogger(DropOffSpotHelper.class);

    @Autowired
    DropOffSpotRepository dropOffSpotRepository;

    public DropOffSpot getAndBlockAvailableSpot(String ugvId, SpotType spotType) {
        logger.debug("Searching for available {} spot for UGV: {}", spotType, ugvId);
        
        List<DropOffSpot> freeSpots = dropOffSpotRepository.findByOccupiedAndSpotType(false, spotType);
        if(freeSpots.isEmpty()) {
            logger.warn("No available {} spot found for UGV: {}", spotType, ugvId);
            return null;
        }
        
        DropOffSpot current = freeSpots.get(0);
        logger.info("Found available {} spot: {} for UGV: {}", spotType, current.getId(), ugvId);
        
        current.setOccupied(true);
        current.setUgvId(ugvId);
        dropOffSpotRepository.save(current);
        logger.info("{} spot blocked successfully - Spot ID: {}, UGV: {}", spotType, current.getId(), ugvId);
        
        return current;
    }

    public void releaseSpot(String ugvId) {
        logger.debug("Releasing spot for UGV: {}", ugvId);
        
        List<DropOffSpot> occupiedSpots = dropOffSpotRepository.findByUgvId(ugvId);
        if (occupiedSpots == null || occupiedSpots.isEmpty()) {
            logger.warn("No occupied spot found for UGV: {}", ugvId);
            return;
        }
        
        DropOffSpot current = occupiedSpots.get(0);
        logger.info("Releasing spot: {} for UGV: {}", current.getId(), ugvId);
        
        current.setOccupied(false);
        current.setUgvId("");
        dropOffSpotRepository.save(current);
        logger.info("Spot released successfully - Spot ID: {}, UGV: {}", current.getId(), ugvId);
    }
    
}
