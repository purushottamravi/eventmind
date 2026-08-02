package com.ravi.eventmind.observability.jfr;

import com.ravi.eventmind.observability.jfr.controller.JfrController;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Makes sure the controller just hands off to the analyzer and passes along whatever it returns. Simple as that.
 */
class JfrControllerTest {

    @Test
    void report_shouldDelegateToAnalyzer() {
        JfrAnalyzer analyzer = mock(JfrAnalyzer.class);
        when(analyzer.analyze()).thenReturn("JFR REPORT (generated 2026-01-01T00:00:00Z)\n");

        String result = new JfrController(analyzer).report();

        assertEquals("JFR REPORT (generated 2026-01-01T00:00:00Z)\n", result);
    }
}
