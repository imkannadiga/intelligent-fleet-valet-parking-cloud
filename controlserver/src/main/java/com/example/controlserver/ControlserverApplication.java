package com.example.controlserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ControlserverApplication {

	private static final Logger logger = LoggerFactory.getLogger(ControlserverApplication.class);

	public static void main(String[] args) {
		logger.info("Starting Control Server Application...");
		logger.debug("Application arguments: {}", java.util.Arrays.toString(args));
		try {
			SpringApplication.run(ControlserverApplication.class, args);
			logger.info("Control Server Application started successfully");
		} catch (Exception e) {
			logger.error("Failed to start Control Server Application", e);
			throw e;
		}
	}

}
