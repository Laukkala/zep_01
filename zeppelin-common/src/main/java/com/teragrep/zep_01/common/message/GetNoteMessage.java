package com.teragrep.zep_01.common.message;

import com.teragrep.zep_01.common.exception.MalformedMessageException;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

import java.util.Objects;

public final class GetNoteMessage implements Message {

    final String op;
    final JsonObject json;

    public GetNoteMessage(JsonObject json){
        this.op = "GET_NOTE";
        this.json = json;
    }

    @Override
    public String op() {
        return op;
    }

    @Override
    public JsonObject asJson() {
        final JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("op",op);
        json.add("data",Json.createObjectBuilder().add("noteId",noteId()));
        return json.build();
    }

    public String noteId() {
        final String jsonKey = "noteId";
        if(json.containsKey(jsonKey) && json.get(jsonKey).getValueType().equals(JsonValue.ValueType.STRING)){
            return json.getString(jsonKey);
        }
        else {
            throw new MalformedMessageException(op+" message does not contain a "+jsonKey+" field!");
        }
    }

    @Override
    public boolean equals(final Object o) {
        final boolean equals;
        if (this == o) {
            equals = true;
        } else if (o == null || getClass() != o.getClass()) {
            equals = false;
        } else {
            final GetNoteMessage that = (GetNoteMessage) o;
            equals = Objects.equals(op, that.op) && Objects.equals(json, that.json);
        }
        return equals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, json);
    }
}
