package com.agentos.mcp;

import com.agentos.mcp.connector.*;
import com.agentos.mcp.registry.ToolDefinition;
import com.agentos.mcp.registry.ToolRegistry;
import com.agentos.mcp.skill.SkillDefinition;
import com.agentos.mcp.skill.SkillEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mcp")
public class McpController {

    @Autowired private ToolRegistry toolRegistry;
    @Autowired private SkillEngine skillEngine;

    // ===== Tool Registry =====

    @GetMapping("/tools")
    public ResponseEntity<?> listTools(@RequestParam(required = false) String provider) {
        return ResponseEntity.ok(toolRegistry.listTools(provider));
    }

    @GetMapping("/tools/search")
    public ResponseEntity<?> searchTools(@RequestParam String q) {
        return ResponseEntity.ok(toolRegistry.searchTools(q));
    }

    @PostMapping("/tools/register")
    public ResponseEntity<?> registerTool(@RequestBody ToolDefinition tool) {
        toolRegistry.registerTool(tool);
        return ResponseEntity.ok(Map.of("status", "registered", "name", tool.getName()));
    }

    @DeleteMapping("/tools/{name}")
    public ResponseEntity<?> removeTool(@PathVariable String name) {
        toolRegistry.removeTool(name);
        return ResponseEntity.ok(Map.of("status", "removed"));
    }

    @PostMapping("/tools/call")
    public ResponseEntity<?> callTool(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "tool name required"));
        }
        Map<String, Object> args = (Map<String, Object>) body.getOrDefault("arguments", Map.of());
        return ResponseEntity.ok(toolRegistry.callTool(name, args));
    }

    @GetMapping("/tools/stats")
    public ResponseEntity<?> toolStats() {
        return ResponseEntity.ok(toolRegistry.getStats());
    }

    // ===== Skill Engine =====

    @GetMapping("/skills")
    public ResponseEntity<?> listSkills() {
        return ResponseEntity.ok(skillEngine.listSkills());
    }

    @PostMapping("/skills/upload")
    public ResponseEntity<?> uploadSkill(@RequestBody UploadRequest req) {
        SkillDefinition skill = skillEngine.upload(req.name, req.version, req.description, req.manifest);
        return ResponseEntity.ok(Map.of("status", "uploaded", "id", skill.getId()));
    }

    @GetMapping("/skills/{id}")
    public ResponseEntity<?> getSkill(@PathVariable String id) {
        SkillDefinition skill = skillEngine.getSkill(id);
        if (skill == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(skill);
    }

    @GetMapping("/skills/search")
    public ResponseEntity<?> searchSkills(@RequestParam String q) {
        return ResponseEntity.ok(skillEngine.searchSkills(q));
    }

    @DeleteMapping("/skills/{id}")
    public ResponseEntity<?> deleteSkill(@PathVariable String id) {
        boolean removed = skillEngine.deleteSkill(id);
        return ResponseEntity.ok(Map.of("status", removed ? "deleted" : "not_found"));
    }

    // ===== Connectors =====

    @PostMapping("/connectors/init")
    public ResponseEntity<?> initConnectors() {
        for (ToolDefinition t : GitHubConnector.getTools()) toolRegistry.registerTool(t);
        for (ToolDefinition t : EmailConnector.getTools()) toolRegistry.registerTool(t);
        for (ToolDefinition t : CalendarConnector.getTools()) toolRegistry.registerTool(t);
        for (ToolDefinition t : CrmConnector.getTools()) toolRegistry.registerTool(t);
        for (ToolDefinition t : ErpConnector.getTools()) toolRegistry.registerTool(t);
        return ResponseEntity.ok(Map.of("status", "initialized", "tools", toolRegistry.getStats().get("total_tools")));
    }

    public static class UploadRequest {
        public String name; public String version; public String description; public String manifest;
    }
}