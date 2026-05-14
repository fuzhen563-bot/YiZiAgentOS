package com.agentos.gateway.router;

import com.agentos.gateway.core.ModelProvider;
import com.agentos.gateway.core.ModelRequest;
import java.util.Comparator;
import java.util.List;

public class CostPriorityRouter implements Router {

    @Override
    public ModelProvider route(List<ModelProvider> candidates, ModelRequest request) {
        return candidates.stream()
            .filter(ModelProvider::isAvailable)
            .min(Comparator.comparingDouble(p -> p.getConfig().getCostPer1kTokens()))
            .orElse(null);
    }
}