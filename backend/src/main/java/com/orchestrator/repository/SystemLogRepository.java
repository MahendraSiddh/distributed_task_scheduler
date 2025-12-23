package com.orchestrator.repository;

import com.orchestrator.entity.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    List<SystemLog> findTop50ByOrderByTimestampDesc();
}