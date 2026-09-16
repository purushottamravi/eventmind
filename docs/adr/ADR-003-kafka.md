# ADR-003: Kafka for Cross-Service Event Distribution

- Status: Accepted
- Date: 2026-09-16
- Module(s) involved: `eventmind-command`, `eventmind-query`,
  `eventmind-observability`, `docker-compose.yml`

## Context

EventMind is no longer a single processed stream: `eventmind-command` produces
domain events, and both `eventmind-query` (read model) and `eventmind-observability`
(diagnostics) need to consume them, independently. Axon Server remains the event
store and command bus, but carrying the *distribution* of events over it couples
every consumer to the Axon infrastructure and gives each module little control over
its own consumption position.

## Decision

EventMind introduces Apache Kafka as the **event transport** between services, via
the Axon Kafka extension (`axon-kafka-spring-boot-starter` 4.11):

- **Producer** (`eventmind-command`): the auto-configured `KafkaEventPublisher`
  (subscribing event processor) forwards every event published on the local event
  bus to the shared `eventmind.events` topic
  (`eventmind-command/src/main/resources/application.yml`, `axon.kafka.*`).
- **Consumers**: `eventmind-query` and `eventmind-observability` register their
  tracking event processors (`symptom`, `observability`) against the auto-configured
  `StreamableKafkaMessageSource<K, V>` instead of Axon Server's event stream
  (`KafkaEventProcessingConfig.java` in both modules). Each processor keeps its own
  tokens in its local `TOKEN_ENTRY` store, so each read model tracks its own offset
  in the topic.
- **Broker**: a single-node KRaft broker (no ZooKeeper) in
  `docker-compose.yml` (`confluentinc/cp-kafka:7.7.1`), with auto topic creation so
  `eventmind.events` is created on first publish.

Kafka is explicitly **transport, not a store**. Event sourcing continues to run
through Axon Server (ADR-002); only distribution moves to Kafka.

## Alternatives considered

- **Axon Server alone** (pre-Kafka): keeps one infrastructure piece but couples all
  consumers to the event store's stream and start position.
- **Direct HTTP fan-out**: each consumer would need to be called by the producer,
  reintroducing point-to-point coupling and losing replay/offset semantics.
- **RabbitMQ (topic/AMQP)**: viable transport, but the Axon Kafka extension plus
  Kafka's log-based offset model map naturally onto Axon's tracking event
  processors.

## Consequences

### Advantages

- Decoupled, durable event transport: consumers come and go without touching the
  producer.
- Independent consumption: each tracking processor controls its own position, with
  replayable offsets. As new read models appear, they simply subscribe to the topic.
- Same-topic ordering is preserved per key by the `StreamableKafkaMessageSource`,
  while Axon keeps full control of command handling and event sourcing.

### Trade-offs

- Extra infrastructure (a Kafka broker) and configuration (`axon.kafka.*`, listener
  setup, compose health checks).
- **At-least-once publication**: consumers may see the same event more than once.
  The read models tolerate duplicates (aggregate idempotency, keyed writes) and the
  command module ignores duplicate creates
  (`SymptomCommandController`, `AggregateStreamCreationException`).
- One more moving part to operate.

## Compliance

- The command module is producer-only (`fetcher.enabled: false`); consumers are
  consumer-only (`publisher.enabled: false`) so observability never echoes events
  back onto the topic.
- All modules point at the same topic name: `eventmind.events`.