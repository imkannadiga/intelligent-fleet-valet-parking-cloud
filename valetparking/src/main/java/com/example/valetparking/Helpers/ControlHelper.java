package com.example.valetparking.Helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ControlHelper {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HeartbeatHelper heartbeatHelper;

    private final String CONTROL_SERVER_ADDRESS = "http://controlserver:10001/api";

    private final String CALLBACK_URL = "http://valetparking:9000/api/valetparking/action-complete";

    private static final Logger logger = LoggerFactory.getLogger(ControlHelper.class);

    public String sendRequestToControlServer(String ugvId, Map<String, Object> targetPose) {

        // Generate payload to send to server
        Map<String, Object> payload = new HashMap<>();
        payload.put("ugvId", ugvId);
        payload.put("targetPose", targetPose);
        payload.put("callbackURL", CALLBACK_URL);

        // Send message to control server
        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> resp = restTemplate.postForEntity(CONTROL_SERVER_ADDRESS + "/navigation-request", payload,
                Map.class);
        logger.info("Response from control server: " + resp.getBody());

        return (String) resp.getBody().get("id");

    }

    public boolean checkIfUGVIsValid(String ugvId) throws Exception {

        String resp_str = restTemplate.getForObject(CONTROL_SERVER_ADDRESS + "/ugv", String.class);

        JsonObject resp = new JsonParser().parse(resp_str).getAsJsonArray().get(0).getAsJsonObject();

        // System.out.println(resp.toString());

        // check to see if recieved response is empty or invalid
        if (resp.isJsonNull() || !resp.has("id"))
            return false;

        // System.out.println("Json not null and has ID");

        // check to see if UGV is online
        if (!heartbeatHelper.isUGVOnline(ugvId))
            return false;

        return true;
    }

    public boolean checkIfUGVIsParkable(String ugvId) {

        return true;

    }

    public boolean checkIfUGVIsRetrieveable(String ugvId) {

        return true;

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<Map> getAllUGVs() {
        ResponseEntity<List> resp = restTemplate.getForEntity(CONTROL_SERVER_ADDRESS + "/ugv", List.class);
        return resp.getBody();
    }
}
