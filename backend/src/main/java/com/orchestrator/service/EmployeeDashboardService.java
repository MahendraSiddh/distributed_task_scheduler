package com.orchestrator.service;

import com.orchestrator.entity.EmployeeStats;
import com.orchestrator.entity.Task;
import com.orchestrator.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmployeeDashboardService {
    
    private final TaskAssignmentService assignmentService;
    private final TaskService taskService;
    private final EmployeeStatsService employeeStatsService;
    
    /**
     * Get complete dashboard data for employee
     */
    public Map<String, Object> getEmployeeDashboard(User employee) {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Current active task
        Task currentTask = assignmentService.getCurrentTaskForEmployee(employee);
        dashboard.put("currentTask", currentTask);
        
        // Employee statistics
        EmployeeStats stats = employeeStatsService.getEmployeeStats(employee);
        dashboard.put("stats", Map.of(
            "totalAssigned", stats.getTotalTasksAssigned(),
            "totalCompleted", stats.getTotalTasksCompleted(),
            "totalFailed", stats.getTotalTasksFailed(),
            "isWorking", stats.getCurrentActiveTask() > 0,
            "averageTimeMinutes", stats.getAverageCompletionTimeMinutes()
        ));
        
        // Recent task history
        dashboard.put("recentTasks", taskService.getTasksByEmployee(employee));
        
        return dashboard;
    }
}