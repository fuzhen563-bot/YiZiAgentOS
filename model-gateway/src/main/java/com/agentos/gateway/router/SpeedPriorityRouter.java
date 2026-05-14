package com.agentos.gateway.router;

import com.agentos.gateway.core.ModelProvider;
import com.agentos.gateway.core.ModelRequest;
import java.util.Comparator;
import java.util.List;

public class SpeedPriorityRouter implements Router {

    @Override
    public ModelProvider route(List<ModelProvider> candidates, ModelRequest request) {
        return candidates.stream()
            .filter(ModelProvider::isAvailable)
            .min(Comparator.comparingLong(p -> {
                long ms = p.checkHealth().getLatencyMs();
                return ms;
            }))
            .orElse(null);
    }
}