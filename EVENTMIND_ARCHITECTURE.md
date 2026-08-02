# EventMind Architecture Design

## Overview

EventMind is an event-driven observability platform built with Java 21, Spring Boot, and the Axon Framework. It applies CQRS and Event Sourcing principles to capture business events, monitor application behavior, and provide the foundation for AI-assisted operational analysis.
The platform is designed to help engineers understand system behavior by correlating commands, domain events, application logs, and runtime diagnostics.

The system combines:

- CQRS architecture
- Domain-driven design principles
- Event-driven processing
- Application logging
- Java Flight Recorder (JFR) analysis
- Large Language Model (LLM) analysis
- Human-in-the-loop approval workflow

The AI component does not directly execute corrective actions. It provides recommendations that require human approval before any healing action is performed.

---

# High-Level Architecture

```
                         Command
                            |
                            v
                 Command Processing Layer
                            |
                            v
                    Symptom Created
                            |
              +-------------+-------------+
              |                           |
              v                           v
 Application Logging              JFR Analysis
              |                           |
              +-------------+-------------+
                            |
                            v
                 Diagnostic Context Builder
                            |
                            v
                    AI Analysis Engine
                            |
                            v
               Healing Recommendation Created
                            |
                            v
                  Human Approval Workflow
                            |
                  +---------+---------+
                  |                   |
                  v                   v
              Approved            Rejected
                  |
                  v
            Healing Execution
```

---

# Recommended Package Structure

```
com.ravi.eventmind

├── command
│
│   ├── api
│   │   ├── commands
│   │   └── controller
│   │
│   ├── domain
│   │   ├── aggregate
│   │   ├── events
│   │   ├── model
│   │   └── exceptions
│   │
│   └── application
│       └── service
│
├── query
│
│   ├── api
│   │   └── controller
│   │
│   ├── model
│   └── repository
│
├── symptom
│
│   ├── domain
│   │   ├── Symptom.java
│   │   └── SymptomCreatedEvent.java
│   │
│   ├── repository
│   └── service
│
├── observability
│
│   ├── logging
│   │   ├── ApplicationLog.java
│   │   ├── ApplicationLogRepository.java
│   │   └── DatabaseLogger.java
│   │
│   └── jfr
│       ├── JfrAnalyzer.java
│       ├── JfrReport.java
│       └── JfrEventListener.java
│
├── ai
│
│   ├── analyzer
│   │   ├── SymptomAnalyzer.java
│   │   └── PromptBuilder.java
│   │
│   ├── model
│   │   └── HealingRecommendation.java
│   │
│   └── approval
│       └── HumanApprovalService.java
│
├── healing
│
│   ├── command
│   ├── service
│   └── events
│
└── EventMindApplication.java
```

---

# Architectural Responsibilities

## Command Module

Responsible for:

- Receiving commands
- Executing business operations
- Creating domain events
- Updating aggregates

Example:

```
CreateSymptomCommand
        |
        v
SymptomCommandHandler
        |
        v
SymptomCreatedEvent
```

---

# Symptom Domain

The symptom represents an observed problem in the application.

Examples:

- High memory usage
- Failed command execution
- Database timeout
- Event processing failure

Structure:

```
symptom

├── domain
│   ├── Symptom.java
│   └── SymptomCreatedEvent.java
│
├── repository
└── service
```

---

# Observability Module

Responsible for collecting technical information.

## Logging

Stores application-generated logs.

Example:

```
ApplicationLog
--------------
id
symptomId
level
message
timestamp
```

---

## JFR Analysis

Java Flight Recorder provides JVM-level information.

Examples:

- Heap usage
- Garbage collection activity
- Thread count
- CPU usage
- Allocation rate

Structure:

```
observability

└── jfr

    ├── JfrAnalyzer
    ├── JfrReport
    └── JfrEventListener
```

---

# AI Analysis Module

The AI layer is responsible for reasoning over diagnostic information.

It should not directly modify application state.

Structure:

```
ai

├── analyzer
│   ├── SymptomAnalyzer
│   └── PromptBuilder
│
├── model
│   └── HealingRecommendation
│
└── approval
    └── HumanApprovalService
```

---

# AI Prompt Design

The AI prompt should contain:

## Role

Defines the AI responsibility.

Example:

```
You are an expert Java Site Reliability Engineer.
```

---

## Context

Provides application information.

Example:

```
Application:
EventMind

Architecture:
Spring Boot
CQRS
Event Sourcing
Axon Framework

Available data:
- Application logs
- JFR analysis
- Symptom information
```

---

## Constraints

Defines AI boundaries.

Example:

```
- Do not execute actions.
- Always request human approval.
- Provide confidence score.
- Explain reasoning.
```

---

## Expected Output

The AI response should be structured.

Example:

```json
{
  "diagnosis": "Memory leak detected",
  "rootCause": "Increasing retained heap objects",
  "confidence": 87,
  "recommendedAction": "Restart service instance",
  "requiresHumanApproval": true
}
```

---

# Human Approval Workflow

The AI creates a recommendation.

The human decides whether the action is allowed.

Flow:

```
AI Recommendation

        |
        v

Human Review

        |
        +-------------+
        |             |
        v             v

    Approved       Rejected

        |
        v

Healing Command
```

---

# Healing Module

The healing module executes only approved actions.

Examples:

- Restart service
- Clear cache
- Retry failed processing
- Trigger event replay
- Adjust configuration

Structure:

```
healing

├── command
├── service
└── events
```

---

# CQRS Event Flow

Recommended event flow:

```
Command
   |
   v
Command Handler
   |
   v
SymptomCreatedEvent
   |
   +----------------+
   |                |
   v                v

Store Logs       Capture JFR

   |
   v

DiagnosticContextCreatedEvent

   |
   v

AIAnalysisCommand

   |
   v

HealingRecommendationCreatedEvent

   |
   v

Human Approval

   |
   v

HealingApprovedEvent

   |
   v

HealingExecutedEvent
```

---

# Design Principles

## Separation of Responsibilities

The AI should:

- Analyze
- Recommend
- Explain

The AI should not:

- Execute commands
- Modify state
- Bypass approval


## Domain Driven Design

Each business capability should have its own boundary:

- Command processing
- Symptoms
- Observability
- AI analysis
- Approval
- Healing


## Event Driven Architecture

Events represent facts:

Examples:

```
SymptomCreatedEvent

HealingRecommendationCreatedEvent

HealingApprovedEvent

HealingExecutedEvent
```

---

# Future Enhancements

Possible future additions:

- Vector database for historical symptom analysis
- Similar incident detection
- Automated confidence calibration
- AI-generated incident reports
- Dashboard for human approval
- Event replay recommendations
- Kubernetes remediation actions
