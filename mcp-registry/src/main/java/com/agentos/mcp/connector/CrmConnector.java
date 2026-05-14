package com.agentos.mcp.connector;

import com.agentos.mcp.registry.ToolDefinition;
import java.util.*;

public class CrmConnector {
    public static List<ToolDefinition> getTools() {
        return List.of(
            tool("crm_list_contacts", "List CRM contacts", "business", "medium"),
            tool("crm_get_contact", "Get contact details", "business", "medium"),
            tool("crm_create_contact", "Create a new contact", "business", "high"),
            tool("crm_list_deals", "List sales deals", "business", "high"),
            tool("crm_get_deal", "Get deal details", "business", "high")
        );
    }

    private static ToolDefinition tool(String name, String desc, String category, String risk) {
        ToolDefinition def = new ToolDefinition();
        def.setName(name);
        def.setDescription(desc);
        def.setProvider("crm");
        def.setCost(2);
        def.setRiskLevel(risk);
        return def;
    }
}