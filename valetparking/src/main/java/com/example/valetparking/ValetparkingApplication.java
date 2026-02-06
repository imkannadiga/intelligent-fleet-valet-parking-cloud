package com.example.valetparking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ValetparkingApplication {

	private static final Logger logger = LoggerFactory.getLogger(ValetparkingApplication.class);

	public static void main(String[] args) {
		logger.info("Starting Valet Parking Application...");
		logger.debug("Application arguments: {}", java.util.Arrays.toString(args));
		try {
			SpringApplication.run(ValetparkingApplication.class, args);
			logger.info("Valet Parking Application started successfully");
		} catch (Exception e) {
			logger.error("Failed to start Valet Parking Application", e);
			throw e;
		}
	}
}
