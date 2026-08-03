package com.ravi.eventmind.ai.analyzer;


import com.ravi.eventmind.ai.model.DiagnosticContext;
import com.ravi.eventmind.ai.model.HealingRecommendation;
import com.ravi.eventmind.shared.exceptions.EventMindExceptions;
import com.ravi.eventmind.shared.exceptions.EventMindReasonsEnum;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * The actual chat with the model. Takes a diagnostic context, fires the prompt,
 * and turns whatever the LLM answers into a recommendation.
 */
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
        HealingRecommendation recommendation = chatClient.prompt()
                .user(prompt)
                .call()
                .entity(HealingRecommendation.class);
        if (recommendation == null || recommendation.suggestedAction() == null
                || recommendation.suggestedAction().isBlank()) {
            throw new EventMindExceptions(EventMindReasonsEnum.EXECUTION_FAILED,
                    "LLM response is missing a suggestedAction; refusing to persist a broken recommendation");
        }
        return recommendation;
    }
}
