package com.ravi.eventmind.observability.jfr.event;

import jdk.jfr.Event;
import jdk.jfr.Label;

/**
 * A moment of truth captured from the JVM while a command ran. Nothing lives here, it's just what happened.
 */
public class CommandExecutionEvent extends Event {

    @Label("Command Name")
    String commandName;

    @Label("Aggregate Id")
    Long aggregateId;

    public CommandExecutionEvent(String commandName, Long aggregateId) {
        this.commandName = commandName;
        this.aggregateId = aggregateId;
    }
}
