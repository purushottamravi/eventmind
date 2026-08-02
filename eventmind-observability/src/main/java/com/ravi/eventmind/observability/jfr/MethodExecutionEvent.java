package com.ravi.eventmind.observability.jfr;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;

@Name("com.ravi.eventmind.MethodExecution")
@Category({"EventMind", "Application"})
@Label("Method Execution")
public class MethodExecutionEvent extends Event {

    @Label("Class Name")
    public String className;

    @Label("Method Name")
    public String methodName;

    @Label("Execution Time (ms)")
    public long executionTime;

    @Label("Status")
    public String status;

    @Label("Exception")
    public String exception;
}