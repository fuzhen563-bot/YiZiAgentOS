package com.agentos.evolution.sop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SopEngine {
    private static final Logger log = LoggerFactory.getLogger(SopEngine.class);
    private final Map<String, SopProcedure> procedures = new ConcurrentHashMap<>();
    private final Map<String, List<String>> behaviorLogs = new ConcurrentHashMap<>();

    public static class SopStep {
        private int order;
        private String action;
        private String description;
        private String expectedOutcome;
        private List<String> requiredTools;

        public SopStep() {}

        public SopStep(int order, String action, String description) {
            this.order = order;
            this.action = action;
            this.description = description;
            this.requiredTools = new ArrayList<>();
            this.expectedOutcome = "";
        }

        public int getOrder() { return order; }
        public String getAction() { return action; }
        public String getDescription() { return description; }
        public String getExpectedOutcome() { return expectedOutcome; }
        public void setExpectedOutcome(String expectedOutcome) { this.expectedOutcome = expectedOutcome; }
        public List<String> getRequiredTools() { return requiredTools; }
        public void addRequiredTool(String tool) { requiredTools.add(tool); }
    }

    public static class SopProcedure {
        private String id;
        private String name;
        private String goal;
        private String version;
        private List<SopStep> steps;
        private String quality;
        private int usageCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public SopProcedure(String name, String goal) {
            this.id = UUID.randomUUID().toString();
            this.name = name;
            this.goal = goal;
            this.version = "1.0.0";
            this.steps = new ArrayList<>();
            this.quality = "pending";
            this.createdAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getGoal() { return goal; }
        public String getVersion() { return version; }
        public List<SopStep> getSteps() { return steps; }
        public void addStep(SopStep step) { steps.add(step); }
        public String getQuality() { return quality; }
        public void setQuality(String quality) { this.quality = quality; }
        public int getUsageCount() { return usageCount; }
        public void incrementUsage() { usageCount++; updatedAt = LocalDateTime.now(); }
    }

    public void logBehavior(String agentId, String action, String context) {
        behaviorLogs.computeIfAbsent(agentId, k -> Collections.synchronizedList(new ArrayList<>())).add(action + ": " + context);
    }

    public SopProcedure extractSop(String agentId, String name, String goal) {
        List<String> logs = behaviorLogs.get(agentId);
        SopProcedure sop = new SopProcedure(name, goal);
        if (logs != null) {
            int order = 1;
            for (String log : logs) {
                SopStep step = new SopStep(order++, log, "Extracted from behavior log");
                sop.addStep(step);
            }
        }
        procedures.put(sop.getId(), sop);
        log.info("Extracted SOP '{}' ({} steps) from agent {}", name, sop.getSteps().size(), agentId);
        return sop;
    }

    public SopProcedure createSop(String name, String goal, List<SopStep> steps) {
        SopProcedure sop = new SopProcedure(name, goal);
        steps.forEach(sop::addStep);
        procedures.put(sop.getId(), sop);
        log.info("Created SOP '{}' with {} steps", name, steps.size());
        return sop;
    }

    public SopProcedure getSop(String id) { return procedures.get(id); }

    public List<SopProcedure> searchSops(String query) {
        String q = query.toLowerCase();
        return procedures.values().stream()
            .filter(s -> s.getName().toLowerCase().contains(q) || s.getGoal().toLowerCase().contains(q))
            .collect(Collectors.toList());
    }

    public void evaluateQuality(String sopId) {
        SopProcedure sop = procedures.get(sopId);
        if (sop == null) return;
        int score = 0;
        if (sop.getSteps().size() >= 3) score += 30;
        if (sop.getSteps().stream().allMatch(s -> !s.getExpectedOutcome().isEmpty())) score += 30;
        if (sop.getUsageCount() >= 5) score += 20;
        if (sop.getSteps().stream().allMatch(s -> !s.getRequiredTools().isEmpty())) score += 20;
        String quality = score >= 80 ? "excellent" : score >= 50 ? "good" : score >= 30 ? "fair" : "poor";
        sop.setQuality(quality);
        log.info("Evaluated SOP '{}' quality: {} (score: {})", sop.getName(), quality, score);
    }

    public List<SopProcedure> listByQuality(String minQuality) {
        int minScore = switch (minQuality) {
            case "excellent" -> 80; case "good" -> 50; case "fair" -> 30; default -> 0;
        };
        return procedures.values().stream()
            .filter(s -> {
                evaluateQuality(s.getId());
                int score = qualityToScore(s.getQuality());
                return score >= minScore;
            }).collect(Collectors.toList());
    }

    private int qualityToScore(String q) {
        return switch (q) { case "excellent" -> 80; case "good" -> 50; case "fair" -> 30; default -> 0; };
    }
}