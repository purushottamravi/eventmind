# Event Flow (current)

1. A REST request hits `HealingController` in **eventmind-command** (:8081) → `CreateSymptomCommand`.
2. `SymptomAggregate.apply(...)` emits `SymptomCreatedEvent` on the Axon command bus → Axon Server records it (event store).
3. Axon's auto-configured `KafkaEventPublisher` also forwards the event to the Kafka topic **`eventmind.events`**.
4. Tracking processors consume the topic via `StreamableKafkaMessageSource`:
   - **query** (:8082), group `symptom` → `SymptomEventsHandler` updates H2.
   - **observability** (:8083), group `observability` → `SymptomEventLogHandler` logs it.
5. Axon Server remains the command bus + event store (source of truth); Kafka is the distribution channel to the read/observability sides.

Startup order: `docker compose up -d` brings up Axon Server, Kafka, and all four services.

See `README.md` for the full architecture and `DECISIONS.md` ADR-012 for the design rationale.
