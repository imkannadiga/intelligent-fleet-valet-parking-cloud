package com.example.valetparking.Schedulers;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.valetparking.Helpers.ControlHelper;
import com.example.valetparking.Helpers.DropOffSpotHelper;
import com.example.valetparking.Helpers.ParkingHelper;
import com.example.valetparking.Helpers.Phase;
import com.example.valetparking.Helpers.SpotType;
import com.example.valetparking.Models.DropOffSpot;
import com.example.valetparking.Models.Occupancy;
import com.example.valetparking.Models.Requests;
import com.example.valetparking.Repositories.OccupancyRepository;
import com.example.valetparking.Repositories.RequestRepository;

@Component
public class HandlePendingTasks {

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    ParkingHelper parkingHelper;

    @Autowired
    DropOffSpotHelper dropOffSpotHelper;

    @Autowired
    ControlHelper controlHelper;

    @Autowired
    OccupancyRepository occupancyRepository;

    private static final Logger logger = LoggerFactory.getLogger(HandlePendingTasks.class);


    @Scheduled(fixedDelay = 10000)
    public void handleNextStep() {
        logger.debug("Started handling pending tasks scheduler");
        List<Requests> requests = requestRepository.findBySchedulerFlag(true);
        logger.info("Found {} pending request(s) to process", requests.size());
        
        for (Requests req : requests) {
            logger.debug("Processing request - ID: {}, UGV: {}, Task: {}, Phase: {}", 
                    req.getId(), req.getUgvId(), req.getCurrentTask(), req.getCurrentPhase());
            
            switch (req.getCurrentTask()) {
                case PARK:
                    // Handle parking requests
                    logger.info("Handling parking request for UGV: {}", req.getUgvId());
                    switch (req.getCurrentPhase()) {
                        case WAITING:
                            String ugvId = req.getUgvId();
                            logger.debug("Processing PARK request in WAITING phase for UGV: {}", ugvId);
                            
                            // Search for available drop off location and block it
                            DropOffSpot spot = dropOffSpotHelper.getAndBlockAvailableSpot(ugvId, SpotType.DROPOFF);

                            if(spot==null) {
                                logger.info("No available drop off spot found for UGV: {}, pushing back to queue", ugvId);
                                break;
                            }

                            // Send ugv ID and drop off location with callback url to the control server
                            String navigationId = controlHelper.sendRequestToControlServer(ugvId, spot.getCoordinates());

                            logger.info("Navigation request sent to control server - Navigation ID: {}, UGV: {}", navigationId, ugvId);

                            req.setCurrentJobId(navigationId);
                            req.setCurrentPhase(Phase.DROP_OFF_LOCATION);
                            req.setSchedulerFlag(false);

                            requestRepository.save(req);
                            logger.debug("Request updated - ID: {}, Phase: DROP_OFF_LOCATION", req.getId());
                            break;

                        case DROP_OFF_LOCATION:
                            logger.debug("Processing PARK request in DROP_OFF_LOCATION phase for UGV: {}", req.getUgvId());
                            
                            // update currentPhase to PARKED
                            Map<String, Object> parkingSpot = parkingHelper.getAndBlockAvailableParkingSpot();
                            if(parkingSpot==null) {
                                logger.info("No available parking spot found for UGV: {}, pushing back to queue", req.getUgvId());
                                break;
                            }
                            
                            logger.info("Found parking spot: {} for UGV: {}", parkingSpot.get("id"), req.getUgvId());
                            
                            req.setCurrentPhase(Phase.PARKED);
                            // Parking subroutine
                            dropOffSpotHelper.releaseSpot(req.getUgvId());
                            String newNavId = controlHelper.sendRequestToControlServer(req.getUgvId(), parkingSpot);
                            req.setCurrentJobId(newNavId);
                            req.setParkingSpotId((String) parkingSpot.get("id"));
                            req.setSchedulerFlag(false);
                            requestRepository.save(req);

                            // Create occupancy record
                            Occupancy occupancy = new Occupancy();
                            occupancy.setUgvId(req.getUgvId());
                            occupancy.setParkingSpotId(req.getParkingSpotId());
                            occupancyRepository.save(occupancy);
                            logger.info("Occupancy record created - UGV: {}, Parking Spot: {}", req.getUgvId(), req.getParkingSpotId());
                            break;

                        case PARKED:
                            logger.info("Parking task completed for UGV: {}", req.getUgvId());
                            req.setFinished(true);
                            req.setSchedulerFlag(false);
                            requestRepository.save(req);
                            logger.debug("Request marked as finished - ID: {}", req.getId());
                            break;

                        default:
                            logger.warn("Unknown phase for PARK task: {} - Request ID: {}", req.getCurrentPhase(), req.getId());
                            break;
                    }
                    break;

                case RETRIEVE:
                    // Handle retrieval requests
                    logger.info("Handling retrieval request for UGV: {}", req.getUgvId());
                    switch (req.getCurrentPhase()) {
                        case WAITING:
                            String ugvId = req.getUgvId();
                            logger.debug("Processing RETRIEVE request in WAITING phase for UGV: {}", ugvId);
                            
                            // Search for available drop off location and block it
                            DropOffSpot spot = dropOffSpotHelper.getAndBlockAvailableSpot(ugvId, SpotType.PICKUP);

                            if(spot==null) {
                                logger.info("No available pick up spot found for UGV: {}, pushing back to queue", ugvId);
                                break;
                            }

                            // Fetch occupancy record for the UGV
                            List<Occupancy> occupancies = occupancyRepository.findByUgvId(req.getUgvId());
                            if (occupancies == null || occupancies.isEmpty()) {
                                logger.error("No occupancy record found for UGV: {}", ugvId);
                                break;
                            }

                            Occupancy occupancy = occupancies.get(0); // Assuming one occupancy record per UGV
                            String parkingSpotId = occupancy.getParkingSpotId();
                            logger.debug("Found occupancy record - UGV: {}, Parking Spot: {}", ugvId, parkingSpotId);

                            occupancyRepository.delete(occupancy); // Remove occupancy record
                            logger.debug("Occupancy record deleted for UGV: {}", ugvId);

                            // release the parking spot
                            parkingHelper.releaseParkingSpot(parkingSpotId);

                            // Send ugv ID and drop off location with callback url to the control server
                            String navigationId = controlHelper.sendRequestToControlServer(ugvId, spot.getCoordinates());

                            req.setCurrentJobId(navigationId);
                            req.setCurrentPhase(Phase.PICK_UP_LOCATION);
                            req.setSchedulerFlag(false);

                            requestRepository.save(req);
                            logger.info("Request updated - ID: {}, Phase: PICK_UP_LOCATION, Navigation ID: {}", 
                                    req.getId(), navigationId);
                            break;

                        case PICK_UP_LOCATION:
                            logger.info("Retrieval task completed for UGV: {}", req.getUgvId());
                            req.setCurrentPhase(Phase.DRIVE_AWAY_LOCATION);
                            // Drive away subroutine:TODO
                            req.setSchedulerFlag(false);
                            req.setFinished(true);
                            requestRepository.save(req);
                            dropOffSpotHelper.releaseSpot(req.getUgvId());
                            logger.debug("Request marked as finished - ID: {}", req.getId());
                            break;

                        case DRIVE_AWAY_LOCATION:
                            logger.debug("UGV in DRIVE_AWAY_LOCATION phase - UGV: {}", req.getUgvId());
                            break;

                        default:
                            logger.warn("Unknown phase for RETRIEVE task: {} - Request ID: {}", req.getCurrentPhase(), req.getId());
                            break;
                    }
                    break;

                default:
                    logger.warn("Unknown task type: {} - Request ID: {}", req.getCurrentTask(), req.getId());
                    break;
            }
        }
        logger.debug("Completed handling pending tasks scheduler");
    }
}
