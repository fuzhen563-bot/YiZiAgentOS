package com.agentos.mcp.skill;

import java.time.LocalDateTime;
import java.util.*;

public class SkillDefinition {
    private String id;
    private String name;
    private String version;
    private String description;
    private String manifestPath;
    private List<String> tools;
    private List<String> dependencies;
    private Map<String, Object> config;
    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SkillDefinition() {}

    public SkillDefinition(String name, String version, String description) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.version = version;
        this.description = description;
        this.tools = new ArrayList<>();
        this.dependencies = new ArrayList<>();
        this.config = new HashMap<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getManifestPath() { return manifestPath; }
    public void setManifestPath(String manifestPath) { this.manifestPath = manifestPath; }
    public List<String> getTools() { return tools; }
    public void setTools(List<String> tools) { this.tools = tools; }
    public void addTool(String tool) { this.tools.add(tool); }
    public List<String> getDependencies() { return dependencies; }
    public void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }
    public Map<String, Object> getConfig() { return config; }
    public void setConfig(Map<String, Object> config) { this.config = config; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}