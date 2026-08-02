# EventMind

An event-driven, self-healing system for Java applications. EventMind captures application
symptoms through CQRS + Event Sourcing (Axon), observes runtime logs, and uses a RAG pipeline
(Spring AI) to generate and execute healing recommendations under human approval.

## Problem Statement

Production Java applications fail for reasons that are hard to pin down: an OOM, a GC
pressure spike, a cascading service timeout. The signals that could explain each failure are
scattered across symptoms, application logs, and JVM diagnostics, and the knowledge of how to
fix them usually lives only in someone's head. Diagnosing is slow, remediation is reactive and
manual, and every incident repeats the same tribal-knowledge lookup.

EventMind tackles that end to end:

- **Capture** symptoms as first-class, auditable facts (CQRS + Event Sourcing) instead of ad-hoc
  tickets.
- **Observe** the runtime: application logs and JVM/JFR diagnostics are recorded for every
  executed operation.
- **Contextualize** incidents with RAG, grounding the analysis in project documents and recent
  operational log entries.
- **Act** with guardrails: the LLM proposes a structured healing plan, but a human approves it
  before anything is executed.

## Architecture

```
REST ──► Command (Axon) ──► Events ──► Axon Server ──► Query side (read models)
                 │
                 ▼
          Observability (ApplicationLog)
                 │   POST /logs (ingestion)
                 ▼
          AI log store (AI_APPLICATION_LOG, AI-owned)
                 │
                 ▼
          RAG retrieval ──► LLM analysis ──► Recommendation ──► Approve/Execute
```

The pipeline is fully wired across modules:

- **REST → CQRS**: `POST /symptoms` dispatches a `CreateSymptomCommand` via Axon.
- **Events → Observability**: `SymptomCreatedEvent` is consumed by the observability
  module through the Axon event stream (`SymptomEventLogHandler`, tracking processor
  `observability`) and recorded into `APPLICATION_LOG`. Observability never reaches into
  the command module; it only reacts to published domain events.
- **Observability → AI (port ownership)**: the AI module never reads the observability
  database directly. It owns its own `AI_APPLICATION_LOG` store and ingests log entries
  through `POST /logs` (AI-side). The observability module is the producer: after
  recording an event it pushes the entry to `POST /logs` via `LogIngestionClient`
  (best-effort, asynchronous, failure-isolated), so each module stays isolated behind its
  own repository.
- **Logs → RAG**: log entries are fed into the vector store as knowledge documents.
- **RAG → LLM → Recommendation**: `POST /healing/analyze` retrieves relevant knowledge,
  builds a `DiagnosticContext` from recent application logs, and asks the LLM for a
  structured healing recommendation.
- **Recommendation execution**: recommendations require human approval and are executed
  through a pluggable `HealingExecutor`.

## Design Decisions

### Why CQRS?

Symptoms are written once and read many times, from very different workloads. The write side
(`eventmind-command`) owns the invariants — idempotent create, origin validation, UUID
idempotency keys — and dispatches commands through Axon. The read side (`eventmind-query`)
keeps a denormalized, paginated read model and never has to worry about write contention.
CQRS lets each side scale, evolve, and fail independently, and it keeps the write model free of
read-optimization concerns.

### Why Event Sourcing?

Every symptom is an immutable fact, not a mutable row. Storing the event stream instead of (or
alongside) the current state gives EventMind a complete, replayable audit trail of everything
that happened, which matches the system's core promise: observable and explainable behavior.
Read models are derived projections that can be rebuilt or extended at any time, and duplicate
delivery (at-least-once semantics) is neutralized by aggregate idempotency rather than fragile
exactly-once tricks.

### Why RAG?

A bare LLM has no knowledge of this application's operating context — its services, its error
signatures, its runbook knowledge. RAG grounds every analysis in documents (`DocumentEntity`)
and recent application-log entries, so the recommendation is based on evidence the system can
trace back to a source instead of on the model's general guesses. Retrieval narrows the prompt
to the relevant context, which reduces hallucinations and keeps token costs bounded.

### Why JFR?

Java Flight Recorder is the lowest-overhead, always-on window into the JVM: GC, memory
pressure, and thread activity. EventMind records a custom `MethodExecutionEvent` (and
`CommandExecutionEvent`) via `AuditAspect` for every executed operation — class, method,
duration, status, and exception — producing a JVM-native trace alongside the application log.
That diagnostic signal (`jfrReport`) is passed straight into the LLM prompt during analysis, so
the AI reasons about the same heap/GC evidence an engineer would.

### Why Human Approval?

Autonomous healing is dangerous: the wrong remediation can drop data, restart a service in a
worse state, or amplify an incident. Recommendations therefore move through an explicit status
machine (`PENDING_APPROVAL → APPROVED/REJECTED → EXECUTED/FAILED`) and only execute after a
human approves. The decision is auditable, and optimistic locking (`@Version` + a guarded
`transitionStatus`) guarantees that two concurrent approvals cannot double-execute — the loser
gets an HTTP 409. Automation where it is safe, a human gate where it is not.

## Modules

