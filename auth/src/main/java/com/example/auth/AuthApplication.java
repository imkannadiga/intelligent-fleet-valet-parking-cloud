package com.example.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class AuthApplication {

	private static final Logger logger = LoggerFactory.getLogger(AuthApplication.class);

	public static void main(String[] args) {
		logger.info("Starting Auth Application...");
		logger.debug("Application arguments: {}", java.util.Arrays.toString(args));
		try {
			SpringApplication.run(AuthApplication.class, args);
			logger.info("Auth Application started successfully");
		} catch (Exception e) {
			logger.error("Failed to start Auth Application", e);
			throw e;
		}
	}

	@Bean
    public RestTemplate restTemplate() {
		logger.debug("Creating RestTemplate bean");
        return new RestTemplate();
    }

}
