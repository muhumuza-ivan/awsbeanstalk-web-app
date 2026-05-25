package com.demo.controller;

import com.demo.service.DynamoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
public class AppController {

    private final DynamoService dynamoService;

    @Value("${APP_VERSION:1.0.0}")
    private String appVersion;

    public AppController(DynamoService dynamoService) {
        this.dynamoService = dynamoService;
    }

    /** Health / root endpoint — proves the app is running */
    @GetMapping("/")
    public ResponseEntity<Map<String, String>> home() {
        return ResponseEntity.ok(Map.of(
                "status",    "OK",
                "message",   "Spring Boot app Version 2 - deployed automatically via GitHub Actions",
                "version",   appVersion,
                "timestamp", Instant.now().toString()
        ));
    }

    /** Demonstrates DynamoDB integration (optional challenge) */
    @PostMapping("/visits")
    public ResponseEntity<Map<String, String>> recordVisit(
            @RequestParam(defaultValue = "anonymous") String user) {

        String id = dynamoService.recordVisit(user);
        return ResponseEntity.ok(Map.of(
                "status",  "recorded",
                "visitId", id
        ));
    }

    /** Retrieve recent visits */
    @GetMapping("/visits")
    public ResponseEntity<?> getVisits() {
        return ResponseEntity.ok(Map.of(
                "visits", dynamoService.listVisits()
        ));
    }
}