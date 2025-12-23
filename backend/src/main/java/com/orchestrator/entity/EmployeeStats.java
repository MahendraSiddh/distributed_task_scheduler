package com.orchestrator.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_stats")
@Data
@Getter @Setter @NoArgsConstructor
public class EmployeeStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", unique = true)
    private User employee;

    public User getEmployee(){
        return employee;
    }
    
    private Integer totalTasksAssigned;
    
    private Integer totalTasksCompleted;
    
    private Integer totalTasksFailed;
    
    private Integer currentActiveTask; // 0 = idle, 1 = working
    
    private Double averageCompletionTimeMinutes;
    
    private Integer priorityScore; // For fair distribution
    
    private LocalDateTime lastTaskAssignedAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        totalTasksAssigned = 0;
        totalTasksCompleted = 0;
        totalTasksFailed = 0;
        currentActiveTask = 0;
        averageCompletionTimeMinutes = 0.0;
        priorityScore = 0;
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
