package com.teragrep.zep_01.common.message;

import com.teragrep.zep_01.common.exception.MalformedMessageException;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

import java.util.Objects;

public final class GetHomeNoteMessage implements Message {

    final JsonObject json;
    final String op;

    public GetHomeNoteMessage(JsonObject json){
        this.json = json;
        this.op = "GET_HOME_NOTE";
    }

    @Override
    public String op() {
        return op;
    }

    @Override
    public JsonObject asJson() {
        final JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("op",op);
        json.add("data",Json.createObjectBuilder());
        return json.build();
    }

    @Override
    public boolean equals(final Object o) {
        final boolean equals;
        if (this == o) {
            equals = true;
        } else if (o == null || getClass() != o.getClass()) {
            equals = false;
        } else {
            final GetHomeNoteMessage that = (GetHomeNoteMessage) o;
            equals = Objects.equals(json, that.json) && Objects.equals(op, that.op);
        }
        return equals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(json, op);
    }
}
