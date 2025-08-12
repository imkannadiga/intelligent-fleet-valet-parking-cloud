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
        logger.info("Started handling pending tasks scheduler");
        List<Requests> requests = requestRepository.findBySchedulerFlag(true);
        for (Requests req : requests) {
            switch (req.getCurrentTask()) {
                case PARK:
                    // Handle parking requests
                    logger.info("Handling parking request for UGV: " + req.getUgvId());
                    switch (req.getCurrentPhase()) {
                        case WAITING:
                            String ugvId = req.getUgvId();
                            // Search for available drop off location and block it
                            DropOffSpot spot = dropOffSpotHelper.getAndBlockAvailableSpot(ugvId, SpotType.DROPOFF);

                            if(spot==null) {
                                logger.info(ugvId + " No available drop off spot found, pushing back to queue");
                                break;
                            }

                            // Send ugv ID and drop off location with callback url to the control server
                            String navigationId = controlHelper.sendRequestToControlServer(ugvId, spot.getCoordinates());

                            logger.info("Navigation request sent to control server with ID: " + navigationId);

                            req.setCurrentJobId(navigationId);
                            req.setCurrentPhase(Phase.DROP_OFF_LOCATION);
                            req.setSchedulerFlag(false);

                            requestRepository.save(req);
                            break;

                        case DROP_OFF_LOCATION:
                            // update currentPhase to PARKED
                            Map<String, Object> parkingSpot = parkingHelper.getAndBlockAvailableParkingSpot();
                            if(parkingSpot==null) {
                                logger.info("No available parking spot found, pushing back to queue");
                                break;
                            }
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

                            break;

                        case PARKED:
                            req.setFinished(true);
                            req.setSchedulerFlag(false);
                            requestRepository.save(req);
                            break;

                        default:
                            break;
                    }
                    break;

                case RETRIEVE:
                    // Handle retrieval requests
                    logger.info("Handling retrieval request for UGV: " + req.getUgvId());
                    switch (req.getCurrentPhase()) {
                        case WAITING:
                            String ugvId = req.getUgvId();
                            // Search for available drop off location and block it
                            DropOffSpot spot = dropOffSpotHelper.getAndBlockAvailableSpot(ugvId, SpotType.PICKUP);

                            if(spot==null) {
                                // No available drop off spot found
                                logger.info(ugvId + " No available pick up spot found, pushing back to queue");
                                break;
                            }

                            // Fetch occupancy record for the UGV
                            List<Occupancy> occupancies = occupancyRepository.findByUgvId(req.getUgvId());
                            if (occupancies == null || occupancies.isEmpty()) {
                                logger.error("No occupancy record found for UGV: " + ugvId);
                                break;
                            }

                            Occupancy occupancy = occupancies.get(0); // Assuming one occupancy record per UGV

                            String parkingSpotId = occupancy.getParkingSpotId();

                            occupancyRepository.delete(occupancy); // Remove occupancy record

                            // release the parking spot
                            parkingHelper.releaseParkingSpot(parkingSpotId);

                            // Send ugv ID and drop off location with callback url to the control server
                            String navigationId = controlHelper.sendRequestToControlServer(ugvId, spot.getCoordinates());

                            req.setCurrentJobId(navigationId);
                            req.setCurrentPhase(Phase.PICK_UP_LOCATION);
                            req.setSchedulerFlag(false);

                            requestRepository.save(req);
                            break;

                        case PICK_UP_LOCATION:
                            req.setCurrentPhase(Phase.DRIVE_AWAY_LOCATION);
                            // Drive away subroutine:TODO
                            req.setSchedulerFlag(false);
                            req.setFinished(true);
                            requestRepository.save(req);
                            dropOffSpotHelper.releaseSpot(req.getUgvId());
                            break;

                        case DRIVE_AWAY_LOCATION:
                            break;

                        default:
                            break;
                    }
                    break;

                default:
                    break;
            }
        }
    }
}
