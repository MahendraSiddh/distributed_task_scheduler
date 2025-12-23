package com.orchestrator.controller;

import com.orchestrator.entity.User;
import com.orchestrator.entity.UserRole;
import com.orchestrator.entity.UserStatus;
import com.orchestrator.repository.UserRepository;
import com.orchestrator.service.EmployeeStatsService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/employees")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmployeeManagementController {
    
    private final UserRepository userRepository;
    private final EmployeeStatsService employeeStatsService;
    
    /**
     * Get all employees
     */
    @GetMapping
    public ResponseEntity<?> getAllEmployees() {
        List<User> employees = userRepository.findByRole(UserRole.EMPLOYEE);
        
        List<Map<String, Object>> employeeData = employees.stream()
            .map(employee -> {
                var stats = employeeStatsService.getEmployeeStats(employee);
                return Map.of(
                    "id", employee.getId(),
                    "username", employee.getUsername(),
                    "fullName", employee.getFullName(),
                    "email", employee.getEmail(),
                    "status", employee.getStatus(),
                    "stats", Map.of(
                        "totalAssigned", stats.getTotalTasksAssigned(),
                        "totalCompleted", stats.getTotalTasksCompleted(),
                        "totalFailed", stats.getTotalTasksFailed(),
                        "isWorking", stats.getCurrentActiveTask() > 0,
                        "averageTimeMinutes", stats.getAverageCompletionTimeMinutes()
                    )
                );
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(employeeData);
    }
    
    /**
     * Get active employees (available for work)
     */
    @GetMapping("/active")
    public ResponseEntity<List<User>> getActiveEmployees() {
        return ResponseEntity.ok(
            userRepository.findByRoleAndStatus(UserRole.EMPLOYEE, UserStatus.ACTIVE)
        );
    }
    
    /**
     * Get employee statistics
     */
    @GetMapping("/{employeeId}/stats")
    public ResponseEntity<?> getEmployeeStats(@PathVariable Long employeeId) {
        try {
            User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
            
            var stats = employeeStatsService.getEmployeeStats(employee);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
