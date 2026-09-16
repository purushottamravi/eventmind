# ADR-004: AI-Assisted Healing Analysis (evidence-based, human-approved)

- Status: Accepted
- Date: 2026-09-16
- Module(s) involved: `eventmind-ai`, `eventmind-observability`

## Context

An LLM has no knowledge of EventMind's operational history and, given free rein,
can make confident but wrong remediation decisions. Operational evidence lives in
three places: application logs, JVM-level diagnostics (JFR), and project knowledge.
AI analysis is only trustworthy if it reasons over that evidence and is never allowed
to act on its own.

## Decision

The AI module (`eventmind-ai`) runs local models via **Ollama** (chat: `llama3.1`,
embeddings: `nomic-embed-text` on pgvector) through Spring AI, and follows four rules:

### 1. Analyze over assembled evidence

`HealingRecommendationService.analyze(...)` builds a `DiagnosticContext` from:
- recent application logs (`LogProvider`),
- a JFR report (`JfrReportClient` → `GET /jfr/report` on observability, or an
  inline report passed in the request),
- the symptom description itself.

`SymptomAnalyzerService` fires the prompt via Spring AI `ChatClient` and maps the
response into a structured `HealingRecommendation` with a confidence score, reasoning,
and `requiresHumanApproval` flag (`SymptomAnalyzerService.java`).

### 2. Retrieve knowledge before generation (RAG)

`RagRetrievalService` (default `VectorStoreRagRetrievalService`) searches project
knowledge documents embedded into pgvector, and the top-K chunks are injected into
the prompt as `knowledgeContext` (`HealingRecommendationService`, `topK` default 3).
This reduces hallucinations and grounds answers in domain context (see
`rag/` package: loader, chunker, embedding, retrieval, ingestion).

### 3. Never act — recommend

The AI only produces a recommendation. It never touches application state. The
`recommendation` flow stops at `PENDING_APPROVAL`.

### 4. Require human approval

`HumanApprovalService` turns the approver's decision into an `ApprovalResult`.
`HealingRecommendationService` walks the lifecycle:

```
PENDING_APPROVAL -> APPROVED -> EXECUTED
                 \-> REJECTED \-> FAILED
```

Transitions are guarded (`store.transition(...)`), so a recommendation can only be
approved/executed once (state conflict otherwise). Actual execution is delegated to
the `HealingExecutor` (currently the `LoggingHealingExecutor` logs the action; the
command module owns real remediation via `POST /healing/approve` and
`HealingService`).

### Failure isolation

Each piece of evidence is optional and degrades independently: LLM down → default
recommendation; vector store down → analysis without RAG; JFR unavailable → analysis
without JVM diagnostics.

## Alternatives considered

- **Direct LLM API (e.g. OpenAI)**: would require a cloud API key and sends
  operational logs externally; Ollama keeps data and analysis local.
- **Automated healing without approval**: rejected — incorrect remediation can
  worsen incidents (ADR-008).
- **No RAG**: LLM would hallucinate about a codebase it has never seen.

## Consequences

### Advantages

- Diagnostics are grounded in real evidence (logs + JFR + symptom + knowledge).
- The AI's answer is structured and explainable, with reasoning and confidence.
- Human oversight prevents dangerous autonomous actions.
- All model traffic stays local (Ollama container).

### Trade-offs

- LLM output is non-deterministic; a graceful fallback path is required.
- Requires a vector store (pgvector) and RAG pipeline to manage, and local GPU/CPU
  cost for Ollama.
- Latency of analysis is bounded by model inference time plus evidence retrieval.

## Compliance

- No code path lets the AI persist or execute a healing action itself; healing is
  only reached through the explicit approval workflow.