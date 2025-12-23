package com.orchestrator.controller;

import com.orchestrator.entity.Task;
import com.orchestrator.entity.User;
import com.orchestrator.service.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/employee/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmployeeTaskController {
    
    private final TaskService taskService;
    private final TaskAssignmentService assignmentService;
    private final EmployeeDashboardService dashboardService;
    private final AuthService authService;
    
    /**
     * Get employee dashboard data
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(@RequestHeader("User-Id") Long userId) {
        try {
            User employee = authService.getUserById(userId);
            Map<String, Object> dashboard = dashboardService.getEmployeeDashboard(employee);
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get current assigned task
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentTask(@RequestHeader("User-Id") Long userId) {
        try {
            User employee = authService.getUserById(userId);
            Task task = assignmentService.getCurrentTaskForEmployee(employee);
            
            if (task == null) {
                return ResponseEntity.ok(Map.of("message", "No active task"));
            }
            
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get next available task (when employee is ready)
     */
    @PostMapping("/get-next")
    public ResponseEntity<?> getNextTask(@RequestHeader("User-Id") Long userId) {
        try {
            User employee = authService.getUserById(userId);
            Task task = assignmentService.getNextTaskForEmployee(employee);
            
            if (task == null) {
                return ResponseEntity.ok(Map.of("message", "No tasks available"));
            }
            
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Mark task as completed
     */
    @PostMapping("/{taskId}/complete")
    public ResponseEntity<?> completeTask(
            @RequestHeader("User-Id") Long userId,
            @PathVariable String taskId,
            @RequestBody TaskCompletionRequest request) {
        try {
            User employee = authService.getUserById(userId);
            Task task = taskService.completeTask(taskId, employee, request.getMessage());
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Mark task as failed
     */
    @PostMapping("/{taskId}/fail")
    public ResponseEntity<?> failTask(
            @RequestHeader("User-Id") Long userId,
            @PathVariable String taskId,
            @RequestBody TaskCompletionRequest request) {
        try {
            User employee = authService.getUserById(userId);
            Task task = taskService.failTask(taskId, employee, request.getMessage());
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Update task progress
     */
    @PostMapping("/{taskId}/progress")
    public ResponseEntity<?> updateProgress(
            @RequestHeader("User-Id") Long userId,
            @PathVariable String taskId,
            @RequestBody ProgressUpdateRequest request) {
        try {
            User employee = authService.getUserById(userId);
            taskService.updateProgress(taskId, employee, request.getProgress());
            return ResponseEntity.ok(Map.of("message", "Progress updated"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get task history
     */
    @GetMapping("/history")
    public ResponseEntity<?> getTaskHistory(@RequestHeader("User-Id") Long userId) {
        try {
            User employee = authService.getUserById(userId);
            return ResponseEntity.ok(taskService.getEmployeeHistory(employee));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
}

@Data
class TaskCompletionRequest {
    private String message;
}

@Data
class ProgressUpdateRequest {
    private Integer progress;
}