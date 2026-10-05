package com.teragrep.zep_01.common.message;

import com.teragrep.zep_01.common.MessageId;
import com.teragrep.zep_01.common.exception.MalformedMessageException;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

import java.util.Objects;

public final class GetHomeNoteMessage implements Message {

    final JsonObject json;
    final String op;
    final MessageId msgId;

    public GetHomeNoteMessage(JsonObject json, MessageId msgId){
        this.json = json;
        this.op = "GET_HOME_NOTE";
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
        json.add("data",Json.createObjectBuilder());
        if(!msgId.isStub()){
            json.add("msgId",msgId.asJson());
        }
        return json.build();
    }

    @Override
    public MessageId msgId() {
        return msgId;
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
            equals = Objects.equals(json, that.json) && Objects.equals(op, that.op) && Objects.equals(msgId, that.msgId);
        }
        return equals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(json, op, msgId);
    }
}
