package com.agentos.goal.config;

import com.agentos.goal.core.GoalRegistry;
import com.agentos.goal.core.ObjectiveEngine;
import com.agentos.goal.core.ReplanningEngine;
import com.agentos.goal.persist.CheckpointManager;
import com.agentos.goal.task.TaskGraph;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoalEngineConfig {
    @Bean public GoalRegistry goalRegistry() { return new GoalRegistry(); }
    @Bean public ObjectiveEngine objectiveEngine() { return new ObjectiveEngine(); }
    @Bean public TaskGraph taskGraph() { return new TaskGraph(); }
    @Bean public CheckpointManager checkpointManager() { return new CheckpointManager(); }
    @Bean public ReplanningEngine replanningEngine() { return new ReplanningEngine(); }
}