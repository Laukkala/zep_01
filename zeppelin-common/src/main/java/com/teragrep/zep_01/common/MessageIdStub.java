package com.teragrep.zep_01.common;

public final class MessageIdStub implements MessageId {

    public MessageIdStub(){

    }
    @Override
    public String asString() {
        throw new RuntimeException("MessageIdStub does not implement asString()");
    }

    @Override
    public boolean isStub() {
        return true;
    }
}
