package com.valetparking.valetparking.controllers;

import com.valetparking.valetparking.entities.UGV;
import com.valetparking.valetparking.services.UGVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/ugv")
public class UGVController {

    @Autowired
    private UGVService ugvService;

    @PostMapping
    public ResponseEntity<UGV> createUGV(@RequestBody UGV ugv) {
        UGV createdUGV = ugvService.saveUGV(ugv);
        return ResponseEntity.ok(createdUGV);
    }

    @GetMapping
    public ResponseEntity<List<UGV>> getAllUGVs() {
        List<UGV> ugvs = ugvService.getAllUGVs();
        return ResponseEntity.ok(ugvs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UGV> getUGVById(@PathVariable String id) {
        UGV ugv = ugvService.getUGVById(id);
        return ResponseEntity.ok(ugv);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UGV> updateUGV(@PathVariable String id, @RequestBody UGV ugvDetails) {
        UGV updatedUGV = ugvService.updateUGV(id, ugvDetails);
        return ResponseEntity.ok(updatedUGV);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUGV(@PathVariable String id) {
        ugvService.deleteUGV(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/registerSession")
    public ResponseEntity<UGV> setSessionId(@PathVariable String id, @RequestBody String sessionId) {
        return ResponseEntity.ok(ugvService.setUGVSessionID(id, sessionId));
    }
}
