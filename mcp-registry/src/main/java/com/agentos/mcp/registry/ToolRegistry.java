package com.agentos.mcp.registry;

import com.agentos.mcp.core.McpAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ToolRegistry {
    private static final Logger log = LoggerFactory.getLogger(ToolRegistry.class);
    private final Map<String, ToolDefinition> tools = new ConcurrentHashMap<>();
    private final Map<String, McpAdapter> adapters = new ConcurrentHashMap<>();
    private final Map<String, String> toolToAdapter = new ConcurrentHashMap<>();

    public void registerAdapter(String id, McpAdapter adapter) {
        adapters.put(id, adapter);
        try {
            List<ToolDefinition> remoteTools = adapter.listTools();
            for (ToolDefinition tool : remoteTools) {
                tool.setProvider(id);
                String key = id + ":" + tool.getName();
                tools.put(key, tool);
                toolToAdapter.put(key, id);
            }
            log.info("Registered adapter '{}' with {} tools", id, remoteTools.size());
        } catch (Exception e) {
            log.warn("Failed to list tools from adapter '{}': {}", id, e.getMessage());
        }
    }

    public void registerTool(ToolDefinition tool) {
        tools.put(tool.getName(), tool);
        log.info("Registered tool: {}", tool.getName());
    }

    public ToolDefinition getTool(String name) {
        return tools.get(name);
    }

    public List<ToolDefinition> listTools(String provider) {
        if (provider == null || provider.isBlank()) return new ArrayList<>(tools.values());
        return tools.values().stream()
            .filter(t -> provider.equals(t.getProvider()))
            .collect(Collectors.toList());
    }

    public List<ToolDefinition> searchTools(String query) {
        String q = query.toLowerCase();
        return tools.values().stream()
            .filter(t -> t.getName().toLowerCase().contains(q)
                || (t.getDescription() != null && t.getDescription().toLowerCase().contains(q)))
            .collect(Collectors.toList());
    }

    public Map<String, Object> callTool(String toolName, Map<String, Object> args) {
        String adapterId = toolToAdapter.get(toolName);
        if (adapterId != null) {
            McpAdapter adapter = adapters.get(adapterId);
            if (adapter != null) return adapter.callTool(toolName, args);
        }
        ToolDefinition def = tools.get(toolName);
        if (def == null) return Map.of("error", "Tool not found: " + toolName);
        return simulateResult(toolName, args, def);
    }

    private Map<String, Object> simulateResult(String toolName, Map<String, Object> args, ToolDefinition def) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "success");
        result.put("tool", toolName);
        result.put("simulated", true);
        result.put("provider", def.getProvider());

        if (toolName.contains("list_repos")) {
            String owner = args != null ? (String) args.getOrDefault("owner", "user") : "user";
            result.put("data", List.of(
                Map.of("name", owner + "/project-a", "stars", 42, "language", "Java"),
                Map.of("name", owner + "/project-b", "stars", 128, "language", "TypeScript"),
                Map.of("name", owner + "/agentos", "stars", 256, "language", "Java")
            ));
            result.put("summary", "Found 3 repositories for " + owner);
        }
        else if (toolName.contains("get_file")) {
            String path = args != null ? (String) args.getOrDefault("path", "README.md") : "README.md";
            result.put("data", Map.of("path", path, "content", "# " + path + "\n\nThis is a sample file content.", "size", 1024));
        }
        else if (toolName.contains("create_issue")) {
            String title = args != null ? (String) args.getOrDefault("title", "Issue") : "Issue";
            result.put("data", Map.of("issue_number", (int)(Math.random() * 1000), "title", title, "state", "open"));
            result.put("summary", "Created issue: " + title);
        }
        else if (toolName.contains("search_code")) {
            String query = args != null ? (String) args.getOrDefault("query", "") : "";
            result.put("data", List.of(
                Map.of("file", "src/main.java", "matches", 3),
                Map.of("file", "src/utils.js", "matches", 1)
            ));
            result.put("summary", "Found " + query + " in 2 files");
        }
        else if (toolName.contains("email_send")) {
            result.put("data", Map.of("messageId", "msg-" + UUID.randomUUID().toString().substring(0, 8), "status", "sent"));
            result.put("summary", "Email sent successfully");
        }
        else if (toolName.contains("email_read")) {
            result.put("data", List.of(
                Map.of("from", "boss@company.com", "subject", "Q4 Review", "date", "2026-05-14"),
                Map.of("from", "team@company.com", "subject", "Meeting Notes", "date", "2026-05-13")
            ));
            result.put("summary", "Found 2 unread emails");
        }
        else if (toolName.contains("list_events")) {
            result.put("data", List.of(
                Map.of("title", "Team Standup", "date", "2026-05-15", "time", "10:00"),
                Map.of("title", "Sprint Planning", "date", "2026-05-16", "time", "14:00")
            ));
            result.put("summary", "Found 2 upcoming events");
        }
        else if (toolName.contains("create_event")) {
            result.put("data", Map.of("eventId", "evt-" + UUID.randomUUID().toString().substring(0, 8), "status", "created"));
            result.put("summary", "Event created");
        }
        else if (toolName.contains("crm_list_contacts") || toolName.contains("crm_get_contact")) {
            result.put("data", List.of(
                Map.of("name", "Alice Wang", "company", "Tech Corp", "email", "alice@tech.com"),
                Map.of("name", "Bob Li", "company", "Data Inc", "email", "bob@data.com")
            ));
            result.put("summary", "Found 2 contacts");
        }
        else if (toolName.contains("crm_list_deals") || toolName.contains("crm_get_deal")) {
            result.put("data", List.of(
                Map.of("deal", "Enterprise License", "value", "$50,000", "stage", "negotiation"),
                Map.of("deal", "SaaS Subscription", "value", "$12,000", "stage", "closed_won")
            ));
            result.put("summary", "Found 2 deals");
        }
        else if (toolName.contains("erp_list_inventory")) {
            result.put("data", List.of(
                Map.of("sku", "PRD-001", "name", "Widget A", "qty", 150),
                Map.of("sku", "PRD-002", "name", "Widget B", "qty", 75)
            ));
            result.put("summary", "Found 2 inventory items");
        }
        else if (toolName.contains("erp_get_order")) {
            result.put("data", Map.of("orderId", "ORD-2026-001", "status", "shipped", "total", "$3,200"));
            result.put("summary", "Order retrieved");
        }
        else {
            result.put("data", Map.of("result", toolName + " executed successfully"));
            result.put("summary", toolName + " completed");
        }
        return result;
    }

    public void removeTool(String name) {
        tools.remove(name);
        toolToAdapter.remove(name);
    }

    public Map<String, Object> getStats() {
        return Map.of(
            "total_tools", tools.size(),
            "total_adapters", adapters.size(),
            "tools_by_provider", tools.values().stream()
                .collect(Collectors.groupingBy(ToolDefinition::getProvider, Collectors.counting()))
        );
    }
}