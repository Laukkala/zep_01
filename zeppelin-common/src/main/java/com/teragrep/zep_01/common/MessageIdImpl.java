package com.teragrep.zep_01.common;

import com.google.gson.*;

import javax.xml.crypto.Data;
import java.lang.reflect.Type;

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
}
