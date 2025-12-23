package com.orchestrator.repository;

import com.orchestrator.entity.EmployeeTaskAssignment;
import com.orchestrator.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface EmployeeTaskAssignmentRepository extends JpaRepository<EmployeeTaskAssignment, Long> {
    List<EmployeeTaskAssignment> findByEmployee(User employee);
    
    @Query("SELECT eta FROM EmployeeTaskAssignment eta WHERE eta.employee = :employee ORDER BY eta.assignedAt DESC")
    List<EmployeeTaskAssignment> findRecentAssignmentsByEmployee(@Param("employee") User employee);
}
