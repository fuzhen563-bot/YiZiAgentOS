package com.agentos.gateway.core;

import java.util.concurrent.CompletableFuture;

public interface ModelProvider {
    String getName();
    boolean isAvailable();
    ModelResponse call(ModelRequest request);
    CompletableFuture<ModelResponse> callAsync(ModelRequest request);
    ModelHealth checkHealth();
    ProviderConfig getConfig();
}