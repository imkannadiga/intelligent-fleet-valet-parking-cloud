package com.example.heartbeat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class HeartbeatApplication {

	private static final Logger logger = LoggerFactory.getLogger(HeartbeatApplication.class);

	public static void main(String[] args) {
		logger.info("Starting Heartbeat Application...");
		logger.debug("Application arguments: {}", java.util.Arrays.toString(args));
		try {
			SpringApplication.run(HeartbeatApplication.class, args);
			logger.info("Heartbeat Application started successfully");
		} catch (Exception e) {
			logger.error("Failed to start Heartbeat Application", e);
			throw e;
		}
	}

}
