package com.valetparking.valetparking.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ping")
public class BaseController {
    @GetMapping
    public ResponseEntity<?> getStatus() {
        return ResponseEntity.ok("Server is up!!!!!!!");
    }
}
