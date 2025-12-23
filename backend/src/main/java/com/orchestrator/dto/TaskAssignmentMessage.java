package com.orchestrator.dto;

import com.orchestrator.entity.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskAssignmentMessage {
    private String taskId;
    private String taskName;
    private String description;
    private Integer priority;
    private Long employeeId;
    private String employeeName;
    private String message;
    
    public static TaskAssignmentMessage fromTask(Task task) {
        TaskAssignmentMessage msg = new TaskAssignmentMessage();
        msg.setTaskId(task.getTaskId());
        msg.setTaskName(task.getName());
        msg.setDescription(task.getDescription());
        msg.setPriority(task.getPriority());
        if (task.getAssignedTo() != null) {
            msg.setEmployeeId(task.getAssignedTo().getId());
            msg.setEmployeeName(task.getAssignedTo().getFullName());
        }
        return msg;
    }
}
