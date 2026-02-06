package com.example.mapserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MapserverApplication {

	private static final Logger logger = LoggerFactory.getLogger(MapserverApplication.class);

	public static void main(String[] args) {
		logger.info("Starting Map Server Application...");
		logger.debug("Application arguments: {}", java.util.Arrays.toString(args));
		try {
			SpringApplication.run(MapserverApplication.class, args);
			logger.info("Map Server Application started successfully");
		} catch (Exception e) {
			logger.error("Failed to start Map Server Application", e);
			throw e;
		}
	}

}
