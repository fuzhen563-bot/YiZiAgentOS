package com.agentos.mcp.adapter;

import com.agentos.mcp.core.McpAdapter;
import com.agentos.mcp.core.McpTransport;
import com.agentos.mcp.registry.ToolDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class StdioAdapter implements McpAdapter {
    private static final Logger log = LoggerFactory.getLogger(StdioAdapter.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final String command;
    private final List<String> args;
    private Process process;
    private BufferedReader reader;
    private BufferedWriter writer;
    private long requestId;

    public StdioAdapter(String command, List<String> args) {
        this.command = command;
        this.args = args;
    }

    @Override
    public McpTransport supportedTransport() { return McpTransport.STDIO; }

    @Override
    public void connect(String endpoint, Map<String, String> auth) {
        try {
            List<String> cmd = new ArrayList<>();
            cmd.add(command);
            cmd.addAll(args);
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.environment().putAll(auth);
            process = pb.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            log.info("Connected to MCP server via stdio: {}", command);
        } catch (Exception e) {
            throw new RuntimeException("Failed to start MCP process: " + command, e);
        }
    }

    @Override
    public void disconnect() {
        if (process != null) { process.destroy(); process = null; }
    }

    @Override
    public boolean isConnected() { return process != null && process.isAlive(); }

    @Override
    public List<ToolDefinition> listTools() {
        Map<String, Object> result = sendRequest("tools/list", Map.of());
        if (result == null || result.get("tools") == null) return List.of();
        return ((List<Map<String, Object>>) result.get("tools")).stream()
            .map(this::toToolDef).toList();
    }

    @Override
    public Map<String, Object> callTool(String toolName, Map<String, Object> args) {
        return sendRequest("tools/call", Map.of("name", toolName, "arguments", args));
    }

    @Override
    public Map<String, Object> getResource(String uri) {
        return sendRequest("resources/read", Map.of("uri", uri));
    }

    @Override
    public void ping() { sendRequest("ping", Map.of()); }

    private Map<String, Object> sendRequest(String method, Map<String, Object> params) {
        try {
            String id = String.valueOf(++requestId);
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("jsonrpc", "2.0");
            request.put("id", id);
            request.put("method", method);
            request.put("params", params);
            String json = mapper.writeValueAsString(request);
            writer.write(json + "\n");
            writer.flush();
            String line = reader.readLine();
            if (line == null) return null;
            Map<String, Object> response = mapper.readValue(line, Map.class);
            if (response.containsKey("error")) {
                log.warn("MCP error: {}", response.get("error"));
                return null;
            }
            return (Map<String, Object>) response.get("result");
        } catch (Exception e) {
            log.error("MCP request failed: {}", e.getMessage());
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