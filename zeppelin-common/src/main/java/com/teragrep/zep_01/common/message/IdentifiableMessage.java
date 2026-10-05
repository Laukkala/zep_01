package com.teragrep.zep_01.common.message;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public final class IdentifiableMessage implements Message {

    private final Message origin;
    private final String id;
    public IdentifiableMessage(final Message origin, final String id){
        this.origin = origin;
        this.id = id;
    }

    @Override
    public String op() {
        return origin.op();
    }

    @Override
    public JsonObject asJson() {
        final JsonObjectBuilder builder = Json.createObjectBuilder(origin.asJson());
        builder.add("msgId",id);
        return builder.build();
    }
}
