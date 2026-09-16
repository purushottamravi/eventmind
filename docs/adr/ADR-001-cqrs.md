# ADR-001: Command Query Responsibility Segregation (CQRS)

- Status: Accepted
- Date: 2026-09-16
- Module(s) involved: `eventmind-command`, `eventmind-query`, `eventmind-shared`

## Context

Operational systems are dominated by reads, not writes. A symptom is created once
but is queried many times by dashboards, operational tools, and the AI module.
Writing and reading therefore have fundamentally different requirements:

- The **write path** must validate business rules, enforce invariants, and produce an
  authoritative, audit-ready record of *what happened*.
- The **read path** must be shaped for fast, convenient querying (search, report,
  projection), not for enforcing business rules.

Mixing both in one model forces compromises: the write model becomes cluttered with
query concerns, and the read model is tempted to accept writes it has no business
validating.

## Decision

EventMind splits the two responsibilities into separate services with their own
persistence:

| Concern | Module | Responsibility |
| --- | --- | --- |
| Writes (commands) | `eventmind-command` | Validate, enforce invariants, apply events to Axon aggregates |
| Reads (queries) | `eventmind-query` | Project `SymptomCreatedEvent` into a read model and serve queries |

The write side is a pure command surface:

- `SymptomCommandController` validates the request body (JSON Schema + mapping) and
  dispatches `CreateSymptomCommand` through the Axon `CommandGateway`
  (`eventmind-command/.../symptom/controller/SymptomCommandController.java`). No
  business logic lives in the controller.
- `SymptomAggregate` is the single source of truth for a symptom: it validates the
  command, applies `SymptomCreatedEvent`, and rebuilds its state from the event
  stream (`SymptomAggregate.java`).

The read side is a pure projection:

- `SymptomEventsHandler` (processing group `symptom`) listens for
  `SymptomCreatedEvent`, maps it onto the query `Symptom` entity, and stores it in
  the query read model (`SymptomEventsHandler.java`).
- `SymptomQueryService` / `SymptomQueryController` serve those read models over REST
  (`eventmind-query`).

Shared contracts (`SymptomCreatedEvent`, `SymptomRestModel`) live in
`eventmind-shared` so both sides compile against the same model.

## Alternatives considered

- **Single CRUD service**: simplest, but read and write models evolve at the same
  pace; schema changes on either side risk regressing the other, and the write
  invariants leak into the query API.
- **Read model via database views**: couples the query module to the command
  module's schema; rejected in favor of an explicitly projected read model.

## Consequences

### Advantages

- Write and read models evolve independently.
- Read models are optimized purely for querying (no business logic drift).
- The command model stays simple and focused on invariants.
- Read models can be rebuilt from the event stream at any time.
- Natural fit with Event Sourcing (see ADR-002) and with the multi-module layout
  (ADR-004).

### Trade-offs

- Additional infrastructure: two services and two stores instead of one.
- **Eventual consistency**: the read model lags the write model until the event is
  projected.
- More moving parts to operate and trace (mitigated by correlation IDs, ADR-010).

## Compliance

- `POST /symptoms` only dispatches commands; `GET` endpoints exist only in
  `eventmind-query`.
- The command module never reads the read model; the query module never accepts
  writes.