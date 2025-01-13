package com.valetparking.valetparking.controllers;

import org.springframework.web.bind.annotation.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.TextMessage;

import com.valetparking.valetparking.entities.UGV;
import com.valetparking.valetparking.handlers.SocketConnectionHandler;
import com.valetparking.valetparking.helpers.UGVHelper;
import com.valetparking.valetparking.misc.Status;
import com.valetparking.valetparking.services.UGVService;

import java.util.Arrays;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/valetparking")
public class ValetParkingController {

    @Autowired
    private UGVService ugvService;

    @Autowired
    private SocketConnectionHandler socketConnectionHandler;

    UGVHelper helper = new UGVHelper();

    int[] DROP_OFF_LOCATION = new int[] { 1, 2, 3, 4 };
    int[] PICK_UP_LOCATION = new int[] { 1, 2, 3, 4 };

    @PostMapping("/park")
    public ResponseEntity<String> parkUGV(@RequestParam String ugvId) {
        try {
            // Check if UGV is ONLINE
            boolean isOnline = ugvService.checkUGVStatus(ugvId, Status.ONLINE);
            if (!isOnline) {
                return ResponseEntity.badRequest().body("UGV is not online");
            }

            // Get the UGV socket session ID
            UGV ugv = ugvService.getUGVById(ugvId);

            ugv.setStatus(Status.BUSY);

            ugvService.updateUGV(ugvId, ugv);

            TextMessage message = new TextMessage(Arrays.toString(DROP_OFF_LOCATION));

            // Send navigation request to 'DROP_OFF_LOCATION'
            socketConnectionHandler.sendMessageToClient(ugv.getSessionId(), message);

            return ResponseEntity.ok("Navigation to drop off location started");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/park/dropoffcompleted")
    public ResponseEntity<String> postDropOff(@RequestParam String ugvId) {

        try {
            // Thread.sleep(5000);

            // get nearest parking spot
            RestTemplate restTemplate = new RestTemplate();
            String parkingServiceUrl = "http://localhost:8088/getFirstFreeSpace";
            String parkingSpot = restTemplate.getForObject(parkingServiceUrl, String.class);

            JSONObject parkingSpotJson = new JSONObject(parkingSpot);

            // block the parking spot

            String parkingServiceBlockUrl = "http://localhost:8088/UpdateParkingStatus";

            parkingSpotJson.put("status", "occupied");

            System.out.println(parkingSpotJson);

            restTemplate.put(parkingServiceBlockUrl, parkingSpotJson.toMap());

            // Navigate to parking spot

            UGV ugv = ugvService.getUGVById(ugvId);

            TextMessage message = new TextMessage(parkingSpot);

            socketConnectionHandler.sendMessageToClient(ugv.getSessionId(), message);

            return ResponseEntity.ok("Navigation to parking spot started!!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error occurred: " + e.getMessage());
        }

    }

    @PostMapping("/park/parkingcompleted")
    public ResponseEntity<String> postParking(@RequestBody String ugvId) {

        UGV ugv = ugvService.getUGVById(ugvId);
        ugv.setStatus(Status.PARKED);
        ugvService.updateUGV(ugvId, ugv);

        return ResponseEntity.ok("Status updated");

    }


    @PostMapping("/retrieve")
    public ResponseEntity<String> retrieveUGV(@RequestParam String ugvId) {
        try {
            // Check if UGV is PARKED
            boolean isParked = helper.checkUGVStatus(ugvId, Status.PARKED);
            if (!isParked) {
                return ResponseEntity.badRequest().body("UGV is not parked");
            }

            // Get the UGV socket session ID
            UGV ugv = ugvService.getUGVById(ugvId);

            TextMessage message = new TextMessage(Arrays.toString(PICK_UP_LOCATION));

            // Send navigation request to 'DROP_OFF_LOCATION'
            socketConnectionHandler.sendMessageToClient(ugv.getSessionId(), message);

            ugv.setStatus(Status.BUSY);
            ugvService.updateUGV(ugvId, ugv);

            return ResponseEntity.ok("Navigation to pick up location started");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancelOperation(@RequestBody String ugvId) {
        //TODO: process POST request
        return ResponseEntity.status(501).body("Not yet implemented.......");
        
    }
    


}
