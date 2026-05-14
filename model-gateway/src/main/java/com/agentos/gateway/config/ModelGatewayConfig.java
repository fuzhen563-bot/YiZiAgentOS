package com.agentos.gateway.config;

import com.agentos.gateway.core.ModelGatewayService;
import com.agentos.gateway.core.ModelRole;
import com.agentos.gateway.core.ProviderConfig;
import com.agentos.gateway.provider.*;
import com.agentos.gateway.router.RoutingStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class ModelGatewayConfig {

    @Value("${OPENAI_API_KEY:}") private String openaiApiKey;
    @Value("${OPENAI_API_BASE:https://api.openai.com}") private String openaiApiBase;
    @Value("${OPENAI_MODEL:gpt-4o-mini}") private String openaiModel;
    @Value("${ANTHROPIC_API_KEY:}") private String claudeApiKey;
    @Value("${DASHSCOPE_API_KEY:}") private String qwenApiKey;
    @Value("${DEEPSEEK_API_KEY:}") private String deepseekApiKey;

    @Bean
    public ModelGatewayService modelGatewayService() {
        ModelGatewayService service = new ModelGatewayService();

        if (!openaiApiKey.isBlank()) {
            ProviderConfig openaiConfig = new ProviderConfig();
            openaiConfig.setName("openai");
            openaiConfig.setApiKey(openaiApiKey);
            openaiConfig.setEndpoint(openaiApiBase);
            openaiConfig.setCostPer1kTokens(0.002);
            openaiConfig.setModels(Map.of(
                openaiModel, modelConfig(openaiModel, "Default Model", ModelRole.CHAT, 128000, 0.001, 0.002)
            ));
            service.registerProvider(new OpenAIProvider(openaiConfig), List.of(openaiModel));
        }

        if (!claudeApiKey.isBlank()) {
            ProviderConfig claudeConfig = new ProviderConfig();
            claudeConfig.setName("claude");
            claudeConfig.setApiKey(claudeApiKey);
            claudeConfig.setEndpoint("https://api.anthropic.com");
            claudeConfig.setCostPer1kTokens(0.003);
            claudeConfig.setModels(Map.of(
                "claude-sonnet-4", modelConfig("claude-sonnet-4", "Claude Sonnet 4", ModelRole.CHAT, 200000, 0.003, 0.015),
                "claude-haiku-3.5", modelConfig("claude-haiku-3.5", "Claude Haiku 3.5", ModelRole.CHAT, 200000, 0.0008, 0.004)
            ));
            service.registerProvider(new ClaudeProvider(claudeConfig), List.of("claude-sonnet-4", "claude-haiku-3.5"));
        }

        if (!qwenApiKey.isBlank()) {
            ProviderConfig qwenConfig = new ProviderConfig();
            qwenConfig.setName("qwen");
            qwenConfig.setApiKey(qwenApiKey);
            qwenConfig.setEndpoint("https://dashscope.aliyuncs.com");
            qwenConfig.setCostPer1kTokens(0.001);
            qwenConfig.setModels(Map.of(
                "qwen-max", modelConfig("qwen-max", "Qwen Max", ModelRole.CHAT, 32000, 0.002, 0.006),
                "qwen-plus", modelConfig("qwen-plus", "Qwen Plus", ModelRole.CHAT, 32000, 0.0008, 0.002)
            ));
            service.registerProvider(new QwenProvider(qwenConfig), List.of("qwen-max", "qwen-plus"));
        }

        if (!deepseekApiKey.isBlank()) {
            ProviderConfig deepseekConfig = new ProviderConfig();
            deepseekConfig.setName("deepseek");
            deepseekConfig.setApiKey(deepseekApiKey);
            deepseekConfig.setEndpoint("https://api.deepseek.com");
            deepseekConfig.setCostPer1kTokens(0.0005);
            deepseekConfig.setModels(Map.of(
                "deepseek-chat", modelConfig("deepseek-chat", "DeepSeek Chat", ModelRole.CHAT, 64000, 0.00027, 0.0011),
                "deepseek-reasoner", modelConfig("deepseek-reasoner", "DeepSeek Reasoner", ModelRole.CHAT, 64000, 0.00055, 0.00219)
            ));
            service.registerProvider(new DeepSeekProvider(deepseekConfig), List.of("deepseek-chat", "deepseek-reasoner"));
        }

        service.getRouter().setStrategy("gpt-4o", RoutingStrategy.SPEED_PRIORITY);
        service.getRouter().setStrategy("deepseek-chat", RoutingStrategy.COST_PRIORITY);
        service.getRouter().setStrategy("qwen-max", RoutingStrategy.HYBRID);

        return service;
    }

    private ProviderConfig.ModelConfig modelConfig(String id, String displayName, ModelRole role,
                                                    int contextWindow, double inputCost, double outputCost) {
        ProviderConfig.ModelConfig mc = new ProviderConfig.ModelConfig();
        mc.setId(id);
        mc.setDisplayName(displayName);
        mc.setRole(role);
        mc.setContextWindow(contextWindow);
        mc.setCostPer1kInputTokens(inputCost);
        mc.setCostPer1kOutputTokens(outputCost);
        return mc;
    }
}