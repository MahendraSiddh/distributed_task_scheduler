package com.orchestrator.statemachine;

public enum TaskEvent {
    START,
    PROGRESS,
    COMPLETE,
    FAIL,
    RETRY
}