package com.teragrep.zep_01.interpreter.util;

public class ProcessIdStub implements ProcessId {
    @Override
    public long asLong() {
        throw new RuntimeException("ProcessId is a stub!");
    }

    @Override
    public boolean isStub() {
        return true;
    }
}
