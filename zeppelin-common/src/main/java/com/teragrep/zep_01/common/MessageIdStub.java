package com.teragrep.zep_01.common;

import jakarta.json.JsonValue;

import java.util.Objects;

public final class MessageIdStub implements MessageId{

    private final boolean isStub;
    public MessageIdStub(){
        this(true);
    }

    private MessageIdStub(final boolean isStub){
        this.isStub = isStub;
    }

    @Override
    public boolean isStub() {
        return true;
    }

    @Override
    public JsonValue asJson() {
        throw new IllegalStateException("Cannot turn a MessageIdStub into JSON!");
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final MessageIdStub that = (MessageIdStub) o;
        return isStub == that.isStub;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isStub);
    }
}
