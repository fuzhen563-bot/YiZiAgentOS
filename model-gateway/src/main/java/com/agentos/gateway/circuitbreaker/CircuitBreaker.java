package com.agentos.gateway.circuitbreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class CircuitBreaker {
    private static final Logger log = LoggerFactory.getLogger(CircuitBreaker.class);
    private final String name;
    private final int failureThreshold;
    private final long halfOpenTimeoutMs;
    private final AtomicReference<CircuitBreakerState> state;
    private final AtomicInteger failureCount;
    private final AtomicLong lastFailureTime;

    public CircuitBreaker(String name, int failureThreshold, long halfOpenTimeoutMs) {
        this.name = name;
        this.failureThreshold = failureThreshold;
        this.halfOpenTimeoutMs = halfOpenTimeoutMs;
        this.state = new AtomicReference<>(CircuitBreakerState.CLOSED);
        this.failureCount = new AtomicInteger(0);
        this.lastFailureTime = new AtomicLong(0);
    }

    public boolean isAllowed() {
        CircuitBreakerState current = state.get();
        if (current == CircuitBreakerState.CLOSED) {
            return true;
        }
        if (current == CircuitBreakerState.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime.get() > halfOpenTimeoutMs) {
                if (state.compareAndSet(CircuitBreakerState.OPEN, CircuitBreakerState.HALF_OPEN)) {
                    log.info("Circuit breaker {} transitioned OPEN -> HALF_OPEN", name);
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public void onSuccess() {
        if (state.get() == CircuitBreakerState.HALF_OPEN) {
            state.set(CircuitBreakerState.CLOSED);
            failureCount.set(0);
            log.info("Circuit breaker {} reset to CLOSED", name);
        }
        failureCount.set(0);
    }

    public void onFailure() {
        int failures = failureCount.incrementAndGet();
        lastFailureTime.set(System.currentTimeMillis());
        if (failures >= failureThreshold) {
            state.set(CircuitBreakerState.OPEN);
            log.warn("Circuit breaker {} OPEN after {} failures", name, failures);
        }
    }

    public CircuitBreakerState getState() { return state.get(); }
    public int getFailureCount() { return failureCount.get(); }
    public String getName() { return name; }
}