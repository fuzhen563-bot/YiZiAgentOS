package com.agentos.goal.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/goals")
public class GoalController {

    @Autowired private GoalRegistry registry;
    @Autowired private ObjectiveEngine objectiveEngine;
    @Autowired private com.agentos.goal.task.TaskGraph taskGraph;
    @Autowired private com.agentos.goal.persist.CheckpointManager checkpointManager;
    @Autowired private ReplanningEngine replanningEngine;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        Goal g = registry.create(body.get("title"), body.get("description"), body.get("workspaceId"));
        return ResponseEntity.ok(Map.of("id", g.getId(), "title", g.getTitle()));
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String workspaceId) {
        return ResponseEntity.ok(registry.list(workspaceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id) {
        Goal g = registry.get(id);
        if (g == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(g);
    }

    @PostMapping("/{id}/decompose")
    public ResponseEntity<?> decompose(@PathVariable String id) {
        Goal g = registry.get(id);
        if (g == null) return ResponseEntity.notFound().build();
        var tasks = objectiveEngine.decompose(g.getDescription());
        tasks.forEach(t -> {
            var node = taskGraph.addNode(id, t);
            g.addTask(node.getId());
        });
        return ResponseEntity.ok(Map.of("goalId", id, "tasks", tasks.size()));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestParam String status) {
        registry.updateStatus(id, GoalStatus.valueOf(status.toUpperCase()));
        return ResponseEntity.ok(Map.of("status", "updated"));
    }

    @PostMapping("/{id}/kpi")
    public ResponseEntity<?> setKpi(@PathVariable String id, @RequestParam String name, @RequestParam double value) {
        Goal g = registry.get(id);
        if (g == null) return ResponseEntity.notFound().build();
        g.setKpi(name, value);
        return ResponseEntity.ok(Map.of("kpi", name, "value", value));
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<?> progress(@PathVariable String id) {
        Goal g = registry.get(id);
        if (g == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("progress", objectiveEngine.calculateProgress(g)));
    }

    @GetMapping("/{id}/task-graph")
    public ResponseEntity<?> taskGraph(@PathVariable String id) {
        return ResponseEntity.ok(taskGraph.getGraphState(id));
    }

    @PostMapping("/{id}/checkpoint")
    public ResponseEntity<?> saveCheckpoint(@PathVariable String id) {
        Goal g = registry.get(id);
        if (g == null) return ResponseEntity.notFound().build();
        var cp = checkpointManager.save(id, Map.of("goalId", id, "title", g.getTitle()));
        return ResponseEntity.ok(Map.of("checkpointId", cp.getId()));
    }

    @GetMapping("/{id}/recovery")
    public ResponseEntity<?> recovery(@PathVariable String id) {
        return ResponseEntity.ok(checkpointManager.recoveryPlan(id, registry.list(null), taskGraph));
    }

    @GetMapping("/overdue")
    public ResponseEntity<?> overdue() {
        return ResponseEntity.ok(objectiveEngine.checkDeadlines(registry.list(null)));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        return ResponseEntity.ok(registry.getStats());
    }
}