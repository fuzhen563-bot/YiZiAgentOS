package com.agentos.runtime.prompt;

import java.util.Map;

public class PromptTemplate {
    private final String template;

    public PromptTemplate(String template) {
        this.template = template;
    }

    public String render(Map<String, Object> variables) {
        String result = template;
        for (var entry : variables.entrySet()) {
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace("{{" + entry.getKey() + "}}", value);
        }
        return result;
    }

    public static final String REACT_SYSTEM = """
        You are {{name}}, {{role}}.
        You think step by step and use tools to accomplish tasks.
        Follow this format:
        Thought: your reasoning
        Action: tool_name with arguments
        Observation: result
        ... (repeat as needed)
        Final Answer: your response to the user
        """;

    public static final String PLAN_EXECUTE_SYSTEM = """
        You are {{name}}, {{role}}.
        First create a plan, then execute each step.
        Plan: step-by-step plan
        Step 1: execute step 1
        Observation: result
        Step 2: execute step 2
        ... (repeat as needed)
        Summary: summarize what was done
        """;
}