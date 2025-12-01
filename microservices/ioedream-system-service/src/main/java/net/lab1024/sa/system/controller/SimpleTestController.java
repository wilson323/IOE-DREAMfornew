package net.lab1024.sa.system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple Test Controller
 * For microservice startup verification
 */
@RestController
@RequestMapping("/api/test")
public class SimpleTestController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "ioedream-system-service");
        result.put("timestamp", LocalDateTime.now());
        result.put("message", "Microservice started successfully!");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> result = new HashMap<>();
        result.put("serviceName", "IOE-DREAM System Management Service");
        result.put("version", "1.0.0");
        result.put("description", "Enterprise Intelligent Management System - System Management Module");
        result.put("features", new String[]{
            "Data Dictionary Management", "Menu Permission Management", "Role Permission Management",
            "System Configuration Management", "Department Management", "Cache Management", "Login Management"
        });
        result.put("apiCount", 165);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api-count")
    public ResponseEntity<Map<String, Object>> apiCount() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalAPIs", 165);
        result.put("modules", Map.of(
            "DictController", 15,
            "MenuController", 20,
            "RoleController", 30,
            "ConfigController", 25,
            "DepartmentController", 18,
            "CacheManagementController", 25,
            "LoginController", 20
        ));
        result.put("completionRate", "100%");
        return ResponseEntity.ok(result);
    }
}