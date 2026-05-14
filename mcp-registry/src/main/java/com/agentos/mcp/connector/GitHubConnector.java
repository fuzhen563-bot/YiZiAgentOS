package com.agentos.mcp.connector;

import com.agentos.mcp.registry.ToolDefinition;
import java.util.*;

public class GitHubConnector {
    public static List<ToolDefinition> getTools() {
        return List.of(
            tool("github_list_repos", "List repositories for the authenticated user", low("owner", "string"), "code"),
            tool("github_get_file", "Get file content from a repository", medium("owner", "string", "repo", "string", "path", "string"), "code"),
            tool("github_create_issue", "Create an issue in a repository", medium("owner", "string", "repo", "string", "title", "string"), "code"),
            tool("github_search_code", "Search code across repositories", medium("query", "string"), "code")
        );
    }

    private static ToolDefinition tool(String name, String desc, Map<String, Object> schema, String category) {
        ToolDefinition def = new ToolDefinition();
        def.setName(name);
        def.setDescription(desc);
        def.setInputSchema(schema);
        def.setProvider("github");
        def.setCost(1);
        def.setRiskLevel("medium");
        return def;
    }

    private static Map<String, Object> low(Object... kv) { return schema(kv); }
    private static Map<String, Object> medium(Object... kv) { return schema(kv); }
    private static Map<String, Object> high(Object... kv) { return schema(kv); }

    private static Map<String, Object> schema(Object... kv) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "object");
        Map<String, Object> props = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            Map<String, Object> prop = new LinkedHashMap<>();
            prop.put("type", kv[i + 1]);
            props.put((String) kv[i], prop);
        }
        map.put("properties", props);
        return map;
    }
}