package com.agentos.runtime.core;

import java.util.*;

public class AgentResponse {
    private String agentId;
    private String message;
    private String status;
    private List<String> thoughts;
    private List<ToolCall> toolCalls;
    private Map<String, Object> output;
    private long durationMs;
    private int steps;

    public AgentResponse() {}

    public AgentResponse(String agentId, String message, String status) {
        this.agentId = agentId;
        this.message = message;
        this.status = status;
        this.thoughts = new ArrayList<>();
        this.toolCalls = new ArrayList<>();
        this.output = new HashMap<>();
    }

    public static AgentResponse success(String agentId, String message) {
        return new AgentResponse(agentId, message, "success");
    }

    public static AgentResponse failure(String agentId, String error) {
        return new AgentResponse(agentId, error, "failure");
    }

    public String getAgentId() { return agentId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStatus() { return status; }
    public List<String> getThoughts() { return thoughts; }
    public void addThought(String thought) { thoughts.add(thought); }
    public List<ToolCall> getToolCalls() { return toolCalls; }
    public void addToolCall(ToolCall call) { toolCalls.add(call); }
    public Map<String, Object> getOutput() { return output; }
    public void setOutput(Map<String, Object> output) { this.output = output; }
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
    public int getSteps() { return steps; }
    public void setSteps(int steps) { this.steps = steps; }

    public static class ToolCall {
        private String toolName;
        private Map<String, Object> arguments;
        private Map<String, Object> result;

        public ToolCall(String toolName, Map<String, Object> arguments) {
            this.toolName = toolName;
            this.arguments = arguments;
        }

        public String getToolName() { return toolName; }
        public Map<String, Object> getArguments() { return arguments; }
        public Map<String, Object> getResult() { return result; }
        public void setResult(Map<String, Object> result) { this.result = result; }
    }
}