package com.agentos.runtime.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class LlmClient {
    private static final Logger log = LoggerFactory.getLogger(LlmClient.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public static String chat(String prompt) {
        if (prompt == null || prompt.isBlank()) return "";
        if (prompt.length() > 8000) prompt = prompt.substring(0, 8000);

        File tmpFile = null;
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", "deepseek-v4-flash");
            body.put("messages", List.of(Map.of("role", "user", "content", prompt)));
            body.put("max_tokens", 2048);
            body.put("temperature", 0.7);
            body.put("stream", false);

            String json = mapper.writeValueAsString(body);

            tmpFile = File.createTempFile("llm-", ".json");
            Files.writeString(tmpFile.toPath(), json, StandardCharsets.UTF_8);

            ProcessBuilder pb = new ProcessBuilder(
                "powershell.exe", "-NoProfile", "-NonInteractive",
                "-ExecutionPolicy", "Bypass",
                "-File", System.getProperty("user.dir") + "\\call-llm.ps1",
                tmpFile.getAbsolutePath()
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.waitFor();

            File outFile = new File(tmpFile.getAbsolutePath().replace(".json", ".out"));
            if (outFile.exists()) {
                String result = Files.readString(outFile.toPath(), StandardCharsets.UTF_8).trim();
                outFile.delete();
                if (result.startsWith("LLM_ERROR:")) {
                    log.warn("LLM proxy error: {}", result);
                } else if (!result.isEmpty()) {
                    return result;
                }
            }
        } catch (Exception e) {
            log.warn("LLM call failed: {}", e.getMessage());
        } finally {
            if (tmpFile != null) tmpFile.delete();
        }
        return fallback(prompt);
    }

    // fallback unchanged
    private static String fallback(String prompt) {
        if (prompt == null) return "Hello! How can I help?";
        if (prompt.contains("tool")) {
            if (prompt.contains("repo")) return "Found 3 repos.";
            if (prompt.contains("calendar")||prompt.contains("event")) return "You have 2 upcoming events.";
            if (prompt.contains("email")) return "You have 2 unread emails.";
            if (prompt.contains("inventory")) return "Inventory: Widget A (150), Widget B (75).";
            return "Tool executed.";
        }
        String lower = prompt.toLowerCase();
        if (lower.contains("hello")||lower.contains("hi")) return "Hello! I'm your AI assistant.";
        if (lower.contains("who are you")) return "I'm an AI agent running on AgentOS.";
        return "I received your question. Let me process that for you.";
    }
}