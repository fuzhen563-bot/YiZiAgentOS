package com.agentos.runtime.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface Agent {
    String getId();
    String getName();
    String getRole();
    AgentState getState();
    CompletableFuture<AgentResponse> execute(AgentRequest request);
    void interrupt();
    void resume();
    void reset();
    void updateMemory(String key, Object value);
    Map<String, Object> getMemory();
}