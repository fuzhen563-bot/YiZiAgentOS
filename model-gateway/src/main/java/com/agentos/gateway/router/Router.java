package com.agentos.gateway.router;

import com.agentos.gateway.core.ModelProvider;
import com.agentos.gateway.core.ModelRequest;
import java.util.List;

public interface Router {
    ModelProvider route(List<ModelProvider> candidates, ModelRequest request);
}