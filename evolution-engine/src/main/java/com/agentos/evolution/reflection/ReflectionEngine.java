package com.agentos.evolution.reflection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ReflectionEngine {
    private static final Logger log = LoggerFactory.getLogger(ReflectionEngine.class);
    private final Map<String, TaskRecord> tasks = new ConcurrentHashMap<>();

    public static class TaskRecord {
        private String id;
        private String agentId;
        private String goal;
        private String status;
        private List<String> steps;
        private List<String> toolsUsed;
        private int totalTokens;
        private long durationMs;
        private String errorMessage;
        private long createdAt;

        public TaskRecord(String agentId, String goal) {
            this.id = UUID.randomUUID().toString();
            this.agentId = agentId;
            this.goal = goal;
            this.status = "running";
            this.steps = new ArrayList<>();
            this.toolsUsed = new ArrayList<>();
            this.createdAt = System.currentTimeMillis();
        }

        public String getId() { return id; }
        public String getAgentId() { return agentId; }
        public String getGoal() { return goal; }
        public String getStatus() { return status; }
        public List<String> getSteps() { return steps; }
        public List<String> getToolsUsed() { return toolsUsed; }
        public int getTotalTokens() { return totalTokens; }
        public long getDurationMs() { return durationMs; }
        public String getErrorMessage() { return errorMessage; }

        public void addStep(String step) { steps.add(step); }
        public void addTool(String tool) { toolsUsed.add(tool); }
        public void complete(int tokens) {
            this.status = "success";
            this.totalTokens = tokens;
            this.durationMs = System.currentTimeMillis() - createdAt;
        }
        public void fail(String error) {
            this.status = "failure";
            this.errorMessage = error;
            this.durationMs = System.currentTimeMillis() - createdAt;
        }
    }

    public TaskRecord startTask(String agentId, String goal) {
        TaskRecord task = new TaskRecord(agentId, goal);
        tasks.put(task.getId(), task);
        return task;
    }

    public void recordStep(String taskId, String step) {
        TaskRecord task = tasks.get(taskId);
        if (task != null) task.addStep(step);
    }

    public void completeTask(String taskId, int tokens) {
        TaskRecord task = tasks.get(taskId);
        if (task != null) task.complete(tokens);
    }

    public void failTask(String taskId, String error) {
        TaskRecord task = tasks.get(taskId);
        if (task != null) task.fail(error);
    }

    public Map<String, Object> analyze(String taskId) {
        TaskRecord task = tasks.get(taskId);
        if (task == null) return Map.of("error", "Task not found");

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("taskId", task.getId());
        report.put("agentId", task.getAgentId());
        report.put("goal", task.getGoal());
        report.put("status", task.getStatus());
        report.put("durationMs", task.getDurationMs());
        report.put("totalTokens", task.getTotalTokens());
        report.put("toolsUsed", task.getToolsUsed());

        if ("failure".equals(task.getStatus())) {
            report.put("failureReason", task.getErrorMessage());
            report.put("suggestion", generateSuggestion(task));
        }
        if (!task.getToolsUsed().isEmpty()) {
            report.put("optimization", suggestOptimization(task));
        }
        report.put("cost", task.getTotalTokens() * 0.000002);
        return report;
    }

    public Map<String, Object> getAgentSummary(String agentId) {
        List<TaskRecord> agentTasks = tasks.values().stream()
            .filter(t -> t.getAgentId().equals(agentId)).toList();
        long success = agentTasks.stream().filter(t -> "success".equals(t.getStatus())).count();
        long failed = agentTasks.stream().filter(t -> "failure".equals(t.getStatus())).count();
        double avgDuration = agentTasks.stream().mapToLong(TaskRecord::getDurationMs).average().orElse(0);
        int totalTokens = agentTasks.stream().mapToInt(TaskRecord::getTotalTokens).sum();
        return Map.of(
            "agentId", agentId,
            "totalTasks", agentTasks.size(),
            "success", success,
            "failed", failed,
            "successRate", agentTasks.isEmpty() ? 0 : (double) success / agentTasks.size(),
            "avgDurationMs", avgDuration,
            "totalTokens", totalTokens,
            "estimatedCost", totalTokens * 0.000002
        );
    }

    public List<Map<String, Object>> listTasks(String agentId) {
        return tasks.values().stream()
            .filter(t -> agentId == null || t.getAgentId().equals(agentId))
            .map(t -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", t.getId());
                m.put("goal", t.getGoal());
                m.put("status", t.getStatus());
                m.put("durationMs", t.getDurationMs());
                return m;
            })
            .collect(Collectors.toList());
    }

    private String generateSuggestion(TaskRecord task) {
        if (task.getErrorMessage() == null) return "No specific error";
        if (task.getErrorMessage().contains("timeout")) return "Consider increasing timeout or splitting task";
        if (task.getErrorMessage().contains("rate limit")) return "Add retry with backoff";
        if (task.getErrorMessage().contains("auth")) return "Check API key permissions";
        return "Review task goal and retry with simpler steps";
    }

    private String suggestOptimization(TaskRecord task) {
        if (task.getToolsUsed().size() > 5) {
            return "Too many tool calls (" + task.getToolsUsed().size() + "), consider batching";
        }
        return "Tool path looks optimal";
    }
}