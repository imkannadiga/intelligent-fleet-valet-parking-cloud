package com.example.parking_spot_manager.Controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.parking_spot_manager.Repositories.ParkingSpotRepository;
import com.example.parking_spot_manager.Models.ParkingSpot;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/parking-spot")
public class ParkingSpotController {

    private static final Logger logger = LoggerFactory.getLogger(ParkingSpotController.class);

    @Autowired
    private ParkingSpotRepository parkingSpotRepository;

    // GET all parking spots
    @GetMapping
    public List<ParkingSpot> getAllParkingSpots() {
        logger.debug("Get all parking spots request received");
        try {
            List<ParkingSpot> spots = parkingSpotRepository.findAll();
            logger.info("Retrieved {} parking spot(s)", spots.size());
            return spots;
        } catch (Exception e) {
            logger.error("Error retrieving all parking spots", e);
            throw e;
        }
    }

    // GET a parking spot by ID
    @GetMapping("/{id}")
    public ResponseEntity<ParkingSpot> getParkingSpotById(@PathVariable String id) {
        logger.debug("Get parking spot by ID request received - ID: {}", id);
        try {
            Optional<ParkingSpot> spot = parkingSpotRepository.findById(id);
            if (spot.isPresent()) {
                logger.info("Parking spot retrieved successfully - ID: {}", id);
                return ResponseEntity.ok(spot.get());
            } else {
                logger.warn("Parking spot not found - ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error retrieving parking spot - ID: {}", id, e);
            throw e;
        }
    }

    // CREATE a new parking spot
    @PostMapping
    public ResponseEntity<ParkingSpot> createParkingSpot(@RequestBody ParkingSpot parkingSpot) {
        logger.info("Create parking spot request received - x: {}, y: {}, theta: {}", 
                parkingSpot.getX(), parkingSpot.getY(), parkingSpot.getTheta());
        try {
            ParkingSpot saved = parkingSpotRepository.save(parkingSpot);
            logger.info("Parking spot created successfully - ID: {}", saved.getId());
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating parking spot", e);
            throw e;
        }
    }

    // BLOCK a parking spot
    @PostMapping("/{id}/block")
    public ResponseEntity<String> blockParkingSpot(@PathVariable String id) {
        logger.info("Block parking spot request received - ID: {}", id);
        try {
            if (!parkingSpotRepository.existsById(id)) {
                logger.warn("Block parking spot failed: Spot not found - ID: {}", id);
                return ResponseEntity.notFound().build();
            }

            Optional<ParkingSpot> spot = parkingSpotRepository.findById(id);
            spot.get().setOccupied(true);
            parkingSpotRepository.save(spot.get());
            logger.info("Parking spot blocked successfully - ID: {}", id);

            return ResponseEntity.status(200).body("Updated successfully");
        } catch (Exception e) {
            logger.error("Error blocking parking spot - ID: {}", id, e);
            throw e;
        }
    }

    // RELEASE a parking spot
    @PostMapping("/{id}/release")
    public ResponseEntity<String> releaseParkingSpot(@PathVariable String id) {
        logger.info("Release parking spot request received - ID: {}", id);
        try {
            if (!parkingSpotRepository.existsById(id)) {
                logger.warn("Release parking spot failed: Spot not found - ID: {}", id);
                return ResponseEntity.notFound().build();
            }

            Optional<ParkingSpot> spot = parkingSpotRepository.findById(id);
            spot.get().setOccupied(false);
            parkingSpotRepository.save(spot.get());
            logger.info("Parking spot released successfully - ID: {}", id);

            return ResponseEntity.status(200).body("Updated successfully");
        } catch (Exception e) {
            logger.error("Error releasing parking spot - ID: {}", id, e);
            throw e;
        }
    }

    // DELETE a parking spot
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteParkingSpot(@PathVariable String id) {
        logger.info("Delete parking spot request received - ID: {}", id);
        try {
            if (!parkingSpotRepository.existsById(id)) {
                logger.warn("Delete parking spot failed: Spot not found - ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            parkingSpotRepository.deleteById(id);
            logger.info("Parking spot deleted successfully - ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting parking spot - ID: {}", id, e);
            throw e;
        }
    }

    // Get first available parking spot
    @GetMapping("/get-free-space")
	public ResponseEntity<?> getFirstFreeSpace() {
		logger.debug("Get free space request received");
		try {
			// Retrieve all free parking spots from the repository
			List<ParkingSpot> allSpots = parkingSpotRepository.findByOccupied(false);

			if (allSpots == null || allSpots.isEmpty()) {
				logger.warn("No free parking spots available");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No free parking spots available.");
			}

			// Return the first available free spot
			ParkingSpot firstSpot = allSpots.get(0);
			logger.info("Free parking spot found - ID: {}, x: {}, y: {}, theta: {}", 
					firstSpot.getId(), firstSpot.getX(), firstSpot.getY(), firstSpot.getTheta());

			return ResponseEntity.ok(firstSpot);
		} catch (Exception e) {
			logger.error("Error getting free space", e);
			throw e;
		}
	}

}
