package com.agentos.goal.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GoalRegistry {
    private static final Logger log = LoggerFactory.getLogger(GoalRegistry.class);
    private final Map<String, Goal> goals = new ConcurrentHashMap<>();

    public Goal create(String title, String description, String workspaceId) {
        Goal goal = new Goal(title, description, workspaceId);
        goals.put(goal.getId(), goal);
        log.info("Created goal: {} ({})", title, goal.getId());
        return goal;
    }

    public Goal get(String id) { return goals.get(id); }

    public List<Goal> list(String workspaceId) {
        return goals.values().stream()
            .filter(g -> workspaceId == null || g.getWorkspaceId().equals(workspaceId))
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .collect(Collectors.toList());
    }

    public Goal updateStatus(String id, GoalStatus status) {
        Goal goal = goals.get(id);
        if (goal != null) { goal.setStatus(status); }
        return goal;
    }

    public boolean delete(String id) { return goals.remove(id) != null; }

    public Map<String, Object> getStats() {
        long active = goals.values().stream().filter(g -> g.getStatus() == GoalStatus.ACTIVE).count();
        long completed = goals.values().stream().filter(g -> g.getStatus() == GoalStatus.COMPLETED).count();
        long overdue = goals.values().stream().filter(Goal::isOverdue).count();
        return Map.of("total", goals.size(), "active", active, "completed", completed, "overdue", overdue);
    }
}