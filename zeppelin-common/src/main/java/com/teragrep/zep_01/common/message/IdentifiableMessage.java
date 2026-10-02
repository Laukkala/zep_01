package com.teragrep.zep_01.common.message;

import jakarta.json.Json;
import jakarta.json.JsonObject;
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
    public JsonObject asJsonObject() {
        final JsonObjectBuilder builder = Json.createObjectBuilder(origin.asJsonObject());
        builder.add("msgId",id);
        return builder.build();
    }
}
