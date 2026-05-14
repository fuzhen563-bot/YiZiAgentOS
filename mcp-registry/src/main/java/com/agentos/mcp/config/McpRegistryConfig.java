package com.agentos.mcp.config;

import com.agentos.mcp.connector.*;
import com.agentos.mcp.registry.ToolDefinition;
import com.agentos.mcp.registry.ToolRegistry;
import com.agentos.mcp.skill.SkillEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpRegistryConfig {

    @Bean
    public ToolRegistry toolRegistry() {
        ToolRegistry registry = new ToolRegistry();
        for (ToolDefinition t : GitHubConnector.getTools()) registry.registerTool(t);
        for (ToolDefinition t : EmailConnector.getTools()) registry.registerTool(t);
        for (ToolDefinition t : CalendarConnector.getTools()) registry.registerTool(t);
        for (ToolDefinition t : CrmConnector.getTools()) registry.registerTool(t);
        for (ToolDefinition t : ErpConnector.getTools()) registry.registerTool(t);
        return registry;
    }

    @Bean
    public SkillEngine skillEngine() {
        return new SkillEngine();
    }
}