package com.teragrep.zep_01.common;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return MessageIdStub.class.hashCode();
    }
}
