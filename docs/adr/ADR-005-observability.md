# ADR-005: Observability as a First-Class Concern

- Status: Accepted
- Date: 2026-09-16
- Module(s) involved: `eventmind-observability`, `eventmind-ai`, `infra/`,
  `docker-compose.yml`

## Context

To diagnose an operational incident across a distributed, event-driven system you
need more than one category of signal, and you need them *correlated*:

- not only *business* facts (events from ADR-002),
- but also *runtime* facts: application logs, JVM-level diagnostics (GC, CPU,
  exceptions, method latency), and aggregate health metrics.

Why was the symptom raised? Was the JVM under memory pressure? Which method was slow?
Metrics (Prometheus) tell you *what* happened over time, logs and JFR tell you *why*,
and correlation IDs tell you *which* request chain it belongs to.

## Decision

Observability is distributed across three complementary layers, owned by the
`eventmind-observability` module plus shared infra:

### 1. Application logging

- `SymptomEventLogHandler` (processing group `observability`) consumes
  `SymptomCreatedEvent` from Kafka, persists an `APPLICATION_LOG` row, and forwards
  it to the AI module (`LogIngestionClient`).
- `ApplicationLogRecorder` (AOP) records audited method executions (`@AuditLog`) into
  the same log store.
- The correlation ID is restored into the MDC while handling each event, so every
  log line stays traceable to its originating request.

### 2. JVM diagnostics (JFR)

- `JfrEventListener` runs a live `RecordingStream` (GC, CPU load, exceptions, custom
  `MethodExecution` events) and feeds a shared in-memory `JfrReport`
  (`eventmind-observability.jfr.enabled` toggle).
- `JfrAnalyzer` renders the snapshot as a plain-text report, exposed at
  `GET /jfr/report` (`JfrController`). The AI module fetches this report as part of
  its evidence (ADR-004), giving the LLM JVM-level context that logs alone never
  contain.

### 3. Metrics (Prometheus + Grafana)

- Every module exposes `/actuator/prometheus` via Actuator + the Micrometer
  Prometheus registry (shared dependencies in the parent `pom.xml`).
- `infra/prometheus/prometheus.yml` scrapes all four services every 15s
  (`command:8081`, `query:8082`, `observability:8083`, `ai:8080`).
- `infra/grafana/` auto-provisions a Prometheus datasource and the
  `eventmind-overview` dashboard (service up/down, HTTP request rate, latency p95,
  JVM heap). All metrics are tagged with `application` so panels can aggregate per
  service.
- The stack runs as compose services (`docker-compose.yml`): Prometheus on 9090,
  Grafana on 3000.

### Correlation

The correlation ID is the glue: it flows HTTP → MDC → Axon metadata → event
(ADR-002) → handlers → logs → AI knowledge store, as captured in
`eventmind-shared`'s correlation package.

## Alternatives considered

- **Logs only**: no JVM-level visibility and no numeric trends; the AI would analyze
  symptoms blind (ADR-004 depends on JFR evidence).
- **External monitoring SaaS**: would send logs/metrics outside the platform,
  contradicting the local-first choice of Ollama (ADR-004).
- **JFR files only (on-demand)**: `-XX:StartFlightRecording` requires pre-config and
  produces large files; a live streaming listener keeps diagnostics always-on and
  cheap.

## Consequences

### Advantages

- Three independent signal types let the AI and engineers cross-check evidence.
- Always-on, low-overhead JFR diagnostics from native JVM events.
- Metrics are queryable *after* the fact (trends, incidents) and feed future
  alerting.
- Infrastructure is versioned with the repo (`infra/`), config and data cleanly
  separated (bind-mount config vs named volumes for data).

### Trade-offs

- More infrastructure to run and operate (JFR listener, Prometheus, Grafana).
- JFR reports are in-memory snapshots; long-term diagnostics storage is out of
  scope today.
- Prometheus/Grafana add a second observability store alongside the log database.

## Compliance

- All four services expose the same `/actuator/prometheus` contract and carry the
  `application` metric tag.
- The observability module is consumer-only on Kafka (never re-publishes events).