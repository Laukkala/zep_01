package com.teragrep.zep_01.interpreter.status;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public class InterpreterStatusImpl implements InterpreterStatus {
    private final String state;
    private final long memoryUsed; //TODO: Turn fields into objects with Stubs
    private final long uptime;
    private final double cpuLoad;

    /**
     * Snapshot of an Interpreter's status. Includes performance fields if available
     * @param state State of the Interpreter (offline, online)
     * @param memoryUsed Memory usage in MB
     * @param uptime Uptime in milliseconds
     * @param cpuLoad CPU load %
     */
    public InterpreterStatusImpl(String state, long memoryUsed, long uptime, double cpuLoad){
        this.state = state;
        this.memoryUsed = memoryUsed;
        this.uptime = uptime;
        this.cpuLoad = cpuLoad;
    }
    @Override
    public boolean isStub() {
        return false;
    }

    @Override
    public JsonObject asJson() {
        return Json.createObjectBuilder()
                .add("state",state)
                .add("memoryUsed",memoryUsed)
                .add("uptime",uptime)
                .add("cpuLoad",cpuLoad)
                .build();
    }

}
