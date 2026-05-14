package com.agentos.mcp.registry;

import java.util.Map;

public class ToolMetadata {
    private String category;
    private String description;
    private Map<String, Object> parameters;
    private String returnType;
    private int estimatedCost;
    private String riskLevel;
    private boolean requiresApproval;
    private Map<String, Object> examples;

    public ToolMetadata() {}

    public ToolMetadata(String category, String description, String riskLevel) {
        this.category = category;
        this.description = description;
        this.riskLevel = riskLevel;
        this.estimatedCost = 1;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    public String getReturnType() { return returnType; }
    public void setReturnType(String returnType) { this.returnType = returnType; }
    public int getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(int estimatedCost) { this.estimatedCost = estimatedCost; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public boolean isRequiresApproval() { return requiresApproval; }
    public void setRequiresApproval(boolean requiresApproval) { this.requiresApproval = requiresApproval; }
    public Map<String, Object> getExamples() { return examples; }
    public void setExamples(Map<String, Object> examples) { this.examples = examples; }

    public static ToolMetadata forConnector(String name) {
        return switch (name) {
            case "github" -> new ToolMetadata("code", "GitHub code operations", "medium");
            case "email" -> new ToolMetadata("communication", "Send and read emails", "high");
            case "calendar" -> new ToolMetadata("productivity", "Calendar management", "medium");
            case "crm" -> new ToolMetadata("business", "CRM data operations", "high");
            case "erp" -> new ToolMetadata("business", "ERP system operations", "critical");
            default -> new ToolMetadata("general", name + " operations", "medium");
        };
    }
}