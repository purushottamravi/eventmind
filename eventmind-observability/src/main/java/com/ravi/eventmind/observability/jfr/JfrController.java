package com.ravi.eventmind.observability.jfr;

import com.ravi.eventmind.shared.annotations.AuditLog;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Surfaces the live JFR report produced by {@link JfrAnalyzer}.
 *
 * <p>The {@link AuditLog} annotation makes the shared {@code AuditAspect} fire on every
 * report generation: the call is recorded into {@code APPLICATION_LOG} and a
 * {@code MethodExecutionEvent} is committed to the JFR stream, which the listener
 * then feeds back into the report.</p>
 */
@RestController
@RequestMapping("/jfr")
public class JfrController {

    private final JfrAnalyzer jfrAnalyzer;

    public JfrController(JfrAnalyzer jfrAnalyzer) {
        this.jfrAnalyzer = jfrAnalyzer;
    }

    @GetMapping(value = "/report", produces = MediaType.TEXT_PLAIN_VALUE)
    @AuditLog("Generate JFR report")
    public String report() {
        return jfrAnalyzer.analyze();
    }
}
