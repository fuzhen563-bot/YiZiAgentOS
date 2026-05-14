package com.agentos.runtime.config;

import com.agentos.runtime.core.AgentRegistry;
import com.agentos.runtime.multiagent.MultiAgentOrchestrator;
import com.agentos.runtime.workflow.WorkflowEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentRuntimeConfig {

    @Bean
    public AgentRegistry agentRegistry() {
        AgentRegistry registry = new AgentRegistry();
        registry.initDefaultAgents();
        return registry;
    }

    @Bean
    public MultiAgentOrchestrator multiAgentOrchestrator(AgentRegistry registry) {
        return new MultiAgentOrchestrator(registry);
    }

    @Bean
    public WorkflowEngine workflowEngine() {
        return new WorkflowEngine();
    }
}