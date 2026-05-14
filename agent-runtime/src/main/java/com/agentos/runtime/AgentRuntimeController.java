package com.agentos.runtime;

import com.agentos.runtime.core.*;
import com.agentos.runtime.multiagent.MultiAgentOrchestrator;
import com.agentos.runtime.workflow.WorkflowEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/runtime")
public class AgentRuntimeController {

    @Autowired private AgentRegistry registry;
    @Autowired private MultiAgentOrchestrator orchestrator;
    @Autowired private WorkflowEngine workflowEngine;

    // ===== Agent Lifecycle =====

    @GetMapping("/agents")
    public ResponseEntity<?> listAgents() {
        return ResponseEntity.ok(registry.listAgents());
    }

    @PostMapping("/agents/{id}/execute")
    public ResponseEntity<?> execute(@PathVariable String id, @RequestBody AgentRequest request) {
        Agent agent = registry.getAgent(id);
        if (agent == null) return ResponseEntity.notFound().build();
        try {
            var response = agent.execute(request).get();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(AgentResponse.failure(id, e.getMessage()));
        }
    }

    @PostMapping("/agents/{id}/interrupt")
    public ResponseEntity<?> interrupt(@PathVariable String id) {
        Agent agent = registry.getAgent(id);
        if (agent != null) agent.interrupt();
        return ResponseEntity.ok(Map.of("status", "interrupted"));
    }

    @PostMapping("/agents/{id}/resume")
    public ResponseEntity<?> resume(@PathVariable String id) {
        Agent agent = registry.getAgent(id);
        if (agent != null) agent.resume();
        return ResponseEntity.ok(Map.of("status", "resumed"));
    }

    @PostMapping("/agents/{id}/reset")
    public ResponseEntity<?> reset(@PathVariable String id) {
        Agent agent = registry.getAgent(id);
        if (agent != null) agent.reset();
        return ResponseEntity.ok(Map.of("status", "reset"));
    }

    // ===== Routing =====

    @PostMapping("/route")
    public ResponseEntity<?> route(@RequestBody AgentRequest request) {
        Agent agent = registry.route(request.getMessage());
        if (agent == null) return ResponseEntity.ok(AgentResponse.failure("none", "No matching agent"));
        try {
            var response = agent.execute(request).get();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(AgentResponse.failure(agent.getId(), e.getMessage()));
        }
    }

    // ===== Multi-Agent =====

    @PostMapping("/collaborate")
    public ResponseEntity<?> collaborate(@RequestBody CollaborateRequest req) {
        var result = orchestrator.collaborate(req.primaryAgentId, req.task, req.collaboratorIds);
        return ResponseEntity.ok(result);
    }

    // ===== Workflow =====

    @PostMapping("/workflows/create")
    public ResponseEntity<?> createWorkflow(@RequestBody Map<String, String> body) {
        var wf = workflowEngine.createWorkflow(body.get("name"), body.get("description"));
        return ResponseEntity.ok(Map.of("workflowId", wf.getId()));
    }

    @PostMapping("/workflows/{id}/execute")
    public ResponseEntity<?> executeWorkflow(@PathVariable String id) {
        return ResponseEntity.ok(workflowEngine.execute(id));
    }

    public static class CollaborateRequest {
        public String primaryAgentId; public String task; public List<String> collaboratorIds;
    }
}