package com.agentos.mcp.connector;

import com.agentos.mcp.registry.ToolDefinition;
import java.util.*;

public class CalendarConnector {
    public static List<ToolDefinition> getTools() {
        return List.of(
            tool("calendar_list_events", "List upcoming calendar events", "productivity", "low"),
            tool("calendar_create_event", "Create a calendar event", "productivity", "medium"),
            tool("calendar_update_event", "Update an existing event", "productivity", "medium"),
            tool("calendar_delete_event", "Delete a calendar event", "productivity", "high")
        );
    }

    private static ToolDefinition tool(String name, String desc, String category, String risk) {
        ToolDefinition def = new ToolDefinition();
        def.setName(name);
        def.setDescription(desc);
        def.setProvider("calendar");
        def.setCost(1);
        def.setRiskLevel(risk);
        return def;
    }
}