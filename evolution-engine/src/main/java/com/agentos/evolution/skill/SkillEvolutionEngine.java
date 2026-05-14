package com.agentos.evolution.skill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SkillEvolutionEngine {
    private static final Logger log = LoggerFactory.getLogger(SkillEvolutionEngine.class);

    public static class Skill {
        private String id;
        private String name;
        private String description;
        private String prompt;
        private List<String> tools;
        private String status;
        private double score;
        private int generation;
        private String parentId;
        private int usageCount;
        private LocalDateTime createdAt;

        public Skill(String name, String description, String prompt) {
            this.id = UUID.randomUUID().toString();
            this.name = name;
            this.description = description;
            this.prompt = prompt;
            this.tools = new ArrayList<>();
            this.status = "candidate";
            this.generation = 1;
            this.createdAt = LocalDateTime.now();
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }
        public List<String> getTools() { return tools; }
        public void addTool(String tool) { tools.add(tool); }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
        public int getGeneration() { return generation; }
        public void setGeneration(int generation) { this.generation = generation; }
        public String getParentId() { return parentId; }
        public void setParentId(String parentId) { this.parentId = parentId; }
        public int getUsageCount() { return usageCount; }
        public void incrementUsage() { usageCount++; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }

    private final Map<String, Skill> skills = new ConcurrentHashMap<>();

    public Skill createSkill(String name, String description, String prompt) {
        Skill skill = new Skill(name, description, prompt);
        skills.put(skill.getId(), skill);
        log.info("Created skill candidate: {} ({})", name, skill.getId());
        return skill;
    }

    public Skill mutateSkill(String parentId, String mutationDescription) {
        Skill parent = skills.get(parentId);
        if (parent == null) return null;
        Skill child = new Skill(
            parent.getName() + " (v" + (parent.getGeneration() + 1) + ")",
            mutationDescription,
            parent.getPrompt() + "\n# Mutation\n" + mutationDescription
        );
        child.setGeneration(parent.getGeneration() + 1);
        child.setParentId(parentId);
        for (String t : parent.getTools()) {
            child.addTool(t);
        }
        skills.put(child.getId(), child);
        log.info("Mutated skill {} -> {}", parent.getName(), child.getName());
        return child;
    }

    public void evaluate(String skillId) {
        Skill skill = skills.get(skillId);
        if (skill == null) return;
        double score = 50;
        if (skill.getPrompt() != null && skill.getPrompt().length() > 100) score += 15;
        if (!skill.getTools().isEmpty()) score += 15;
        if (skill.getUsageCount() >= 10) score += 10;
        if (skill.getGeneration() >= 3) score += 10;
        skill.setScore(Math.min(100, score));
        if (score >= 70) {
            skill.setStatus("published");
        } else if (score >= 40) {
            skill.setStatus("candidate");
        } else {
            skill.setStatus("draft");
        }
        log.info("Evaluated skill '{}': score={}, status={}", skill.getName(), score, skill.getStatus());
    }

    public void sandboxValidate(String skillId) {
        Skill skill = skills.get(skillId);
        if (skill == null) return;
        boolean valid = skill.getPrompt() != null && !skill.getPrompt().isBlank()
            && skill.getTools() != null && !skill.getTools().isEmpty();
        if (valid) {
            skill.setStatus("validated");
            log.info("Skill '{}' passed sandbox validation", skill.getName());
        } else {
            skill.setStatus("failed");
            log.warn("Skill '{}' failed sandbox validation", skill.getName());
        }
    }

    public List<Skill> getEvolutionCandidates() {
        return skills.values().stream()
            .filter(s -> "validated".equals(s.getStatus()) || "published".equals(s.getStatus()))
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .collect(Collectors.toList());
    }

    public void eliminateLowPerformers(double threshold) {
        List<Skill> toRemove = skills.values().stream()
            .filter(s -> s.getScore() < threshold && s.getUsageCount() < 3)
            .toList();
        toRemove.forEach(s -> skills.remove(s.getId()));
        if (!toRemove.isEmpty()) {
            log.info("Eliminated {} low-performer skills (threshold: {})", toRemove.size(), threshold);
        }
    }

    public Skill getSkill(String id) { return skills.get(id); }
    public List<Skill> listSkills() { return List.copyOf(skills.values()); }
}