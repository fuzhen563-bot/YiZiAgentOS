package com.agentos.secure.browser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BrowserSession {
    private static final Logger log = LoggerFactory.getLogger(BrowserSession.class);
    private final String id;
    private final Map<String, Object> state = new ConcurrentHashMap<>();
    private final long createdAt;
    private volatile boolean active;

    public BrowserSession(String id) {
        this.id = id;
        this.createdAt = System.currentTimeMillis();
        this.active = true;
    }

    public String getId() { return id; }
    public boolean isActive() { return active; }
    public void close() { this.active = false; }
    public long getCreatedAt() { return createdAt; }
    public void putState(String key, Object value) { state.put(key, value); }
    @SuppressWarnings("unchecked")
    public <T> T getState(String key) { return (T) state.get(key); }
    public Map<String, Object> getAllState() { return new HashMap<>(state); }
}