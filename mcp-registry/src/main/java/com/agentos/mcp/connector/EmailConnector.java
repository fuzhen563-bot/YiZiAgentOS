package com.agentos.mcp.connector;

import com.agentos.mcp.registry.ToolDefinition;
import java.util.*;

public class EmailConnector {
    public static List<ToolDefinition> getTools() {
        return List.of(
            tool("email_send", "Send an email", "communication", "high"),
            tool("email_read", "Read emails from inbox", "communication", "high"),
            tool("email_search", "Search emails by query", "communication", "medium"),
            tool("email_drafts", "List email drafts", "communication", "medium")
        );
    }

    private static ToolDefinition tool(String name, String desc, String category, String risk) {
        ToolDefinition def = new ToolDefinition();
        def.setName(name);
        def.setDescription(desc);
        def.setProvider("email");
        def.setCost(2);
        def.setRiskLevel(risk);
        return def;
    }
}