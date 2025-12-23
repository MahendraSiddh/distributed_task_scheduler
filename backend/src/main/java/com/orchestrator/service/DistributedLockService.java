package com.orchestrator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistributedLockService {
    
    private final RedissonClient redissonClient;
    
    public String acquireLock(String taskId) {
        String lockKey = "task:lock:" + taskId;
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            boolean acquired = lock.tryLock(10, 300, TimeUnit.SECONDS);
            if (acquired) {
                log.info("Lock acquired for task: {}", taskId);
                return lockKey;
            }
        } catch (InterruptedException e) {
            log.error("Failed to acquire lock for task: {}", taskId, e);
            Thread.currentThread().interrupt();
        }
        return null;
    }
    
    public void releaseLock(String lockKey) {
        if (lockKey != null) {
            RLock lock = redissonClient.getLock(lockKey);
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("Lock released: {}", lockKey);
            }
        }
    }
    
    public boolean isLocked(String taskId) {
        String lockKey = "task:lock:" + taskId;
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }
}