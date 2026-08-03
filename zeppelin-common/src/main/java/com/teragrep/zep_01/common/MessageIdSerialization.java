package com.teragrep.zep_01.common;

import com.google.gson.*;

import java.lang.reflect.Type;

public final class MessageIdSerialization implements JsonDeserializer<MessageId>, JsonSerializer<MessageId> {

    @Override
    public MessageId deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        return new MessageIdImpl(json.getAsString());
    }
    @Override
    public JsonElement serialize(final MessageId src, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonElement jsonElement;
        if(src.isStub()){
            jsonElement = JsonNull.INSTANCE;
        }
        else {
            jsonElement = new JsonPrimitive(src.asString());
        }
        return jsonElement;
    }
}
