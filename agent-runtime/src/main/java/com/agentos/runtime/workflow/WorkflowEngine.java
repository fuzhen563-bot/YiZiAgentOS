package com.agentos.runtime.workflow;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class WorkflowEngine {
    private final Map<String, WorkflowDefinition> workflows = new LinkedHashMap<>();

    public static class WorkflowStep {
        private String id;
        private String name;
        private String type;
        private String agentId;
        private Map<String, Object> config;
        private List<String> dependsOn;
        private String condition;

        public WorkflowStep() {}

        public WorkflowStep(String id, String name, String type, String agentId) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.agentId = agentId;
            this.config = new HashMap<>();
            this.dependsOn = new ArrayList<>();
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getType() { return type; }
        public String getAgentId() { return agentId; }
        public Map<String, Object> getConfig() { return config; }
        public void setConfig(Map<String, Object> config) { this.config = config; }
        public List<String> getDependsOn() { return dependsOn; }
        public void setDependsOn(List<String> dependsOn) { this.dependsOn = dependsOn; }
        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
    }

    public static class WorkflowDefinition {
        private String id;
        private String name;
        private String description;
        private List<WorkflowStep> steps;
        private Map<String, Object> context;

        public WorkflowDefinition(String name, String description) {
            this.id = UUID.randomUUID().toString();
            this.name = name;
            this.description = description;
            this.steps = new ArrayList<>();
            this.context = new HashMap<>();
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public List<WorkflowStep> getSteps() { return steps; }
        public void addStep(WorkflowStep step) { steps.add(step); }
        public Map<String, Object> getContext() { return context; }
    }

    public WorkflowDefinition createWorkflow(String name, String description) {
        WorkflowDefinition wf = new WorkflowDefinition(name, description);
        workflows.put(wf.getId(), wf);
        return wf;
    }

    public WorkflowDefinition getWorkflow(String id) { return workflows.get(id); }

    public WorkflowStep addStep(String workflowId, String name, String type, String agentId) {
        WorkflowDefinition wf = workflows.get(workflowId);
        if (wf == null) return null;
        int seq = wf.getSteps().size() + 1;
        WorkflowStep step = new WorkflowStep("step-" + seq, name, type, agentId);
        wf.addStep(step);
        return step;
    }

    public Map<String, Object> execute(String workflowId) {
        WorkflowDefinition wf = workflows.get(workflowId);
        if (wf == null) return Map.of("error", "Workflow not found");

        Map<String, Object> results = new LinkedHashMap<>();
        results.put("workflow", wf.getName());
        results.put("status", "running");

        Set<String> completed = new HashSet<>();
        List<WorkflowStep> remaining = new ArrayList<>(wf.getSteps());

        int maxIterations = wf.getSteps().size() * 2;
        int iterations = 0;

        while (!remaining.isEmpty() && iterations < maxIterations) {
            List<WorkflowStep> ready = remaining.stream()
                .filter(s -> s.getDependsOn().isEmpty() || completed.containsAll(s.getDependsOn()))
                .toList();

            if (ready.isEmpty()) {
                results.put("error", "Deadlock detected in workflow");
                results.put("status", "failed");
                return results;
            }

            List<String> completedIds = new ArrayList<>();
            for (WorkflowStep step : ready) {
                Map<String, Object> stepResult = new LinkedHashMap<>();
                stepResult.put("name", step.getName());
                stepResult.put("type", step.getType());

                if ("parallel".equals(step.getType())) {
                    List<CompletableFuture<Void>> futures = new ArrayList<>();
                    Map<String, Object> parallelResults = new LinkedHashMap<>();
                    List<String> subSteps = (List<String>) step.getConfig().getOrDefault("steps", List.of());
                    for (String sub : subSteps) {
                        parallelResults.put(sub, "completed");
                    }
                    stepResult.put("result", parallelResults);
                } else {
                    stepResult.put("result", "executed by " + step.getAgentId());
                }
                results.put(step.getId(), stepResult);
                completed.add(step.getId());
                completedIds.add(step.getId());
            }
            remaining.removeIf(s -> completedIds.contains(s.getId()));
            iterations++;
        }

        results.put("status", remaining.isEmpty() ? "completed" : "partial");
        return results;
    }
}