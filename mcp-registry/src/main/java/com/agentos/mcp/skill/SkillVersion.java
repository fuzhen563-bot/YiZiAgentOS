package com.agentos.mcp.skill;

import java.time.LocalDateTime;

public class SkillVersion {
    private String skillId;
    private String version;
    private String changelog;
    private String manifestContent;
    private LocalDateTime createdAt;

    public SkillVersion(String skillId, String version, String changelog, String manifestContent) {
        this.skillId = skillId;
        this.version = version;
        this.changelog = changelog;
        this.manifestContent = manifestContent;
        this.createdAt = LocalDateTime.now();
    }

    public String getSkillId() { return skillId; }
    public String getVersion() { return version; }
    public String getChangelog() { return changelog; }
    public String getManifestContent() { return manifestContent; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}