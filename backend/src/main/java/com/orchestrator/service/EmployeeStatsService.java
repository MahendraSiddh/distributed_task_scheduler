package com.orchestrator.service;

import com.orchestrator.entity.EmployeeStats;
import com.orchestrator.entity.User;
import com.orchestrator.repository.EmployeeStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Lazy
public class EmployeeStatsService {
    
    private final EmployeeStatsRepository statsRepository;
    
    @Transactional
    public EmployeeStats initializeEmployeeStats(User employee) {
        EmployeeStats stats = new EmployeeStats();
        stats.setEmployee(employee);
        return statsRepository.save(stats);
    }
    
    @Transactional
    public EmployeeStats getOrCreateStats(User employee) {
        return statsRepository.findByEmployee(employee)
            .orElseGet(() -> initializeEmployeeStats(employee));
    }
    
    @Transactional
    public void markEmployeeBusy(User employee) {
        EmployeeStats stats = getOrCreateStats(employee);
        stats.setCurrentActiveTask(1);
        stats.setTotalTasksAssigned(stats.getTotalTasksAssigned() + 1);
        stats.setLastTaskAssignedAt(LocalDateTime.now());
        statsRepository.save(stats);
        log.info("Employee {} marked as busy", employee.getUsername());
    }
    
    @Transactional
    public void markEmployeeIdle(User employee) {
        EmployeeStats stats = getOrCreateStats(employee);
        stats.setCurrentActiveTask(0);
        statsRepository.save(stats);
        log.info("Employee {} marked as idle", employee.getUsername());
    }
    
    @Transactional
    public void recordTaskCompletion(User employee, boolean success, int timeSpentMinutes) {
        EmployeeStats stats = getOrCreateStats(employee);
        
        if (success) {
            stats.setTotalTasksCompleted(stats.getTotalTasksCompleted() + 1);
        } else {
            stats.setTotalTasksFailed(stats.getTotalTasksFailed() + 1);
        }
        
        // Update average completion time
        Double currentAvg = stats.getAverageCompletionTimeMinutes();
        Integer totalCompleted = stats.getTotalTasksCompleted();
        if (totalCompleted > 0) {
            stats.setAverageCompletionTimeMinutes(
                ((currentAvg * (totalCompleted - 1)) + timeSpentMinutes) / totalCompleted
            );
        }
        
        // Calculate priority score for fair distribution
        // Lower score = higher priority to get next task
        int priorityScore = stats.getTotalTasksAssigned() - stats.getTotalTasksCompleted();
        stats.setPriorityScore(priorityScore);
        
        statsRepository.save(stats);
    }
    
    public List<EmployeeStats> getIdleEmployees() {
        return statsRepository.findIdleEmployeesForFairDistribution();
    }
    
    public Long countBusyEmployees() {
        return statsRepository.countBusyEmployees();
    }
    
    public EmployeeStats getEmployeeStats(User employee) {
        return getOrCreateStats(employee);
    }
}