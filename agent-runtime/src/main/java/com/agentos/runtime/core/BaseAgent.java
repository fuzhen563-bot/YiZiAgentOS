package com.agentos.runtime.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

public abstract class BaseAgent implements Agent {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final String id;
    protected final String name;
    protected final String role;
    protected volatile AgentState state;
    protected final Map<String, Object> memory;
    protected volatile boolean interrupted;

    protected BaseAgent(String id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.state = AgentState.IDLE;
        this.memory = new ConcurrentHashMap<>();
        this.interrupted = false;
    }

    @Override
    public String getId() { return id; }
    @Override
    public String getName() { return name; }
    @Override
    public String getRole() { return role; }
    @Override
    public AgentState getState() { return state; }

    @Override
    public void interrupt() {
        this.interrupted = true;
        this.state = AgentState.INTERRUPTED;
        log.info("Agent {} interrupted", name);
    }

    @Override
    public void resume() {
        this.interrupted = false;
        this.state = AgentState.THINKING;
        log.info("Agent {} resumed", name);
    }

    @Override
    public void reset() {
        this.state = AgentState.IDLE;
        this.memory.clear();
        this.interrupted = false;
        log.info("Agent {} reset", name);
    }

    @Override
    public void updateMemory(String key, Object value) {
        memory.put(key, value);
    }

    @Override
    public Map<String, Object> getMemory() {
        return Map.copyOf(memory);
    }
}