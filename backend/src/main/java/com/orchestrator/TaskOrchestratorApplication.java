package com.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling 
public class TaskOrchestratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskOrchestratorApplication.class, args);
    }
}