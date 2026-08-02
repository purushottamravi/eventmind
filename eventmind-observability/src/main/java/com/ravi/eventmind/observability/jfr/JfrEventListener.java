package com.ravi.eventmind.observability.jfr;

import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.Lifecycle;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Streams live Java Flight Recorder events from the running JVM and records the
 * signal into a shared {@link JfrReport}.
 *
 * <p>Subscribed event types:
 * <ul>
 *     <li>{@code jdk.GC.GarbageCollection} - GC pause counts, totals and worst pause;</li>
 *     <li>{@code jdk.CPULoad} - latest machine and JVM CPU load;</li>
 *     <li>{@code jdk.JavaExceptionThrow} - thrown exception counts by class;</li>
 *     <li>{@code com.ravi.eventmind.MethodExecution} - the custom event committed by
 *     {@code AuditAspect} around {@code @AuditLog} methods.</li>
 * </ul>
 *
 * <p>The stream runs on a daemon thread for the lifetime of the context (started/stopped
 * through the Spring {@link Lifecycle} contract), so the report stays always-on without
 * requiring a {@code -XX:StartFlightRecording} file.</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "eventmind.observability.jfr", name = "enabled", havingValue = "true", matchIfMissing = true)
public class JfrEventListener implements Lifecycle {

    private final JfrReport report;
    private volatile RecordingStream stream;
    private volatile Thread streamThread;
    private volatile boolean running;

    public JfrEventListener(JfrReport report) {
        this.report = report;
    }

    @Override
    public synchronized void start() {
        if (running) {
            return;
        }
        RecordingStream newStream = new RecordingStream();
        newStream.enable("jdk.GC.GarbageCollection").withPeriod(Duration.ZERO);
        newStream.enable("jdk.CPULoad").withPeriod(Duration.ofSeconds(1));
        newStream.enable("jdk.JavaExceptionThrow");
        newStream.enable(JfrReport.METHOD_EXECUTION_EVENT);

        newStream.onEvent("jdk.GC.GarbageCollection", this::onGarbageCollection);
        newStream.onEvent("jdk.CPULoad", this::onCpuLoad);
        newStream.onEvent("jdk.JavaExceptionThrow", this::onExceptionThrown);
        newStream.onEvent(JfrReport.METHOD_EXECUTION_EVENT, this::onMethodExecution);

        Thread thread = new Thread(newStream::start, "jfr-event-listener");
        thread.setDaemon(true);
        this.stream = newStream;
        this.streamThread = thread;
        thread.start();
        running = true;
        log.info("JFR event listener started (recording stream active)");
    }

    @Override
    public synchronized void stop() {
        if (!running) {
            return;
        }
        running = false;
        RecordingStream current = stream;
        stream = null;
        if (current != null) {
            current.close();
        }
        Thread thread = streamThread;
        streamThread = null;
        if (thread != null) {
            try {
                thread.join(Duration.ofSeconds(1).toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        log.info("JFR event listener stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private void onGarbageCollection(RecordedEvent event) {
        try {
            report.recordGc(event.getDuration().toMillis(), event.getString("gcCause"));
        } catch (Exception e) {
            log.warn("Could not parse GC JFR event", e);
        }
    }

    private void onCpuLoad(RecordedEvent event) {
        try {
            report.recordCpuLoad(event.getFloat("machineTotal"), event.getFloat("jvmUser"), event.getFloat("jvmSystem"));
        } catch (Exception e) {
            log.warn("Could not parse CPULoad JFR event", e);
        }
    }

    private void onExceptionThrown(RecordedEvent event) {
        try {
            report.recordException(event.getString("throwableClass"));
        } catch (Exception e) {
            log.warn("Could not parse exception JFR event", e);
        }
    }

    private void onMethodExecution(RecordedEvent event) {
        try {
            boolean failed = "FAILED".equals(event.getString("status"));
            report.recordMethodExecution(event.getLong("executionTime"),
                    event.getString("className"), event.getString("methodName"), failed);
        } catch (Exception e) {
            log.warn("Could not parse MethodExecution JFR event", e);
        }
    }
}
