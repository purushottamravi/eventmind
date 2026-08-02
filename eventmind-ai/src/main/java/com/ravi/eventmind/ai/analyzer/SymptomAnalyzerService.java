package com.ravi.eventmind.ai.analyzer;


import com.ravi.eventmind.ai.model.DiagnosticContext;
import com.ravi.eventmind.ai.model.HealingRecommendation;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class SymptomAnalyzerService {

    private final PromptBuilder promptBuilder;
    private final ChatClient chatClient;


    public SymptomAnalyzerService(PromptBuilder promptBuilder, ChatClient.Builder builder) {
        this.promptBuilder = promptBuilder;
        this.chatClient = builder.build();
    }

    public HealingRecommendation analyze(DiagnosticContext context) {
        return analyze(context, "");
    }

    public HealingRecommendation analyze(DiagnosticContext context, String knowledgeContext) {
        String prompt = promptBuilder.build(context, knowledgeContext);
        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(HealingRecommendation.class);
    }
}
