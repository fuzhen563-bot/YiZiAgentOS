package com.agentos.goal.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TaskGraph {
    private static final Logger log = LoggerFactory.getLogger(TaskGraph.class);
    private final Map<String, TaskNode> nodes = new ConcurrentHashMap<>();
    private final Map<String, List<String>> edges = new ConcurrentHashMap<>();

    public static class TaskNode {
        private String id;
        private String goalId;
        private String title;
        private String status;
        private String agentId;
        private Map<String, Object> state;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;

        public TaskNode(String goalId, String title) {
            this.id = UUID.randomUUID().toString();
            this.goalId = goalId;
            this.title = title;
            this.status = "pending";
            this.state = new HashMap<>();
            this.createdAt = LocalDateTime.now();
        }

        public String getId() { return id; }
        public String getGoalId() { return goalId; }
        public String getTitle() { return title; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; if ("completed".equals(status)) completedAt = LocalDateTime.now(); }
        public String getAgentId() { return agentId; }
        public void setAgentId(String agentId) { this.agentId = agentId; }
        public Map<String, Object> getState() { return state; }
        public void setState(String key, Object value) { state.put(key, value); }
        public LocalDateTime getCompletedAt() { return completedAt; }
    }

    public TaskNode addNode(String goalId, String title) {
        TaskNode node = new TaskNode(goalId, title);
        nodes.put(node.getId(), node);
        edges.put(node.getId(), new ArrayList<>());
        return node;
    }

    public void addDependency(String fromId, String toId) {
        edges.computeIfAbsent(fromId, k -> new ArrayList<>()).add(toId);
    }

    public TaskNode getNode(String id) { return nodes.get(id); }
    public List<TaskNode> getNodes(String goalId) {
        return nodes.values().stream().filter(n -> n.getGoalId().equals(goalId)).collect(Collectors.toList());
    }

    public List<String> getReadyTasks(String goalId) {
        List<String> ready = new ArrayList<>();
        for (TaskNode node : getNodes(goalId)) {
            if (!"pending".equals(node.getStatus())) continue;
            List<String> deps = edges.getOrDefault(node.getId(), List.of());
            boolean allDone = deps.stream().allMatch(d -> {
                TaskNode dep = nodes.get(d);
                return dep != null && "completed".equals(dep.getStatus());
            });
            if (allDone) ready.add(node.getId());
        }
        return ready;
    }

    public Map<String, Object> getGraphState(String goalId) {
        List<Map<String, Object>> taskStates = getNodes(goalId).stream().map(n -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", n.getId());
            m.put("title", n.getTitle());
            m.put("status", n.getStatus());
            m.put("dependencies", edges.getOrDefault(n.getId(), List.of()));
            m.put("state", n.getState());
            return m;
        }).collect(Collectors.toList());
        return Map.of("goalId", goalId, "tasks", taskStates, "ready", getReadyTasks(goalId));
    }
}