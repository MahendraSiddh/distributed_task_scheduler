package com.orchestrator.service;

import com.orchestrator.entity.*;
import com.orchestrator.repository.*;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class TaskAssignmentService {
    
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final EmployeeStatsService employeeStatsService;
    private final EmployeeTaskAssignmentRepository assignmentRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public TaskAssignmentService(
            TaskRepository taskRepository, 
            UserRepository userRepository, 
            @Lazy EmployeeStatsService employeeStatsService, // Use @Lazy here
            EmployeeTaskAssignmentRepository assignmentRepository, 
            SimpMessagingTemplate messagingTemplate) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.employeeStatsService = employeeStatsService;
        this.assignmentRepository = assignmentRepository;
        this.messagingTemplate = messagingTemplate;
    }
    
    /**
     * Automatically assign tasks from priority queue to idle employees
     * Runs every 5 seconds
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void autoAssignTasks() {
        // Get pending tasks ordered by priority (1=highest) and creation time
        List<Task> pendingTasks = taskRepository.findPendingTasksByPriority();
        
        if (pendingTasks.isEmpty()) {
            return;
        }
        
        // Get idle employees ordered by fair distribution algorithm
        List<EmployeeStats> idleEmployees = employeeStatsService.getIdleEmployees();
        
        if (idleEmployees.isEmpty()) {
            log.debug("No idle employees available for task assignment");
            return;
        }
        
        // Assign tasks to idle employees
        int assignmentCount = 0;
        for (Task task : pendingTasks) {
            if (assignmentCount >= idleEmployees.size()) {
                break; // No more idle employees
            }
            
            EmployeeStats employeeStats = idleEmployees.get(assignmentCount);
            User employee = employeeStats.getEmployee();
            
            // Assign task
            assignTaskToEmployee(task, employee);
            assignmentCount++;
            
            log.info("Auto-assigned task {} (priority: {}) to employee {}", 
                task.getTaskId(), task.getPriority(), employee.getUsername());
        }
        
        if (assignmentCount > 0) {
            sendWebSocketUpdate("tasks.assigned", assignmentCount);
        }
    }
    
    /**
     * Manually assign a specific task to a specific employee (Admin action)
     */
    @Transactional
    public void manualAssignTask(String taskId, Long employeeId) {
        Task task = taskRepository.findByTaskId(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));
        
        if (task.getStatus() != TaskStatus.PENDING) {
            throw new RuntimeException("Only pending tasks can be assigned");
        }
        
        User employee = userRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        if (employee.getRole() != UserRole.EMPLOYEE) {
            throw new RuntimeException("Can only assign tasks to employees");
        }
        
        // Check if employee already has an active task
        EmployeeStats stats = employeeStatsService.getEmployeeStats(employee);
        if (stats.getCurrentActiveTask() > 0) {
            throw new RuntimeException("Employee is already working on a task");
        }
        
        assignTaskToEmployee(task, employee);
        log.info("Admin manually assigned task {} to employee {}", taskId, employee.getUsername());
    }
    
    /**
     * Core task assignment logic
     */
    @Transactional
    public void assignTaskToEmployee(Task task, User employee) {
        // Update task
        task.setAssignedTo(employee);
        task.setStatus(TaskStatus.RUNNING);
        task.setStartTime(LocalDateTime.now());
        task.setWorkerId("employee-" + employee.getId());
        taskRepository.save(task);
        
        // Mark employee as busy
        employeeStatsService.markEmployeeBusy(employee);
        
        // Create assignment record
        EmployeeTaskAssignment assignment = new EmployeeTaskAssignment();
        assignment.setEmployee(employee);
        assignment.setTask(task);
        assignment.setStartedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);
        
        // Send WebSocket notification to employee
        sendWebSocketUpdate("task.assigned." + employee.getId(), task);
        sendWebSocketUpdate("task.updated", task);
        
        log.info("Task {} assigned to employee {}", task.getTaskId(), employee.getUsername());
    }
    
    /**
     * Get next available task for an employee (when they're ready for more)
     */
    @Transactional
    public Task getNextTaskForEmployee(User employee) {
        // Check if employee already has an active task
        EmployeeStats stats = employeeStatsService.getEmployeeStats(employee);
        if (stats.getCurrentActiveTask() > 0) {
            // Return current active task
            return taskRepository.findActiveTaskByEmployee(employee).orElse(null);
        }
        
        // Get highest priority pending task
        List<Task> pendingTasks = taskRepository.findPendingTasksByPriority();
        if (!pendingTasks.isEmpty()) {
            Task task = pendingTasks.get(0);
            assignTaskToEmployee(task, employee);
            return task;
        }
        
        return null;
    }
    
    /**
     * Find best employee for a task using fair distribution algorithm
     */
    public User findBestEmployeeForTask(Task task) {
        List<EmployeeStats> idleEmployees = employeeStatsService.getIdleEmployees();
        
        if (idleEmployees.isEmpty()) {
            return null;
        }
        
        // For high priority tasks (1-2), prefer experienced employees
        if (task.getPriority() <= 2) {
            return idleEmployees.stream()
                .filter(stats -> stats.getTotalTasksCompleted() > 0)
                .findFirst()
                .map(EmployeeStats::getEmployee)
                .orElse(idleEmployees.get(0).getEmployee());
        }
        
        // For normal/low priority, use fair distribution (lowest priority score)
        return idleEmployees.get(0).getEmployee();
    }
    
    /**
     * Get all tasks assigned to an employee
     */
    public List<Task> getEmployeeTasks(User employee) {
        return taskRepository.findByAssignedTo(employee);
    }
    
    /**
     * Get current active task for an employee
     */
    public Task getCurrentTaskForEmployee(User employee) {
        return taskRepository.findActiveTaskByEmployee(employee).orElse(null);
    }
    
    private void sendWebSocketUpdate(String destination, Object payload) {
        messagingTemplate.convertAndSend("/topic/" + destination, payload);
    }
}