package com.orchestrator.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_task_assignments")
@Data
@Getter @Setter @NoArgsConstructor

public class EmployeeTaskAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
    
    private LocalDateTime assignedAt;
    
    private LocalDateTime startedAt;
    
    private LocalDateTime completedAt;
    
    private Integer timeSpentMinutes;
    
    @Enumerated(EnumType.STRING)
    private TaskStatus finalStatus; // COMPLETED or FAILED
    
    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
    }
}