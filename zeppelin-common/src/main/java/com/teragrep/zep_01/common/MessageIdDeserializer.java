package com.teragrep.zep_01.common;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public final class MessageIdDeserializer implements JsonDeserializer<MessageId>{

    @Override
    public MessageId deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        return new MessageIdImpl(json.getAsString());
    }
}
