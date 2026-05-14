package com.agentos.gateway.billing;

import com.agentos.gateway.core.ModelResponse;
import com.agentos.gateway.core.ProviderConfig;

import java.util.Map;

public class CostCalculator {

    public double calculate(ModelResponse response, ProviderConfig config) {
        if (config == null) return 0;
        Map<String, ProviderConfig.ModelConfig> models = config.getModels();
        if (models != null) {
            ProviderConfig.ModelConfig modelConfig = models.get(response.getModelId());
            if (modelConfig != null) {
                double inputCost = response.getPromptTokens() * modelConfig.getCostPer1kInputTokens() / 1000.0;
                double outputCost = response.getCompletionTokens() * modelConfig.getCostPer1kOutputTokens() / 1000.0;
                return inputCost + outputCost;
            }
        }
        return response.getTotalTokens() * config.getCostPer1kTokens() / 1000.0;
    }
}