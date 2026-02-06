package com.example.parking_spot_manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ParkingSpotManagerApplication {

	private static final Logger logger = LoggerFactory.getLogger(ParkingSpotManagerApplication.class);

	public static void main(String[] args) {
		logger.info("Starting Parking Spot Manager Application...");
		logger.debug("Application arguments: {}", java.util.Arrays.toString(args));
		try {
			SpringApplication.run(ParkingSpotManagerApplication.class, args);
			logger.info("Parking Spot Manager Application started successfully");
		} catch (Exception e) {
			logger.error("Failed to start Parking Spot Manager Application", e);
			throw e;
		}
	}

}
