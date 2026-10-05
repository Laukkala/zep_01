package com.teragrep.zep_01.common.message;

import com.teragrep.zep_01.common.MessageId;
import com.teragrep.zep_01.common.exception.MalformedMessageException;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

import java.util.Objects;

public final class GetNoteMessage implements Message {

    final String op;
    final JsonObject json;
    final MessageId msgId;

    public GetNoteMessage(JsonObject json, MessageId msgId){
        this.op = "GET_NOTE";
        this.json = json;
        this.msgId = msgId;
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
        if(!msgId.isStub()){
            json.add("msgId",msgId.asJson());
        }
        return json.build();
    }

    @Override
    public MessageId msgId() {
        return msgId;
    }

    public String noteId() {
        if(!json.containsKey("data") || !json.get("data").getValueType().equals(JsonValue.ValueType.OBJECT)){
            throw new MalformedMessageException(op+" message does not contain a data field!");
        }
        JsonObject payload = json.getJsonObject("data");
        final String jsonKey = "noteId";
        if(payload.containsKey(jsonKey) && payload.get(jsonKey).getValueType().equals(JsonValue.ValueType.STRING)){
            return payload.getString(jsonKey);
        }
        else {
            throw new MalformedMessageException(op+" message data does not contain a "+jsonKey+" field!");
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
            equals = Objects.equals(op, that.op) && Objects.equals(json, that.json) && Objects.equals(msgId, that.msgId);
        }
        return equals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, json, msgId);
    }
}
