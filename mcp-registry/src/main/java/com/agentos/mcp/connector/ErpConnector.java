package com.agentos.mcp.connector;

import com.agentos.mcp.registry.ToolDefinition;
import java.util.*;

public class ErpConnector {
    public static List<ToolDefinition> getTools() {
        return List.of(
            tool("erp_list_inventory", "List inventory items", "business", "medium"),
            tool("erp_get_order", "Get order details", "business", "high"),
            tool("erp_list_orders", "List purchase orders", "business", "high"),
            tool("erp_get_invoice", "Get invoice details", "business", "high"),
            tool("erp_create_po", "Create a purchase order", "business", "critical")
        );
    }

    private static ToolDefinition tool(String name, String desc, String category, String risk) {
        ToolDefinition def = new ToolDefinition();
        def.setName(name);
        def.setDescription(desc);
        def.setProvider("erp");
        def.setCost(3);
        def.setRiskLevel(risk);
        return def;
    }
}