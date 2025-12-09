package com.petvet.rag.app.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * LangChain4j 配置类
 * 
 * 配置 LangChain4j 的 ChatLanguageModel
 * 支持多种 AI 提供商：OpenAI、DeepSeek、xAI Grok 等
 * 
 * DeepSeek 和 xAI Grok 都使用 OpenAI 兼容的 API，可以通过设置 baseUrl 来使用
 * 
 * @author PetVetRAG Team
 */
@Configuration
@Slf4j
public class LangChainConfig {

    @Value("${spring.ai.provider.type:deepseek}")
    private String providerType;

    @Value("${spring.ai.openai.api-key:${OPENAI_API_KEY:}}")
    private String openAiApiKey;

    @Value("${spring.ai.openai.chat.options.model:gpt-4o}")
    private String openAiModel;

    @Value("${spring.ai.openai.chat.options.temperature:0.7}")
    private Double temperature;

    @Value("${spring.ai.deepseek.api-key:${DEEPSEEK_API_KEY:}}")
    private String deepSeekApiKey;

    @Value("${spring.ai.deepseek.base-url:https://api.deepseek.com}")
    private String deepSeekBaseUrl;

    @Value("${spring.ai.deepseek.chat.options.model:deepseek-chat}")
    private String deepSeekModel;

    @Value("${spring.ai.deepseek.chat.options.temperature:0.7}")
    private Double deepSeekTemperature;

    @Value("${spring.ai.grok.api-key:${GROK_API_KEY:}}")
    private String grokApiKey;

    @Value("${spring.ai.grok.base-url:https://api.x.ai/v1}")
    private String grokBaseUrl;

    @Value("${spring.ai.grok.chat.options.model:grok-4-latest}")
    private String grokModel;

    @Value("${spring.ai.grok.chat.options.temperature:0.7}")
    private Double grokTemperature;

    /**
     * 创建 ChatLanguageModel
     * 根据配置自动选择 OpenAI、DeepSeek 或 Grok
     * 如果指定的 provider 没有 API key，会自动尝试其他可用的 provider
     * 
     * @return ChatLanguageModel 实例
     */
    @Bean
    @Primary
    public ChatLanguageModel chatLanguageModel() {
        log.info("==========================================");
        log.info("RAG AI 配置信息:");
        log.info("  配置的 Provider: {}", providerType);
        log.info("  Grok API Key: {}", isNotEmpty(grokApiKey) ? "已设置 (" + maskApiKey(grokApiKey) + ")" : "未设置");
        log.info("  DeepSeek API Key: {}", isNotEmpty(deepSeekApiKey) ? "已设置 (" + maskApiKey(deepSeekApiKey) + ")" : "未设置");
        log.info("  OpenAI API Key: {}", isNotEmpty(openAiApiKey) ? "已设置 (" + maskApiKey(openAiApiKey) + ")" : "未设置");
        log.info("==========================================");
        
        // 优先使用配置指定的 provider
        if ("grok".equalsIgnoreCase(providerType)) {
            if (isNotEmpty(grokApiKey)) {
                log.info("✅ 使用 Grok AI provider");
                return createGrokModel();
            }
        } else if ("deepseek".equalsIgnoreCase(providerType)) {
            if (isNotEmpty(deepSeekApiKey)) {
                log.info("✅ 使用 DeepSeek AI provider");
                return createDeepSeekModel();
            }
        } else if ("openai".equalsIgnoreCase(providerType)) {
            if (isNotEmpty(openAiApiKey)) {
                log.info("✅ 使用 OpenAI AI provider");
                return createOpenAiModel();
            }
        }
        
        // 如果指定的 provider 没有 API key，尝试其他可用的 provider
        if (isNotEmpty(grokApiKey)) {
            log.warn("⚠️  警告: 配置的 AI provider ({}) 没有 API key，自动切换到 Grok", providerType);
            return createGrokModel();
        }
        if (isNotEmpty(deepSeekApiKey)) {
            log.warn("⚠️  警告: 配置的 AI provider ({}) 没有 API key，自动切换到 DeepSeek", providerType);
            return createDeepSeekModel();
        }
        if (isNotEmpty(openAiApiKey)) {
            log.warn("⚠️  警告: 配置的 AI provider ({}) 没有 API key，自动切换到 OpenAI", providerType);
            return createOpenAiModel();
        }
        
        // 如果都没有 API key，抛出清晰的错误
        throw new IllegalStateException(
            "❌ 错误: 没有配置任何 AI API Key！\n" +
            "\n" +
            "配置检查结果：\n" +
            "  - spring.ai.provider.type = " + providerType + "\n" +
            "  - spring.ai.grok.api-key = " + (isNotEmpty(grokApiKey) ? "已设置" : "未设置") + "\n" +
            "  - spring.ai.deepseek.api-key = " + (isNotEmpty(deepSeekApiKey) ? "已设置" : "未设置") + "\n" +
            "  - spring.ai.openai.api-key = " + (isNotEmpty(openAiApiKey) ? "已设置" : "未设置") + "\n" +
            "\n" +
            "解决方案：\n" +
            "1. 如果使用 Nacos 配置，请确保在 Nacos 中配置了：\n" +
            "   spring.ai.provider.type = deepseek\n" +
            "   spring.ai.deepseek.api-key = your-api-key-here\n" +
            "\n" +
            "2. 如果使用环境变量，请设置：\n" +
            "   export DEEPSEEK_API_KEY=your-api-key-here\n" +
            "   export AI_PROVIDER_TYPE=deepseek\n" +
            "\n" +
            "3. 检查 Nacos 是否连接成功，查看启动日志中的 Nacos 配置加载信息"
        );
    }
    
