package com.orchestrator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskCompletionMessage {
    private String taskId;
    private String taskName;
    private String status; // COMPLETED or FAILED
    private String comment;
    private Long employeeId;
    private String employeeName;
}
