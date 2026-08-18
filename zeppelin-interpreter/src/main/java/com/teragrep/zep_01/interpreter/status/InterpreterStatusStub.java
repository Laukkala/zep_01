package com.teragrep.zep_01.interpreter.status;

import jakarta.json.JsonObject;

public class InterpreterStatusStub implements InterpreterStatus {

    private final boolean isStub;
    public InterpreterStatusStub(){
        this.isStub = true;
    }
    @Override
    public boolean isStub() {
        return isStub;
    }

    @Override
    public JsonObject asJson() {
        throw new RuntimeException("InterpreterStatus is a Stub!");
    }

}
