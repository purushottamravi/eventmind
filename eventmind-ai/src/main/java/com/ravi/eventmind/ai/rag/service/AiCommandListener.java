package com.ravi.eventmind.ai.rag.service;

import com.ravi.eventmind.shared.commands.AnalyzeSymptomWithAiCommand;
import com.ravi.eventmind.shared.commands.CreateHealingRecommendationCommand;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AiCommandListener {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final CommandGateway commandGateway;

    // Spring Boot automatically injects Spring AI components and Axon's gateway
    public AiCommandListener(ChatClient.Builder chatClientBuilder, VectorStore vectorStore, CommandGateway commandGateway) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
        this.commandGateway = commandGateway;
    }

    @CommandHandler
    public void handle(AnalyzeSymptomWithAiCommand command) {
        log.info("🤖 AI Module received command to analyze symptom: '{}' (ID: {})",
                command.symptomName(), command.symptomId());

        String healingAction = "No action recommended.";
        double confidenceScore = 0.0;

        try {
            // STEP 1: Execute RAG Retrieval from pgvector
            log.info("Searching vector database for relevant runbooks and historical logs...");

            // Perform a similarity search using the symptom name or key diagnostic indicators
            List<Document> similarDocuments = vectorStore.similaritySearch(command.symptomName());

            // Flatten retrieved knowledge blocks into a single string context
            String context = similarDocuments.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n---\n"));

            log.info("Retrieved {} relevant documentation fragments for context.", similarDocuments.size());

            // STEP 2: Construct the bounded Prompt and call the LLM (e.g., llama3.1 via Ollama)
            log.info("Invoking LLM for diagnostic analysis...");
            String aiOutput = chatClient.prompt()
                    .system(p -> p.text("You are an expert Java System Reliability Engineer. Analyze the provided JFR report context and suggest a concrete healing action. Keep your response brief and structured. Always conclude your analysis with a line matching this exact format: 'CONFIDENCE: [0.0 to 1.0]'"))
                    .user(p -> p.text(String.format("Context (Runbooks & Past Logs):%s Live JFR Anomaly Report:%s", context, command.diagnosticReportText())))
                    .call()
                    .content();

            log.info("LLM Generation completed successfully.");

            // STEP 3: Parse the LLM output safely to extract data fields
            healingAction = aiOutput;
            confidenceScore = extractConfidenceScore(aiOutput);

        } catch (Exception e) {
            // Failure Isolation Guardrail: If AI or Database is completely offline,
            // degrade gracefully instead of throwing exceptions up to the event bus.
            log.error("AI/RAG pipeline pipeline processing failed. Falling back to default guardrail.", e);
            healingAction = "FALLBACK: System failure detected, but the AI module is currently unreachable. Please check metrics manually.";
            confidenceScore = 0.0;
        }

        // STEP 4: Next Logical Chain Link (Publish the recommendation back to the system)
        log.info("Publishing generated healing recommendation with confidence: {}", confidenceScore);

        // Example: Forwarding to your Axon execution aggregate
           commandGateway.send(new CreateHealingRecommendationCommand(
                 command.symptomId(),
                healingAction,
                 confidenceScore,
                 "PENDING_APPROVAL"
         ));
    }

    /**
     * Helper logic to extract 'CONFIDENCE: X.X' safely from string text
     */
    private double extractConfidenceScore(String text) {
        try {
            if (text != null && text.contains("CONFIDENCE:")) {
                String scoreStr = text.substring(text.lastIndexOf("CONFIDENCE:") + 11).trim();
                return Double.parseDouble(scoreStr);
            }
        } catch (Exception e) {
            log.warn("Could not cleanly parse confidence score from LLM output, defaulting to 0.5");
        }
        return 0.5;
    }
}