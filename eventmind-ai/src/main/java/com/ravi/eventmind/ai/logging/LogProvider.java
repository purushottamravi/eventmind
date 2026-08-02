package com.ravi.eventmind.ai.logging;

import java.util.List;

/**
 * Read port for the AI module's application log store.
 */
public interface LogProvider {

    List<LogEntry> recentLogs(int limit);
}
