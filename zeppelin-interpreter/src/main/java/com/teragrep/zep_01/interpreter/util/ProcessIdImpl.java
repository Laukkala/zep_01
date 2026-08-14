package com.teragrep.zep_01.interpreter.util;

public class ProcessIdImpl implements ProcessId {

    private final long pid;

    public ProcessIdImpl(long pid){
        this.pid = pid;
    }

    @Override
    public long asLong() {
        return pid;
    }

    @Override
    public boolean isStub() {
        return false;
    }
}
