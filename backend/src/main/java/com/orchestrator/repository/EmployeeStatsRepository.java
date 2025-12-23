package com.orchestrator.repository;

import com.orchestrator.entity.EmployeeStats;
import com.orchestrator.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface EmployeeStatsRepository extends JpaRepository<EmployeeStats, Long> {
    Optional<EmployeeStats> findByEmployee(User employee);
    
    @Query("SELECT es FROM EmployeeStats es WHERE es.currentActiveTask = 0 ORDER BY es.priorityScore ASC, es.totalTasksAssigned ASC")
    List<EmployeeStats> findIdleEmployeesForFairDistribution();
    
    @Query("SELECT COUNT(es) FROM EmployeeStats es WHERE es.currentActiveTask = 1")
    Long countBusyEmployees();
}
