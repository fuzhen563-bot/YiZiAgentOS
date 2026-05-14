package com.agentos.goal.persist;

import com.agentos.goal.core.Goal;
import com.agentos.goal.task.TaskGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CheckpointManager {
    private static final Logger log = LoggerFactory.getLogger(CheckpointManager.class);
    private final Map<String, Checkpoint> checkpoints = new ConcurrentHashMap<>();

    public static class Checkpoint {
        private String id;
        private String goalId;
        private Map<String, Object> snapshot;
        private Map<String, Object> taskStates;
        private long createdAt;

        public Checkpoint(String goalId, Map<String, Object> snapshot) {
            this.id = UUID.randomUUID().toString();
            this.goalId = goalId;
            this.snapshot = snapshot;
            this.createdAt = System.currentTimeMillis();
        }

        public String getId() { return id; }
        public String getGoalId() { return goalId; }
        public Map<String, Object> getSnapshot() { return snapshot; }
        public long getCreatedAt() { return createdAt; }
        public void setTaskStates(Map<String, Object> taskStates) { this.taskStates = taskStates; }
        public Map<String, Object> getTaskStates() { return taskStates; }
    }

    public Checkpoint save(String goalId, Map<String, Object> snapshot) {
        Checkpoint cp = new Checkpoint(goalId, snapshot);
        checkpoints.put(cp.getId(), cp);
        log.info("Checkpoint saved for goal {}: {}", goalId, cp.getId());
        return cp;
    }

    public Checkpoint restore(String checkpointId) {
        return checkpoints.get(checkpointId);
    }

    public List<Checkpoint> list(String goalId) {
        return checkpoints.values().stream()
            .filter(c -> c.getGoalId().equals(goalId))
            .sorted((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()))
            .toList();
    }

    public Map<String, Object> recoveryPlan(String goalId, List<Goal> goals, TaskGraph graph) {
        Checkpoint latest = list(goalId).stream().findFirst().orElse(null);
        if (latest == null) return Map.of("status", "no_checkpoint", "action", "start_fresh");

        Goal goal = goals.stream().filter(g -> g.getId().equals(goalId)).findFirst().orElse(null);
        if (goal == null) return Map.of("status", "goal_not_found");

        List<String> incompleteTasks = graph.getReadyTasks(goalId);
        return Map.of(
            "status", "recovery_available",
            "checkpoint", latest.getId(),
            "goal", goal.getTitle(),
            "remaining_tasks", incompleteTasks.size(),
            "action", incompleteTasks.isEmpty() ? "goal_completed" : "resume_execution"
        );
    }
}