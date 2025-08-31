package com.example.auth.Helpers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.auth.Entities.users.UGV;

@Service
public class ControlHelper {

    @Autowired
    private RestTemplate restTemplate;

    private final String CONTROL_SERVER_ADDRESS = "http://controlserver:10001/api";

    public List<UGV> getAllUGVs() {
        ResponseEntity<List<UGV>> resp = restTemplate.exchange(
                CONTROL_SERVER_ADDRESS + "/ugv",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<UGV>>() {
                });
        return resp.getBody();
    }

    public UGV getUGVById(String id) {
        ResponseEntity<UGV> resp = restTemplate.exchange(
                CONTROL_SERVER_ADDRESS + "/ugv/" + id,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<UGV>() {
                });
        return resp.getBody();
    }

    public UGV createUGV(UGV ugv) {
        ResponseEntity<UGV> resp = restTemplate.exchange(
                CONTROL_SERVER_ADDRESS + "/ugv",
                HttpMethod.POST,
                new HttpEntity<>(ugv),
                new ParameterizedTypeReference<UGV>() {
                });
        return resp.getBody();
    }

}
