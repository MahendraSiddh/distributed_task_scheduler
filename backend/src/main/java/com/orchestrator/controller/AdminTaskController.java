package com.orchestrator.controller;

import com.orchestrator.entity.Task;
import com.orchestrator.entity.User;
import com.orchestrator.service.AuthService;
import com.orchestrator.service.TaskService;
import com.orchestrator.service.TaskAssignmentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminTaskController {
    
    private final TaskService taskService;
    private final TaskAssignmentService assignmentService;
    private final AuthService authService;
    
    /**
     * Admin creates a new task
     */
    @PostMapping
    public ResponseEntity<?> createTask(
            @RequestHeader("User-Id") Long userId,
            @RequestBody CreateTaskRequest request) {
        try {
            User admin = authService.getUserById(userId);
            
            Task task = taskService.createTask(
                admin,
                request.getName(),
                request.getDescription(),
                request.getPriority()
            );
            
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get all tasks (Admin view)
     */
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }
    
    /**
     * Get tasks created by this admin
     */
    @GetMapping("/my-tasks")
    public ResponseEntity<?> getMyTasks(@RequestHeader("User-Id") Long userId) {
        try {
            User admin = authService.getUserById(userId);
            return ResponseEntity.ok(taskService.getTasksByAdmin(admin));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get task statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getStatistics() {
        return ResponseEntity.ok(taskService.getStatistics());
    }
    
    /**
     * Manually assign task to employee
     */
    @PostMapping("/{taskId}/assign/{employeeId}")
    public ResponseEntity<?> assignTask(
            @PathVariable String taskId,
            @PathVariable Long employeeId) {
        try {
            assignmentService.manualAssignTask(taskId, employeeId);
            return ResponseEntity.ok(Map.of("message", "Task assigned successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get recent tasks
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Task>> getRecentTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }
}

@Data
class CreateTaskRequest {
    private String name;
    private String description;
    private Integer priority; // 1-5 (1=highest)
}