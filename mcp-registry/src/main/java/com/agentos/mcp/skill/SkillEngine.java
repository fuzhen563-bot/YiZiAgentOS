package com.agentos.mcp.skill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SkillEngine {
    private static final Logger log = LoggerFactory.getLogger(SkillEngine.class);
    private final Map<String, SkillDefinition> skills = new ConcurrentHashMap<>();

    public SkillDefinition upload(String name, String version, String description, String manifestContent) {
        SkillDefinition skill = new SkillDefinition(name, version, description);
        skill.setManifestPath(manifestContent);
        skills.put(skill.getId(), skill);
        log.info("Uploaded skill: {} v{}", name, version);
        return skill;
    }

    public SkillDefinition getSkill(String id) {
        return skills.get(id);
    }

    public List<SkillDefinition> searchSkills(String query) {
        String q = query.toLowerCase();
        return skills.values().stream()
            .filter(s -> s.getName().toLowerCase().contains(q)
                || s.getDescription().toLowerCase().contains(q))
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .collect(Collectors.toList());
    }

    public SkillDefinition updateSkill(String id, String name, String version, String description) {
        SkillDefinition skill = skills.get(id);
        if (skill == null) return null;
        if (name != null) skill.setName(name);
        if (version != null) skill.setVersion(version);
        if (description != null) skill.setDescription(description);
        skill.setUpdatedAt(java.time.LocalDateTime.now());
        log.info("Updated skill: {} v{}", skill.getName(), skill.getVersion());
        return skill;
    }

    public boolean deleteSkill(String id) {
        SkillDefinition removed = skills.remove(id);
        if (removed != null) log.info("Deleted skill: {}", removed.getName());
        return removed != null;
    }

    public List<SkillDefinition> listSkills() {
        return skills.values().stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .collect(Collectors.toList());
    }

    public Map<String, Object> getStats() {
        return Map.of("total_skills", skills.size());
    }
}