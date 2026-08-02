package com.ravi.eventmind.observability.jfr.controller;

import com.ravi.eventmind.observability.jfr.JfrAnalyzer;
import com.ravi.eventmind.shared.annotations.AuditLog;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the live JFR report produced by {@link JfrAnalyzer} over HTTP. The
 * {@link AuditLog} tag means each request gets recorded into the application log
 * and committed back into the JFR stream via the listener.
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
