package com.agentos.runtime.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

public class ToolRegistryClient {
    private static final Logger log = LoggerFactory.getLogger(ToolRegistryClient.class);
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static String mcpUrl = "http://localhost:9092/api/v1/mcp";

    public static void configure(String url) {
        if (url != null) mcpUrl = url;
    }

    public static List<Map<String, Object>> listTools() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(mcpUrl + "/tools"))
                .timeout(Duration.ofSeconds(5))
                .GET().build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(), new TypeReference<List<Map<String, Object>>>() {});
            }
        } catch (Exception e) {
            log.debug("MCP not available: {}", e.getMessage());
        }
        return List.of();
    }

    public static Map<String, Object> callTool(String name, Map<String, Object> args) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("name", name);
            body.put("arguments", args);

            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(mcpUrl + "/tools/call"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .timeout(Duration.ofSeconds(30))
                .build();

            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) {
                return mapper.readValue(resp.body(), new TypeReference<Map<String, Object>>() {});
            }
        } catch (Exception e) {
            log.warn("Tool call failed: {}", e.getMessage());
        }
        return Map.of("error", "Tool call failed: " + name);
    }
}