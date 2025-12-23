package com.orchestrator.service;

import com.orchestrator.entity.*;
import com.orchestrator.repository.*;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final EmployeeTaskAssignmentRepository assignmentRepository;
    private final EmployeeStatsService employeeStatsService;
    private final SimpMessagingTemplate messagingTemplate;

    public TaskService(
                TaskRepository taskRepository, 
                EmployeeTaskAssignmentRepository assignmentRepository, 
                @Lazy EmployeeStatsService employeeStatsService, // BREAKS THE CIRCULAR LOOP
                SimpMessagingTemplate messagingTemplate) {
            this.taskRepository = taskRepository;
            this.assignmentRepository = assignmentRepository;
            this.employeeStatsService = employeeStatsService;
            this.messagingTemplate = messagingTemplate;
        }
    
    /**
     * Admin creates a new task with description and priority
     */
    @Transactional
    public Task createTask(User admin, String name, String description, Integer priority) {
        if (admin.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only admins can create tasks");
        }
        
        if (priority < 1 || priority > 5) {
            throw new RuntimeException("Priority must be between 1 (highest) and 5 (lowest)");
        }
        
        Task task = new Task();
        task.setTaskId(UUID.randomUUID().toString());
        task.setName(name);
        task.setDescription(description);
        task.setPriority(priority);
        task.setStatus(TaskStatus.PENDING);
        task.setCreatedBy(admin);
        
        task = taskRepository.save(task);
        
        log.info("Admin {} created task {} with priority {}", admin.getUsername(), task.getTaskId(), priority);
        sendWebSocketUpdate("task.created", task);
        
        return task;
    }
    
    /**
     * Employee marks task as completed with a message
     */
    @Transactional
    public Task completeTask(String taskId, User employee, String completionMessage) {
        Task task = taskRepository.findByTaskId(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        
        validateEmployeeTaskAccess(task, employee);
        
        if (task.getStatus() != TaskStatus.RUNNING) {
            throw new RuntimeException("Only running tasks can be completed");
        }
        
        // Update task
        task.setStatus(TaskStatus.COMPLETED);
        task.setProgress(100);
        task.setEndTime(LocalDateTime.now());
        task.setCompletionMessage(completionMessage);
        
        task = taskRepository.save(task);
        
        // Update assignment record
        updateAssignmentRecord(task, employee, TaskStatus.COMPLETED);
        
        // Update employee stats
        int timeSpent = calculateTimeSpent(task.getStartTime(), task.getEndTime());
        employeeStatsService.recordTaskCompletion(employee, true, timeSpent);
        employeeStatsService.markEmployeeIdle(employee);
        
        log.info("Employee {} completed task {}", employee.getUsername(), taskId);
        sendWebSocketUpdate("task.completed", task);
        sendWebSocketUpdate("employee.task.completed." + employee.getId(), task);
        
        return task;
    }
    
    /**
     * Employee marks task as failed with error message
     */
    @Transactional
    public Task failTask(String taskId, User employee, String errorMessage) {
        Task task = taskRepository.findByTaskId(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        
        validateEmployeeTaskAccess(task, employee);
        
        if (task.getStatus() != TaskStatus.RUNNING) {
            throw new RuntimeException("Only running tasks can be marked as failed");
        }
        
        // Update task
        task.setStatus(TaskStatus.FAILED);
        task.setEndTime(LocalDateTime.now());
        task.setErrorMessage(errorMessage);
        task.setRetryCount(task.getRetryCount() + 1);
        
        task = taskRepository.save(task);
        
        // Update assignment record
        updateAssignmentRecord(task, employee, TaskStatus.FAILED);
        
        // Update employee stats
        int timeSpent = calculateTimeSpent(task.getStartTime(), task.getEndTime());
        employeeStatsService.recordTaskCompletion(employee, false, timeSpent);
        employeeStatsService.markEmployeeIdle(employee);
        
        log.warn("Employee {} marked task {} as failed: {}", employee.getUsername(), taskId, errorMessage);
        sendWebSocketUpdate("task.failed", task);
        sendWebSocketUpdate("employee.task.failed." + employee.getId(), task);
        
        return task;
    }
    
    /**
     * Update task progress (called by employee)
     */
    @Transactional
    public void updateProgress(String taskId, User employee, int progress) {
        Task task = taskRepository.findByTaskId(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        
        validateEmployeeTaskAccess(task, employee);
        
        task.setProgress(Math.min(progress, 100));
        taskRepository.save(task);
        sendWebSocketUpdate("task.progress", task);
    }
    
    /**
     * Get all tasks (Admin view)
     */
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
    
    /**
     * Get tasks created by specific admin
     */
    public List<Task> getTasksByAdmin(User admin) {
        return taskRepository.findTasksByAdmin(admin);
    }
    
    /**
     * Get tasks assigned to specific employee
     */
    public List<Task> getTasksByEmployee(User employee) {
        return taskRepository.findByAssignedTo(employee);
    }
    
    /**
     * Get task statistics
     */
    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", taskRepository.countByStatus(TaskStatus.PENDING));
        stats.put("running", taskRepository.countByStatus(TaskStatus.RUNNING));
        stats.put("completed", taskRepository.countByStatus(TaskStatus.COMPLETED));
        stats.put("failed", taskRepository.countByStatus(TaskStatus.FAILED));
        return stats;
    }
    
    /**
     * Get employee's assignment history
     */
    public List<EmployeeTaskAssignment> getEmployeeHistory(User employee) {
        return assignmentRepository.findRecentAssignmentsByEmployee(employee);
    }
    
    private void validateEmployeeTaskAccess(Task task, User employee) {
        if (!task.getAssignedTo().getId().equals(employee.getId())) {
            throw new RuntimeException("This task is not assigned to you");
        }
    }
    
    private void updateAssignmentRecord(Task task, User employee, TaskStatus finalStatus) {
        List<EmployeeTaskAssignment> assignments = assignmentRepository.findByEmployee(employee);
        assignments.stream()
            .filter(a -> a.getTask().getId().equals(task.getId()) && a.getCompletedAt() == null)
            .findFirst()
            .ifPresent(assignment -> {
                assignment.setCompletedAt(LocalDateTime.now());
                assignment.setFinalStatus(finalStatus);
                assignment.setTimeSpentMinutes(calculateTimeSpent(assignment.getStartedAt(), assignment.getCompletedAt()));
                assignmentRepository.save(assignment);
            });
    }
    
    private int calculateTimeSpent(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return (int) ChronoUnit.MINUTES.between(start, end);
    }
    
    private void sendWebSocketUpdate(String destination, Object payload) {
    if (payload instanceof Task t) {
            // Send a simple map instead of the proxy-heavy Task entity
            Map<String, Object> map = new HashMap<>();
            map.put("taskId", t.getTaskId());
            map.put("status", t.getStatus());
            map.put("name", t.getName());
            messagingTemplate.convertAndSend("/topic/" + destination, map);
        } else {
            messagingTemplate.convertAndSend("/topic/" + destination, payload);
        }
    }
}
