package com.ravi.eventmind.observability.jfr;

import jdk.jfr.Event;
import jdk.jfr.Label;

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
