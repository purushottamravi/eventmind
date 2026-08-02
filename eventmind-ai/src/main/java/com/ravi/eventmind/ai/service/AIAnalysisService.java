package com.ravi.eventmind.ai.service;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * An earlier attempt at AI analysis, now mostly sleeping behind a big comment block.
 * Kept around for reference, not wired up anywhere.
 */
@Service
public class AIAnalysisService {
/*
    //private final ApplicationLogRepository repository;
    private final ChatClient chatClient;

    public AIAnalysisService(ApplicationLogRepository repository,ChatClient.Builder builder) {
        this.repository = repository;
        this.chatClient = builder.build();
    }

    public String analyzeLogs() {


        var logs = repository.findAll()
                .stream()
                .map(log -> log.getMessage())
                .collect(Collectors.joining("\n"));


        String systemPrompt = """
        ROLE:
        You are an expert Java Site Reliability Engineer.

        CONTEXT:
        You are monitoring a Spring Boot application.
        The application uses CQRS and Event Sourcing.

        Your job:
        Analyze application logs and recommend recovery actions.

        RULES:
        - Do not execute any action.
        - Always require human approval.
        - Provide confidence score.
        - Return JSON only.

        OUTPUT:
        {
          "summary":"",
          "severity":"",
          "rootCause":"",
          "confidence":0,
          "recommendedAction":"",
          "reason":"",
          "requiresApproval":true
        }
        """;


        return chatClient.prompt()
                .system(systemPrompt)
                .user("""
                Analyze these logs:

                %s
                """.formatted(logs))
                .call()
                .content();
    } */
}