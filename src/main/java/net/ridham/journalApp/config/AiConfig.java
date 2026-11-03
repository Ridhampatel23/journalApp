package net.ridham.journalApp.config;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import net.ridham.journalApp.ai.SentimentClassifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.Map;

@Configuration
public class AiConfig {

    @Bean
    @ConditionalOnProperty(name = "ai.provider", havingValue = "anthropic")
    public ChatModel anthropicChatModel(@Value("${ai.anthropic.api-key:}") String apiKey,
                                        @Value("${ai.anthropic.model:claude-opus-5-5}") String model) {
        return AnthropicChatModel.builder()
                .apiKey(apiKey)
                .modelName(model)
                .maxTokens(2048)
                .customParameters(Map.of("output_config", Map.of("effort", "low")))
                .timeout(Duration.ofSeconds(30))
                .maxRetries(2)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
    public ChatModel openAiChatModel(@Value("${ai.openai.api-key:}") String apiKey,
                                     @Value("${ai.openai.model:}") String model) {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(model)
                .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
                .strictJsonSchema(true)
                .timeout(Duration.ofSeconds(30))
                .maxRetries(2)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "ai.provider", havingValue = "groq")
    public ChatModel groqChatModel(@Value("${ai.groq.api-key:}") String apiKey,
                                   @Value("${ai.groq.model:llama-3.1-8b-instant}") String model) {
        return OpenAiChatModel.builder()
                .baseUrl("https://api.groq.com/openai/v1")   // Groq uses the OpenAI API format
                .apiKey(apiKey)
                .modelName(model)
                .timeout(Duration.ofSeconds(30))
                .maxRetries(2)
                .build();
    }

    @Primary
    @Bean
    @ConditionalOnProperty(name = "ai.provider", havingValue = "ollama",  matchIfMissing = true)
    public ChatModel ollamaChatModel(@Value("${ai.ollama.base-url:http://localhost:11434}") String baseUrl,
                                     @Value("${ai.ollama.model:llama3.2:3b}") String model) {
        return OllamaChatModel.builder()
                .baseUrl(baseUrl)                            // local Ollama server, no API key
                .modelName(model)
                .timeout(Duration.ofSeconds(60))// local models can be slow on a laptop
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    // Builds the proxy that implements SentimentClassifier on top of whichever ChatModel exists.
    // This is the swap point: nothing else changes when the provider changes.
    public SentimentClassifier sentimentClassifier(ChatModel chatModel) {
        return AiServices.create(SentimentClassifier.class, chatModel);
    }

}