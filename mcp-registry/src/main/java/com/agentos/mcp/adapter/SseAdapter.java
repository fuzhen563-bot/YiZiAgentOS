package com.agentos.mcp.adapter;

import com.agentos.mcp.core.McpAdapter;
import com.agentos.mcp.core.McpTransport;
import com.agentos.mcp.registry.ToolDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

public class SseAdapter implements McpAdapter {
    private static final Logger log = LoggerFactory.getLogger(SseAdapter.class);
    private final String endpoint;
    private Map<String, String> auth;
    private volatile boolean connected;

    public SseAdapter(String endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public McpTransport supportedTransport() { return McpTransport.SSE; }

    @Override
    public void connect(String endpoint, Map<String, String> auth) {
        this.auth = auth;
        this.connected = true;
        log.info("Connected to MCP SSE: {}", endpoint);
    }

    @Override
    public void disconnect() { connected = false; }

    @Override
    public boolean isConnected() { return connected; }

    @Override
    public List<ToolDefinition> listTools() { return List.of(); }

    @Override
    public Map<String, Object> callTool(String toolName, Map<String, Object> args) {
        return Map.of("status", "queued", "tool", toolName);
    }

    @Override
    public Map<String, Object> getResource(String uri) { return Map.of(); }

    @Override
    public void ping() {}
}