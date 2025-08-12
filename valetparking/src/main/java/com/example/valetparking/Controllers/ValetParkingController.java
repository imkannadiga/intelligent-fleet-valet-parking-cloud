package com.example.valetparking.Controllers;

import com.example.valetparking.Helpers.ControlHelper;
import com.example.valetparking.Helpers.Phase;
import com.example.valetparking.Helpers.Task;
import com.example.valetparking.Models.Occupancy;
import com.example.valetparking.Models.Requests;
import com.example.valetparking.Repositories.OccupancyRepository;
import com.example.valetparking.Repositories.RequestRepository;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/valetparking")
public class ValetParkingController {

    @Autowired
    private ControlHelper controlHelper;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private OccupancyRepository occupancyRepository;

    @PostMapping("/park")
    public ResponseEntity<Object> patk(@RequestBody Map<String, Object> payload) {
        String ugvId = (String) payload.get("ugvID");

        try {
            if (!controlHelper.checkIfUGVIsValid(ugvId)) {
                return ResponseEntity.badRequest().body("UGV ID invalid or UGV not online");
            }

            if (!controlHelper.checkIfUGVIsParkable(ugvId)) {
                return ResponseEntity.badRequest().body("UGV is not retrieveable at the moment");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal server error......please contact developer");
        }

        // Create a requests entity
        Requests request = new Requests();
        request.setUgvId(ugvId);
        request.setCurrentPhase(Phase.WAITING);
        request.setCurrentTask(Task.PARK);
        request.setSchedulerFlag(true);
        request.setFinished(false);

        requestRepository.save(request);

        return ResponseEntity.ok().body("Request queued");

    }
    
    @PostMapping("/retrieve")
    public ResponseEntity<Object> retrieve(@RequestBody Map<String, Object> payload) {
        String ugvId = (String) payload.get("ugvID");

        try {
            if (!controlHelper.checkIfUGVIsValid(ugvId)) {
                return ResponseEntity.badRequest().body("UGV ID invalid or UGV not online");
            }

            if (!controlHelper.checkIfUGVIsRetrieveable(ugvId)) {
                return ResponseEntity.badRequest().body("UGV is not retrieveable at the moment");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal server error......please contact developer");
        }

        // Fetch occupancy record
        List<Occupancy> occupancyList = occupancyRepository.findByUgvId(ugvId);

        if (occupancyList.isEmpty()) {
            return ResponseEntity.badRequest().body("UGV Not parked");
        }

        /// Create a requests entity
        Requests request = new Requests();
        request.setUgvId(ugvId);
        request.setCurrentPhase(Phase.WAITING);
        request.setCurrentTask(Task.RETRIEVE);
        request.setSchedulerFlag(true);
        request.setFinished(false);

        requestRepository.save(request);

        return ResponseEntity.ok().body("Request queued");
    }

    @GetMapping("/ugv")
    public ResponseEntity<Object> getAllUGVs() {
        return ResponseEntity.ok().body(controlHelper.getAllUGVs());
    }
    
    @PostMapping("/action-complete")
    public ResponseEntity<Object> handleActionComplete(@RequestBody Map<String, Object> payload) {

        // get navigation id from payload
        String navigationId = (String) payload.get("jobId");

        // get curresponding requests entity from database
        List<Requests> requests = requestRepository.findByCurrentJobId(navigationId);
        Requests req = requests.get(0);

        req.setSchedulerFlag(true);

        requestRepository.save(req);

        return ResponseEntity.ok().body("OK");
    }
}