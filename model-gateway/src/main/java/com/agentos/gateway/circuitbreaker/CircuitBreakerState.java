package com.agentos.gateway.circuitbreaker;

public enum CircuitBreakerState {
    CLOSED,
    OPEN,
    HALF_OPEN
}