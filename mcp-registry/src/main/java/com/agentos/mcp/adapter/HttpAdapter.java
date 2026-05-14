package com.agentos.mcp.adapter;

import com.agentos.mcp.core.McpAdapter;
import com.agentos.mcp.core.McpTransport;
import com.agentos.mcp.registry.ToolDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

public class HttpAdapter implements McpAdapter {
    private static final Logger log = LoggerFactory.getLogger(HttpAdapter.class);
    private final RestTemplate rest = new RestTemplate();
    private String baseUrl;
    private HttpHeaders headers;

    @Override
    public McpTransport supportedTransport() { return McpTransport.STREAMABLE_HTTP; }

    @Override
    public void connect(String endpoint, Map<String, String> auth) {
        this.baseUrl = endpoint;
        this.headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (auth != null) auth.forEach(headers::set);
    }

    @Override
    public void disconnect() { baseUrl = null; headers = null; }

    @Override
    public boolean isConnected() { return baseUrl != null; }

    @Override
    public List<ToolDefinition> listTools() {
        Map<String, Object> result = post("tools/list", Map.of());
        if (result == null) return List.of();
        return ((List<Map<String, Object>>) result.get("tools")).stream()
            .map(this::toToolDef).toList();
    }

    @Override
    public Map<String, Object> callTool(String toolName, Map<String, Object> args) {
        return post("tools/call", Map.of("name", toolName, "arguments", args));
    }

    @Override
    public Map<String, Object> getResource(String uri) {
        return post("resources/read", Map.of("uri", uri));
    }

    @Override
    public void ping() { post("ping", Map.of()); }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String method, Map<String, Object> params) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("jsonrpc", "2.0");
            body.put("id", UUID.randomUUID().toString());
            body.put("method", method);
            body.put("params", params);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            var resp = rest.exchange(baseUrl, HttpMethod.POST, entity, Map.class);
            Map<String, Object> result = resp.getBody();
            if (result == null || result.containsKey("error")) {
                log.warn("MCP HTTP error: {}", result != null ? result.get("error") : "null");
                return null;
            }
            return (Map<String, Object>) result.get("result");
        } catch (Exception e) {
            log.error("MCP HTTP request failed: {}", e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private ToolDefinition toToolDef(Map<String, Object> m) {
        ToolDefinition def = new ToolDefinition();
        def.setName((String) m.get("name"));
        def.setDescription((String) m.get("description"));
        def.setInputSchema((Map<String, Object>) m.get("inputSchema"));
        return def;
    }
}