package com.ravi.eventmind.ai.logging;

import java.util.List;

/**
 * How the AI module reads back from its application log store.
 */
public interface LogProvider {

    List<LogEntry> recentLogs(int limit);
}
