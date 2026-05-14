package com.agentos.goal.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ReplanningEngine {
    private static final Logger log = LoggerFactory.getLogger(ReplanningEngine.class);

    public List<String> replan(String goalId, String failedTask, String reason, List<String> originalPlan) {
        log.info("Replanning for goal {} after task '{}' failed: {}", goalId, failedTask, reason);
        List<String> newPlan = new ArrayList<>();
        boolean found = false;
        for (String task : originalPlan) {
            if (task.equals(failedTask)) {
                found = true;
                newPlan.add("Retry: " + task + " (alternative approach)");
            } else if (found) {
                newPlan.add(task);
            } else {
                newPlan.add(task);
            }
        }
        if (!found) newPlan.addAll(originalPlan);
        return newPlan;
    }

    public List<String> simplify(String goalId, List<String> plan, int maxSteps) {
        if (plan.size() <= maxSteps) return plan;
        log.info("Simplifying plan for goal {} from {} to {} steps", goalId, plan.size(), maxSteps);
        List<String> simplified = new ArrayList<>();
        simplified.add(plan.get(0));
        int step = (plan.size() - 2) / (maxSteps - 2);
        for (int i = 1; i < plan.size() - 1 && simplified.size() < maxSteps - 1; i += step) {
            simplified.add(plan.get(i));
        }
        simplified.add(plan.get(plan.size() - 1));
        return simplified;
    }
}