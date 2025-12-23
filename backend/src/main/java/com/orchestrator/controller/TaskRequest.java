package com.orchestrator.controller;

import lombok.Data;

@Data
public class TaskRequest {
    private String name;
    private String payload;
}