# Architectural Decision Records (ADR)

# EventMind – Architectural Decisions

This document explains the major architectural decisions made during the design and implementation of EventMind.

The goal of EventMind is not simply to demonstrate individual technologies, but to show how established enterprise architecture patterns can be combined with modern AI capabilities to build explainable, observable, and controlled operational intelligence.

---

# ADR-001: Why CQRS?

## Context

Production systems generate many more read operations than write operations.

A symptom is written once, but it may be queried repeatedly by dashboards, operational tools, and AI services.

The write side and read side therefore have different responsibilities.

## Decision

EventMind adopts Command Query Responsibility Segregation (CQRS).

The command side validates business rules and publishes immutable domain events.

The query side maintains optimized read models for searching and reporting.

## Consequences

### Advantages

- Independent evolution of write and read models
- Read models optimized for querying
- Simplified command model
- Better scalability
- Natural fit with Event Sourcing

### Trade-offs

- Additional infrastructure
- Eventual consistency
- More moving parts

---

# ADR-002: Why Event Sourcing?

## Context

Operational incidents are historical facts.

Replacing the current state with updates loses valuable diagnostic information.

## Decision

Symptoms are represented as immutable domain events.

Read models are projections generated from the event stream.

## Consequences

### Advantages

- Complete audit trail
- Replay capability
- Easy creation of new projections
- Explainable operational history

### Trade-offs

- Increased implementation complexity
- Projection management
- Event schema evolution

---

# ADR-003: Why Axon Framework?

## Context

Building CQRS and Event Sourcing infrastructure manually requires considerable boilerplate.

## Decision

Axon Framework is used for:

- Command routing
- Event publication
- Aggregates
- Event handlers
- Query gateway
- Tracking event processors

## Why not Kafka?

Kafka is an excellent event streaming platform but focuses on transport.

EventMind requires domain-oriented concepts such as:

- Aggregates
- Commands
- Event sourcing
- Query models

Axon provides these concepts directly.

Kafka was later introduced for event distribution between services; Axon Framework
remains the event-sourcing and command-handling backbone (see ADR-012).

---

# ADR-004: Why Domain-Driven Design?

## Context

Operational diagnostics involve multiple business capabilities.

Mixing concerns leads to tightly coupled services.

## Decision

EventMind separates responsibilities into bounded contexts.

Current modules include:

- eventmind-command
- eventmind-query
- eventmind-observability
- eventmind-ai

Each module owns its own business logic and persistence.

## Consequences

### Advantages

- Clear ownership
- Loose coupling
- Independent deployment
- Better maintainability

---

# ADR-005: Why Separate Databases?

## Context

Direct database sharing tightly couples services.

Changing one schema can break another service.

## Decision

Each module owns its own persistence.

For example:

Observability owns:

APPLICATION_LOG

AI owns:

AI_APPLICATION_LOG

Communication occurs through published APIs rather than database access.

## Advantages

- Service autonomy
- Independent schema evolution
- Better encapsulation
- Cleaner bounded contexts

---

# ADR-006: Why Retrieval-Augmented Generation (RAG)?

## Context

Large Language Models have no knowledge of the application's operational history.

Without context they may produce unreliable recommendations.

## Decision

The AI retrieves relevant documents and operational logs before generating recommendations.

Knowledge sources include:

- Project documentation
- Recent application logs
- Operational evidence

## Consequences

### Advantages

- Reduced hallucinations
- Explainable recommendations
- Lower token usage
- Domain-specific responses

---

# ADR-007: Why Java Flight Recorder?

## Context

Application logs alone rarely explain JVM-level failures.

Memory pressure, garbage collection, and thread activity are often missing.

## Decision

EventMind records JVM diagnostics using Java Flight Recorder.

Custom JFR events capture:

- Method execution
- Duration
- Exceptions
- Status

Standard JFR events provide:

- GC activity
- CPU load
- JVM diagnostics

The AI receives the JFR report during analysis.

## Advantages

