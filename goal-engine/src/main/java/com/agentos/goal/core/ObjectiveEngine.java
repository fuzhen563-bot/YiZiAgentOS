package com.agentos.goal.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ObjectiveEngine {
    private static final Logger log = LoggerFactory.getLogger(ObjectiveEngine.class);

    public List<String> decompose(String goalDescription) {
        String[] sentences = goalDescription.split("[.。]");
        List<String> tasks = new ArrayList<>();
        for (String s : sentences) {
            String t = s.trim();
            if (t.length() > 5) tasks.add(t);
        }
        if (tasks.isEmpty()) {
            tasks.add("Analyze: " + goalDescription);
            tasks.add("Plan: Create execution plan");
            tasks.add("Execute: Carry out tasks");
            tasks.add("Review: Verify results");
        }
        log.info("Decomposed goal into {} tasks", tasks.size());
        return tasks;
    }

    public double calculateProgress(Goal goal) {
        if (goal.getKpis().isEmpty()) return 0;
        return goal.getKpis().values().stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    public List<Goal> checkDeadlines(List<Goal> goals) {
        return goals.stream().filter(g -> {
            if (g.getDeadline() == null) return false;
            return java.time.LocalDateTime.now().plusDays(1).isAfter(g.getDeadline());
        }).collect(Collectors.toList());
    }
}