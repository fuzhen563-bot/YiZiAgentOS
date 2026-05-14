package com.agentos.runtime.core;

import com.agentos.runtime.reasoning.PlanExecuteAgent;
import com.agentos.runtime.reasoning.ReActAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AgentRegistry {
    private static final Logger log = LoggerFactory.getLogger(AgentRegistry.class);
    private final Map<String, Agent> agents = new ConcurrentHashMap<>();
    private final Map<String, String> routingRules = new ConcurrentHashMap<>();

    public void register(Agent agent) {
        agents.put(agent.getId(), agent);
        log.info("Registered agent: {} ({}) - {}", agent.getName(), agent.getId(), agent.getRole());
    }

    public Agent getAgent(String id) { return agents.get(id); }

    public Agent route(String intent) {
        for (var entry : routingRules.entrySet()) {
            if (intent.toLowerCase().contains(entry.getKey().toLowerCase())) {
                Agent agent = agents.get(entry.getValue());
                if (agent != null) return agent;
            }
        }
        return agents.values().stream().findFirst().orElse(null);
    }

    public void addRoutingRule(String intentKeyword, String agentId) {
        routingRules.put(intentKeyword, agentId);
    }

    public List<Map<String, Object>> listAgents() {
        return agents.values().stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("name", a.getName());
            m.put("role", a.getRole());
            m.put("state", a.getState());
            return m;
        }).toList();
    }

    public void initDefaultAgents() {
        int seq = 1;
        register(new ReActAgent("agent-" + seq++, "SalesBot", "Sales Representative"));
        register(new PlanExecuteAgent("agent-" + seq++, "HRBot", "HR Assistant"));
        register(new ReActAgent("agent-" + seq++, "FinanceBot", "Financial Analyst"));
        register(new PlanExecuteAgent("agent-" + seq++, "LegalBot", "Legal Counsel"));
        register(new ReActAgent("agent-" + seq++, "SupportBot", "Customer Support"));

        addRoutingRule("sale", "agent-1");
        addRoutingRule("hr", "agent-2");
        addRoutingRule("finance", "agent-3");
        addRoutingRule("legal", "agent-4");
        addRoutingRule("support", "agent-5");
    }
}