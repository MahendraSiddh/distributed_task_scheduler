package com.orchestrator.service;

import com.orchestrator.dto.TaskAssignmentMessage;
import com.orchestrator.dto.TaskCompletionMessage;
import com.orchestrator.dto.WebSocketMessage;
import com.orchestrator.entity.Task;
import com.orchestrator.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Notify specific employee about new task assignment
     */
    public void notifyEmployeeTaskAssigned(Task task, User employee) {
        TaskAssignmentMessage assignmentMsg = TaskAssignmentMessage.fromTask(task);
        assignmentMsg.setMessage("New task assigned to you!");
        
        WebSocketMessage wsMessage = new WebSocketMessage(
            "TASK_ASSIGNED",
            assignmentMsg,
            employee.getId(),
            getCurrentTimestamp()
        );
        
        // Send to specific user's queue
        messagingTemplate.convertAndSendToUser(
            employee.getId().toString(),
            "/queue/tasks",
            wsMessage
        );
        
        log.info("WebSocket notification sent to employee {} for task {}", 
            employee.getUsername(), task.getTaskId());
    }
    
    /**
     * Broadcast task assignment to all admins
     */
    public void broadcastTaskAssignment(Task task) {
        TaskAssignmentMessage assignmentMsg = TaskAssignmentMessage.fromTask(task);
        
        WebSocketMessage wsMessage = new WebSocketMessage(
            "TASK_ASSIGNED_BROADCAST",
            assignmentMsg,
            null,
            getCurrentTimestamp()
        );
        
        // Broadcast to all admins
        messagingTemplate.convertAndSend("/topic/admin/tasks", wsMessage);
        
        log.info("WebSocket broadcast sent for task assignment: {}", task.getTaskId());
    }
    
    /**
     * Notify about task completion
     */
    public void notifyTaskCompleted(Task task, User employee, String comment) {
        TaskCompletionMessage completionMsg = new TaskCompletionMessage(
            task.getTaskId(),
            task.getName(),
            "COMPLETED",
            comment,
            employee.getId(),
            employee.getFullName()
        );
        
        WebSocketMessage wsMessage = new WebSocketMessage(
            "TASK_COMPLETED",
            completionMsg,
            null,
            getCurrentTimestamp()
        );
        
        // Broadcast to all admins
        messagingTemplate.convertAndSend("/topic/admin/tasks", wsMessage);
        
        // Also send to the employee
        messagingTemplate.convertAndSendToUser(
            employee.getId().toString(),
            "/queue/tasks",
            wsMessage
        );
        
        log.info("Task completed notification sent: {}", task.getTaskId());
    }
    
    /**
     * Notify about task failure
     */
    public void notifyTaskFailed(Task task, User employee, String comment) {
        TaskCompletionMessage completionMsg = new TaskCompletionMessage(
            task.getTaskId(),
            task.getName(),
            "FAILED",
            comment,
            employee.getId(),
            employee.getFullName()
        );
        
        WebSocketMessage wsMessage = new WebSocketMessage(
            "TASK_FAILED",
            completionMsg,
            null,
            getCurrentTimestamp()
        );
        
        // Broadcast to all admins
        messagingTemplate.convertAndSend("/topic/admin/tasks", wsMessage);
        
        // Also send to the employee
        messagingTemplate.convertAndSendToUser(
            employee.getId().toString(),
            "/queue/tasks",
            wsMessage
        );
        
        log.warn("Task failed notification sent: {}", task.getTaskId());
    }
    
    /**
     * Broadcast task creation
     */
    public void broadcastTaskCreated(Task task) {
        WebSocketMessage wsMessage = new WebSocketMessage(
            "TASK_CREATED",
            task,
            null,
            getCurrentTimestamp()
        );
        
        messagingTemplate.convertAndSend("/topic/admin/tasks", wsMessage);
        log.info("Task created broadcast sent: {}", task.getTaskId());
    }
    
    /**
     * Broadcast task progress update
     */
    public void broadcastTaskProgress(Task task) {
        WebSocketMessage wsMessage = new WebSocketMessage(
            "TASK_PROGRESS",
            task,
            null,
            getCurrentTimestamp()
        );
        
        messagingTemplate.convertAndSend("/topic/admin/tasks", wsMessage);
    }
    
    /**
     * Send statistics update
     */
    public void broadcastStatisticsUpdate(Object stats) {
        WebSocketMessage wsMessage = new WebSocketMessage(
            "STATISTICS_UPDATE",
            stats,
            null,
            getCurrentTimestamp()
        );
        
        messagingTemplate.convertAndSend("/topic/admin/statistics", wsMessage);
    }
    
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}