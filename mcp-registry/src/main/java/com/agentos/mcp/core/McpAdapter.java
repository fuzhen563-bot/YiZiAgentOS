package com.agentos.mcp.core;

import com.agentos.mcp.registry.ToolDefinition;
import java.util.List;
import java.util.Map;

public interface McpAdapter {
    McpTransport supportedTransport();
    void connect(String endpoint, Map<String, String> auth);
    void disconnect();
    boolean isConnected();
    List<ToolDefinition> listTools();
    Map<String, Object> callTool(String toolName, Map<String, Object> args);
    Map<String, Object> getResource(String uri);
    void ping();
}