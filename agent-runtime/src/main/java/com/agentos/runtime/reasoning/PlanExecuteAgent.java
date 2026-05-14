package com.agentos.runtime.reasoning;

import com.agentos.runtime.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PlanExecuteAgent extends BaseAgent {
    private static final Logger log = LoggerFactory.getLogger(PlanExecuteAgent.class);

    public PlanExecuteAgent(String id, String name, String role) {
        super(id, name, role);
    }

    private boolean isChinese(String text) {
        return text != null && text.codePoints().anyMatch(cp -> Character.UnicodeScript.of(cp) == Character.UnicodeScript.HAN);
    }

    @Override
    public CompletableFuture<AgentResponse> execute(AgentRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            long start = System.currentTimeMillis();
            AgentResponse response = AgentResponse.success(getId(), "");
            state = AgentState.THINKING;

            String langInstr = isChinese(request.getMessage())
                ? "\n\nIMPORTANT: The user wrote in Chinese. You MUST respond in Chinese (简体中文)."
                : "\n\nIMPORTANT: Respond in the same language as the user's message.";

            String systemPrompt = "You are " + name + ", " + role
                + ". Break down the task into steps and execute each one.\n"
                + "Format:\nPlan: step-by-step plan\nStep 1: do something\n"
                + "Result: outcome\nStep 2: ...\nSummary: final answer"
                + langInstr;

            String llmResponse = LlmClient.chat(systemPrompt + "\n\nTask: " + request.getMessage());
            response.addThought(llmResponse);
            String summary = extractSummary(llmResponse);
            response.setMessage(summary != null ? summary : llmResponse);

            state = AgentState.COMPLETED;
            response.setDurationMs(System.currentTimeMillis() - start);
            response.setSteps(1);
            return response;
        });
    }

    private String extractSummary(String text) {
        if (text == null) return null;
        int idx = text.indexOf("Summary:");
        if (idx < 0) {
            idx = text.indexOf("Final Answer:");
            if (idx < 0) return null;
            return text.substring(idx + 13).trim();
        }
        return text.substring(idx + 8).trim();
    }
}