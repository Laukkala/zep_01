package com.teragrep.zep_01.common.message;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;

public final class IdentifiableMessage implements JsonMessage{

    private final JsonMessage origin;
    private final String id;
    public IdentifiableMessage(final JsonMessage origin, final String id){
        this.origin = origin;
        this.id = id;
    }

    @Override
    public String op() {
        return origin.op();
    }

    @Override
    public String ticket() {
        return origin.ticket();
    }

    @Override
    public String principal() {
        return origin.principal();
    }

    @Override
    public String roles() {
        return origin.roles();
    }

    @Override
    public JsonValue asJson() {
        final JsonValue value = origin.asJson();
        if(!value.getValueType().equals(JsonValue.ValueType.OBJECT)){
            throw new RuntimeException("Decorated Message's Json type is not of JsonObject!");
        }
        final JsonObjectBuilder builder = Json.createObjectBuilder(origin.asJson().asJsonObject());
        builder.add("msgId",id);
        return builder.build();
    }
}
