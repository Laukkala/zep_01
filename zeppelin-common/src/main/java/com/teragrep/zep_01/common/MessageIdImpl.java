package com.teragrep.zep_01.common;

import com.google.gson.*;

import javax.xml.crypto.Data;
import java.lang.reflect.Type;
import java.util.Objects;

public class MessageIdImpl implements MessageId {

    private final String id;

    // No-args constructor required for GSON
    public MessageIdImpl(){
        this.id = null;
    }
    public MessageIdImpl(String id){
        this.id = id;
    }
    @Override
    public String asString() {
        return id;
    }

    @Override
    public boolean isStub() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageIdImpl messageId = (MessageIdImpl) o;
        return Objects.equals(id, messageId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
