package com.orchestrator.repository;

import com.orchestrator.entity.Task;
import com.orchestrator.entity.TaskStatus;
import com.orchestrator.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByTaskId(String taskId);
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByAssignedTo(User employee);
    List<Task> findByCreatedBy(User admin);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status")
    Long countByStatus(@Param("status") TaskStatus status);
    
    @Query("SELECT t FROM Task t WHERE t.status = 'PENDING' ORDER BY t.priority ASC, t.createdAt ASC")
    List<Task> findPendingTasksByPriority();
    
    @Query("SELECT t FROM Task t WHERE t.assignedTo = :employee AND t.status IN ('PENDING', 'RUNNING')")
    Optional<Task> findActiveTaskByEmployee(@Param("employee") User employee);
    
    List<Task> findTop10ByOrderByCreatedAtDesc();
    
    @Query("SELECT t FROM Task t WHERE t.createdBy = :admin ORDER BY t.createdAt DESC")
    List<Task> findTasksByAdmin(@Param("admin") User admin);
}