| Module                | Port | Description                                                                 |
|-----------------------|------|-----------------------------------------------------------------------------|
| `eventmind-command`   | 8081 | Axon aggregate, command handling, symptom/healing REST entry points          |
| `eventmind-query`     | 8082 | Query side: reads symptoms from the event store via `QueryGateway`           |
| `eventmind-observability` | 8083 | Application log persistence (`APPLICATION_LOG`), JFR auditing, `AuditAspect` |
| `eventmind-ai`        | 8080 | RAG ingestion/retrieval, LLM analysis, recommendation store, approval flow   |
| `eventmind-shared`    |  -   | Shared DTOs, exceptions, validation and the `@AuditLog` annotation           |

## Tech Stack

- Java 21, Maven wrapper
- Spring Boot 3.5.4
- Axon Framework 4.11 (CQRS / Event Sourcing), Axon Server 2024.2.2
- Spring AI 1.0.0 (Ollama, vector store RAG)
- JPA / Hibernate (H2 for command/query/observability, Postgres for AI), Flyway

## Prerequisites

- JDK 21
- Docker (for Axon Server)
- Ollama with a model configured (default `llama3.1`) — the LLM call degrades gracefully
  to a default recommendation when unavailable
- Postgres (default `localhost:5432/eventmind`, override via `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`)

## Getting Started

1. Start Axon Server:

   ```bash
   docker compose up -d
   ```

   Axon Server UI: `http://localhost:8024` (gRPC on `8124`).

2. Build and run tests (offline, using the local `.m2` cache):

   ```powershell
   $env:JAVA_HOME="D:\JDK\jdk21"
   .\mvnw.cmd -o clean test
   ```

3. Run each module (either from the IDE or via `.\mvnw.cmd -o spring-boot:run -pl <module>`):
   `eventmind-command`, `eventmind-query`, `eventmind-ai`, and optionally
   `eventmind-observability`.

## REST API

### Symptom (CQRS)

`POST http://localhost:8081/symptoms` — create a symptom

```json
{
  "id": "77a1b2c3-4d5e-4f6a-8b7c-9d0e1f2a3b4c",
  "name": "Database timeout",
  "origin": ["A", "B"],
  "numberOfOccurance": 3
}
```

`origin` is a non-empty JSON array of `OriginType` symbolic names — `A`, `B`, `C`, `D`,
`E`. The server cumulates the values into a numeric bitmask stored with the symptom
(`["A","B"]` → `3`, the OR of the per-type flags `1,2,4,8,16`). The optional `id` is the
client-supplied idempotency key; if omitted the server generates a UUID.

`GET http://localhost:8082/symptoms?name=&page=0&size=20` — list symptoms

The response re-encodes the stored bitmask back to the symbolic `origin` names, so the
wire contract is symmetric: requests and responses both carry `origin` as an array of
`OriginType` names.

### Healing (AI)

`POST http://localhost:8080/healing/analyze` — analyze a symptom and produce a recommendation

```json
{
  "symptomId": "s1",
  "symptom": "OOM",
  "logs": "ERROR heap exhausted",
  "jfrReport": "GC paused",
  "topK": 3
}
```

`GET http://localhost:8080/healing/recommendations/{id}` — fetch a recommendation

`POST http://localhost:8080/healing/approve` — approve or reject a recommendation

```json
{
  "recommendationId": "r1",
  "decision": "APPROVED",
  "approvedBy": "ravi",
  "comment": "go ahead"
}
```

`POST http://localhost:8080/healing/knowledge/ingest` — (re)build the RAG knowledge base
from `documents` and recent AI log entries

`POST http://localhost:8080/logs` — ingest an application log entry into the AI-owned
`AI_APPLICATION_LOG` store (RAG knowledge source)

```json
{
  "level": "ERROR",
  "message": "Database timeout",
  "exception": "connect timed out"
}
```

`POST http://localhost:8081/healing/approve` — command-side healing plan approval

## RAG Knowledge Sources

The vector store is fed by `DocumentLoader`, which combines:

- `DocumentEntity` rows (`documents` table, type/source/domain tagged)
- Recent `ApplicationLogEntity` rows from the AI-owned `AI_APPLICATION_LOG` table
  (ingested via `POST /logs`, read through the `LogProvider` port)

## Failure Isolation

Each AI pipeline step degrades independently:

- LLM unavailable → default recommendation with confidence 0
- Vector store unavailable → empty knowledge context (RAG skipped)
- Log load failure → empty log context

## Concurrency & State Integrity

- `HealingRecommendationEntity` carries a JPA `@Version` for optimistic locking.
- Status changes go through an atomic guarded update
  (`transitionStatus(id, from, to)`), so two concurrent approvals cannot double-execute:
  the loser gets HTTP 409. Missing recommendations return HTTP 404.
- The status machine is enforced in `HealingRecommendationService`:
  `PENDING_APPROVAL → APPROVED/REJECTED → EXECUTED/FAILED`.

## Known Notes

- `eventmind-ai` requires Postgres; command/query/observability use H2 file DBs.
- `docker-compose.yml` runs Axon Server only. The old single-module `dockerfile`
  was removed: it referenced a non-existent `eventmind-application` module and
  cannot represent this multi-app system (command/query/ai/observability each run
  as their own process on their own port). Run each module with
  `.\mvnw.cmd -o spring-boot:run -pl <module>` instead.
- Dependency hygiene is enforced centrally: Axon/Guava versions live in the parent
  BOM only, and `maven-dependency-plugin:analyze` fails the build on unused or
  undeclared dependencies (`clean test`).
