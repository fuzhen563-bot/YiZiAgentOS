package com.agentos.gateway.provider;

import com.agentos.gateway.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractModelProvider implements ModelProvider {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final ProviderConfig config;
    protected final RestTemplate restTemplate;
    protected final ModelHealth health;

    protected AbstractModelProvider(ProviderConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
        this.health = new ModelHealth(config.getName());
    }

    @Override
    public boolean isAvailable() {
        return health.isHealthy();
    }

    @Override
    public ModelHealth checkHealth() {
        long start = System.currentTimeMillis();
        try {
            doHealthCheck();
            health.markHealthy(System.currentTimeMillis() - start);
        } catch (Exception e) {
            health.markUnhealthy(e.getMessage());
            log.warn("Health check failed for provider {}: {}", config.getName(), e.getMessage());
        }
        return health;
    }

    @Override
    public CompletableFuture<ModelResponse> callAsync(ModelRequest request) {
        return CompletableFuture.supplyAsync(() -> call(request));
    }

    @Override
    public ProviderConfig getConfig() {
        return config;
    }

    protected abstract void doHealthCheck();

    protected HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());
        return headers;
    }
}