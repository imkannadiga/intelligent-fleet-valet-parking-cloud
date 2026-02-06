package com.example.auth.Helpers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.auth.Entities.users.UGV;

@Service
public class ControlHelper {

    private static final Logger logger = LoggerFactory.getLogger(ControlHelper.class);

    @Autowired
    private RestTemplate restTemplate;

    private final String CONTROL_SERVER_ADDRESS = "http://controlserver:10001/api";

    public List<UGV> getAllUGVs() {
        logger.debug("Fetching all UGVs from control server");
        try {
            ResponseEntity<List<UGV>> resp = restTemplate.exchange(
                    CONTROL_SERVER_ADDRESS + "/ugv",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<UGV>>() {
                    });
            logger.info("Successfully fetched {} UGV(s) from control server", resp.getBody() != null ? resp.getBody().size() : 0);
            return resp.getBody();
        } catch (RestClientException e) {
            logger.error("Error fetching all UGVs from control server", e);
            throw e;
        }
    }

    public UGV getUGVById(String id) {
        logger.debug("Fetching UGV by ID: {}", id);
        try {
            ResponseEntity<UGV> resp = restTemplate.exchange(
                    CONTROL_SERVER_ADDRESS + "/ugv/" + id,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<UGV>() {
                    });
            if (resp.getBody() != null) {
                logger.info("Successfully fetched UGV with ID: {}", id);
            } else {
                logger.warn("UGV not found with ID: {}", id);
            }
            return resp.getBody();
        } catch (RestClientException e) {
            logger.error("Error fetching UGV by ID: {}", id, e);
            throw e;
        }
    }

    public UGV createUGV(UGV ugv) {
        logger.info("Creating UGV in control server - name: {}, type: {}", ugv.getName(), ugv.getType());
        try {
            ResponseEntity<UGV> resp = restTemplate.exchange(
                    CONTROL_SERVER_ADDRESS + "/ugv",
                    HttpMethod.POST,
                    new HttpEntity<>(ugv),
                    new ParameterizedTypeReference<UGV>() {
                    });
            logger.info("UGV created successfully in control server - ID: {}, name: {}", 
                    resp.getBody() != null ? resp.getBody().getId() : "unknown", ugv.getName());
            return resp.getBody();
        } catch (RestClientException e) {
            logger.error("Error creating UGV in control server - name: {}, type: {}", ugv.getName(), ugv.getType(), e);
            throw e;
        }
    }

}
