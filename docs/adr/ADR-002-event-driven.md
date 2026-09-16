# ADR-002: Event-Driven Architecture with Event Sourcing

- Status: Accepted
- Date: 2026-09-16
- Module(s) involved: `eventmind-shared`, `eventmind-command`, `eventmind-query`,
  `eventmind-observability`

## Context

Operational incidents are historical facts. If the platform only ever stores the
*current* state of a symptom, it loses valuable diagnostic information:

- How many times has this symptom occurred?
- What was the original correlation ID / trace?
- What series of events led to the current recommendation state?

Overwriting current state with updates is exactly the wrong model for a system whose
job is to understand *what happened* and *why*.

## Decision

EventMind treats events as immutable facts and the event log as the system of record:

- Every meaningful change produces an immutable, versioned domain event. Today that
  is `SymptomCreatedEvent` and `HealingRecommendationCreatedEvent`, defined once in
  `eventmind-shared` so all modules compile against the same contract.
- The command side is event-sourced: `SymptomAggregate` and
  `HealingRecommendationAggregate` use Axon's event sourcing, apply events via
  `AggregateLifecycle.apply(...)`, and rebuild their in-memory state from those
  events (`@EventSourcingHandler`).
- Read models are **projections** of the event stream, not an alternative source of
  truth. `SymptomEventsHandler` (query) and `SymptomEventLogHandler` (observability)
  both derive their state purely from events.
- The correlation ID (ADR-001) travels on every event, so each fact stays traceable
  back to its originating request.

The event flow:

```
Command (CreateSymptomCommand / CreateHealingRecommendationCommand)
  -> Aggregate validates + applies event
  -> SymptomCreatedEvent / HealingRecommendationCreatedEvent (immutable fact)
  -> published to Axon event bus and to Kafka for the other modules (ADR-003)
  -> read models / log handlers project the fact
```

## Alternatives considered

- **CRUD state persistence**: simpler to implement, but destroys audit history,
  replay capability, and the ability to build new projections later.
- **Outbox/CDC without an event-sourcing framework**: would require hand-building
  aggregates, event handlers, and replay logic (see ADR-003 for why Axon carries the
  backbone load).

## Consequences

### Advantages

- Complete, append-only audit trail of every operational fact.
- **Replay capability**: read models can be rebuilt from the event stream.
- New projections can be added without touching existing consumers.
- Explainable operational history feeding the AI analysis (ADR-004).

### Trade-offs

- Increased implementation complexity.
- Projection management and eventual consistency (ADR-001).
- **Event schema evolution**: changing an event's shape affects every consumer, so
  events are kept in `eventmind-shared` and treated as APIs.
- At-least-once delivery (ADR-003) means handlers must tolerate duplicates; the
  handlers are written to be idempotent (keyed writes, aggregate idempotency).

## Compliance

- Events are only mutated by adding new ones; existing events are never edited.
- Handlers never persist state derived from anything except the event stream.