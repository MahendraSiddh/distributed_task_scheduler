package com.orchestrator.statemachine;

import com.orchestrator.entity.TaskStatus;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory
public class TaskStateMachineConfig extends StateMachineConfigurerAdapter<TaskStatus, TaskEvent> {
    
    @Override
    public void configure(StateMachineStateConfigurer<TaskStatus, TaskEvent> states) throws Exception {
        states
            .withStates()
            .initial(TaskStatus.PENDING)
            .states(EnumSet.allOf(TaskStatus.class));
    }
    
    @Override
    public void configure(StateMachineTransitionConfigurer<TaskStatus, TaskEvent> transitions) throws Exception {
        transitions
            .withExternal()
                .source(TaskStatus.PENDING).target(TaskStatus.RUNNING)
                .event(TaskEvent.START)
                .and()
            .withExternal()
                .source(TaskStatus.RUNNING).target(TaskStatus.RUNNING)
                .event(TaskEvent.PROGRESS)
                .and()
            .withExternal()
                .source(TaskStatus.RUNNING).target(TaskStatus.COMPLETED)
                .event(TaskEvent.COMPLETE)
                .and()
            .withExternal()
                .source(TaskStatus.RUNNING).target(TaskStatus.FAILED)
                .event(TaskEvent.FAIL)
                .and()
            .withExternal()
                .source(TaskStatus.FAILED).target(TaskStatus.RETRYING)
                .event(TaskEvent.RETRY)
                .and()
            .withExternal()
                .source(TaskStatus.RETRYING).target(TaskStatus.PENDING)
                .event(TaskEvent.START);
    }
}