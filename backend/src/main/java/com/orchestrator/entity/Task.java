package com.orchestrator.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
@Getter @Setter @NoArgsConstructor

public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String taskId;
    
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description; // NEW: Task description from admin
    
    @Enumerated(EnumType.STRING)
    private TaskStatus status;
    
    private Integer priority; // NEW: Priority 1-5 (1=highest, 5=lowest)
    
    private Integer progress;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy; // NEW: Admin who created the task
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo; // NEW: Employee assigned to this task
    
    private String workerId; // For backward compatibility with workers
    
    private String lockId;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer retryCount;
    
    private String errorMessage;
    
    @Column(columnDefinition = "TEXT")
    private String completionMessage; // NEW: Message from employee on completion/failure
    
    @Column(columnDefinition = "TEXT")
    private String payload;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        progress = 0;
        retryCount = 0;
        if (priority == null) {
            priority = 3; // Default medium priority
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
