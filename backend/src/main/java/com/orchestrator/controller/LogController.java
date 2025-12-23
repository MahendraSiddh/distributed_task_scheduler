package com.orchestrator.controller;

import com.orchestrator.entity.SystemLog;
import com.orchestrator.service.LoggingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LogController {
    
    private final LoggingService loggingService;
    
    @GetMapping
    public ResponseEntity<List<SystemLog>> getRecentLogs() {
        return ResponseEntity.ok(loggingService.getRecentLogs());
    }
}
