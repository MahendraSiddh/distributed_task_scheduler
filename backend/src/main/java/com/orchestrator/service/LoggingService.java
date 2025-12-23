package com.orchestrator.service;

import com.orchestrator.entity.LogType;
import com.orchestrator.entity.SystemLog;
import com.orchestrator.repository.SystemLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoggingService {
    
    private final SystemLogRepository logRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    public void log(String message, LogType type, String taskId, String workerId) {
        SystemLog log = new SystemLog();
        log.setMessage(message);
        log.setType(type);
        log.setTaskId(taskId);
        log.setWorkerId(workerId);
        
        logRepository.save(log);
        messagingTemplate.convertAndSend("/topic/logs", log);
    }
    
    public void log(String message, LogType type) {
        log(message, type, null, null);
    }
    
    public List<SystemLog> getRecentLogs() {
        return logRepository.findTop50ByOrderByTimestampDesc();
    }
}