- Low-overhead diagnostics
- JVM-native observability
- Better operational evidence

---

# ADR-008: Why Human Approval?

## Context

Incorrect automated remediation can worsen production incidents.

Examples include:

- Restarting healthy services
- Clearing important data
- Triggering cascading failures

## Decision

AI recommendations require explicit human approval before execution.

The recommendation lifecycle is:

PENDING_APPROVAL

↓

APPROVED / REJECTED

↓

EXECUTED / FAILED

Optimistic locking guarantees that recommendations cannot be executed twice.

## Advantages

- Safer operations
- Auditability
- Human oversight
- Explainable decisions

---

# ADR-009: Why Failure Isolation?

## Context

AI services and external dependencies should not become single points of failure.

## Decision

Each pipeline component degrades independently.

Examples:

LLM unavailable

↓

Default recommendation

Vector store unavailable

↓

Analysis without RAG

JFR unavailable

↓

Analysis without JVM diagnostics

## Consequences

The platform continues to function even when optional capabilities are unavailable.

---

# ADR-010: Why Correlation IDs?

## Context

Operational events travel across multiple services.

Tracing a request across modules is difficult without a shared identifier.

## Decision

Every request carries a Correlation ID.

The identifier flows through:

HTTP

↓

MDC

↓

Axon Metadata

↓

Event Handlers

↓

Application Logs

↓

AI Knowledge Store

## Advantages

- End-to-end tracing
- Easier debugging
- Better auditability

---

# ADR-011: Why Multi-Module Maven?

## Context

A single monolithic module becomes difficult to maintain as responsibilities grow.

## Decision

EventMind is organized as a Maven multi-module project.

Each module represents a logical architectural capability.

Benefits include:

- Independent packaging
- Clear dependency boundaries
- Better separation of concerns
- Faster navigation

---

# ADR-012: Why Kafka for Event Distribution?

## Context

EventMind distributes domain events between services. Originally Axon Server carried the
event stream (ADR-003). As the platform grows, a durable, independently-consumable event
transport decouples the command side from its read models.

## Decision

EventMind introduces Apache Kafka through the Axon Kafka extension
(`axon-kafka-spring-boot-starter` 4.11).

- `eventmind-command` publishes every domain event (today `SymptomCreatedEvent`) to the
  shared `eventmind.events` topic via the auto-configured `KafkaEventPublisher`
  (subscribing event processor). Axon Server stays the event store and command bus.
- `eventmind-query` and `eventmind-observability` consume from the same topic: their
  tracking event processors (`symptom`, `observability`) are registered against a
  `StreamableKafkaMessageSource` instead of Axon Server's event stream. Their tokens stay
  in the local `TOKEN_ENTRY` store, so each read model tracks its own offset.
- Kafka is a transport for the event stream, not an event store. Event sourcing still
  runs through Axon Server.

## Consequences

### Advantages

- Decoupled, durable event transport between services
- Independent consumers: each tracking processor keeps its own position in the topic
- Same-topic ordering preserved by the `StreamableKafkaMessageSource`, while Axon keeps
  full control of command handling and event sourcing

### Trade-offs

- Extra infrastructure (a Kafka broker) and configuration (`axon.kafka.*`)
- At-least-once publication: consumers must tolerate duplicates, which the read models
  already do (aggregate idempotency, keyed writes)
- One more moving part in the platform

---

# Future Architectural Evolution

Potential future enhancements include:

- OpenTelemetry integration
- Prometheus metrics
- Grafana dashboards
- Kubernetes deployment
- Multi-agent AI workflows
- Policy-driven healing engine
- Distributed tracing
- Multi-tenant deployment

---

# Guiding Principles

EventMind follows several architectural principles:

- Prefer events over shared mutable state.
- Every service owns its own data.
- AI must operate on evidence rather than assumptions.
- Diagnostics should be explainable and reproducible.
- Automation should remain under human control.
- Observability is a first-class architectural concern.
- Failure of one subsystem should not compromise the entire platform.