package com.agentos.runtime.multiagent;

import com.agentos.runtime.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

public class MultiAgentOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(MultiAgentOrchestrator.class);
    private final AgentRegistry registry;

    public MultiAgentOrchestrator(AgentRegistry registry) {
        this.registry = registry;
    }

    public AgentResponse delegateTo(Agent agent, AgentRequest request) {
        try {
            return agent.execute(request).get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            return AgentResponse.failure(agent.getId(), "Delegation failed: " + e.getMessage());
        }
    }

    public List<AgentResponse> broadcast(AgentRequest request) {
        List<CompletableFuture<AgentResponse>> futures = registry.listAgents().stream()
            .map(a -> registry.getAgent((String) a.get("id")))
            .filter(Objects::nonNull)
            .map(a -> a.execute(request))
            .toList();
        return futures.stream().map(f -> {
            try { return f.get(30, TimeUnit.SECONDS); }
            catch (Exception e) { return AgentResponse.failure("unknown", "Timeout"); }
        }).toList();
    }

    public AgentResponse collaborate(String primaryAgentId, String task, List<String> collaboratorIds) {
        Agent primary = registry.getAgent(primaryAgentId);
        if (primary == null) return AgentResponse.failure(primaryAgentId, "Agent not found");

        Map<String, Object> context = new HashMap<>();
        context.put("task", task);
        context.put("collaborators", collaboratorIds);

        StringBuilder summary = new StringBuilder();
        for (String collabId : collaboratorIds) {
            Agent collab = registry.getAgent(collabId);
            if (collab == null) continue;

            AgentRequest req = new AgentRequest(
                "Collaborate on task: " + task + ". Provide your expertise as " + collab.getRole(),
                "system");
            try {
                AgentResponse resp = collab.execute(req).get(30, TimeUnit.SECONDS);
                summary.append(collab.getName()).append(": ").append(resp.getMessage()).append("\n");
            } catch (Exception e) {
                summary.append(collab.getName()).append(": error - ").append(e.getMessage()).append("\n");
            }
        }

        AgentRequest finalReq = new AgentRequest(
            "Synthesize these responses into a final answer:\n" + summary.toString(), "system");
        finalReq.setContext(context);
        try {
            return primary.execute(finalReq).get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            return AgentResponse.failure(primaryAgentId, "Collaboration failed: " + e.getMessage());
        }
    }
}