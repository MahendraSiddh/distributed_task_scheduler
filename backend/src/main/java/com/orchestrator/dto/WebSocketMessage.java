package com.orchestrator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketMessage {
    private String type;
    private Object payload;
    private Long userId; // Target user ID (for user-specific messages)
    private String timestamp;
}