    /**
     * 根据模型名称创建 ChatLanguageModel
     * 用于动态选择不同的模型
     * 
     * @param modelName 模型名称（openai, deepseek, grok）
     * @return ChatLanguageModel 实例
     */
    public ChatLanguageModel createModelByName(String modelName) {
        if (modelName == null || modelName.trim().isEmpty()) {
            return chatLanguageModel(); // 使用默认模型
        }
        
        String lowerModelName = modelName.toLowerCase();
        if (lowerModelName.contains("grok")) {
            if (isNotEmpty(grokApiKey)) {
                return createGrokModel();
            }
        } else if (lowerModelName.contains("deepseek")) {
            if (isNotEmpty(deepSeekApiKey)) {
                return createDeepSeekModel();
            }
        } else if (lowerModelName.contains("openai") || lowerModelName.contains("gpt")) {
            if (isNotEmpty(openAiApiKey)) {
                return createOpenAiModel();
            }
        }
        
        // 如果指定的模型不可用，使用默认模型
        log.warn("指定的模型 {} 不可用，使用默认模型", modelName);
        return chatLanguageModel();
    }
    
    /**
     * 掩码 API Key，只显示前后几位
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }
    
    private boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * 创建 DeepSeek ChatLanguageModel
     * DeepSeek 使用 OpenAI 兼容的 API
     */
    private ChatLanguageModel createDeepSeekModel() {
        return OpenAiChatModel.builder()
                .apiKey(deepSeekApiKey)
                .baseUrl(deepSeekBaseUrl)
                .modelName(deepSeekModel)
                .temperature(deepSeekTemperature)
                .build();
    }

    /**
     * 创建 OpenAI ChatLanguageModel
     */
    private ChatLanguageModel createOpenAiModel() {
        return OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName(openAiModel)
                .temperature(temperature)
                .build();
    }

    /**
     * 创建 xAI Grok ChatLanguageModel
     * Grok 使用 OpenAI 兼容的 API
     */
    private ChatLanguageModel createGrokModel() {
        return OpenAiChatModel.builder()
                .apiKey(grokApiKey)
                .baseUrl(grokBaseUrl)
                .modelName(grokModel)
                .temperature(grokTemperature)
                .build();
    }
}
