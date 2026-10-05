package com.teragrep.zep_01.common.message;

import com.google.gson.Gson;
import com.teragrep.zep_01.common.MessageId;
import com.teragrep.zep_01.common.exception.MalformedMessageException;
import jakarta.json.*;

import java.io.StringReader;
import java.util.Map;
import java.util.Objects;

public final class InsertParagraphMessage implements Message {

    final String op;
    final MessageId msgId;
    final JsonObject json;

    public InsertParagraphMessage(JsonObject json, MessageId msgId){
        this.op = "INSERT_PARAGRAPH";
        this.json = json;
        this.msgId = msgId;
    }

    public int index() {
        if(!json.containsKey("data") || !json.get("data").getValueType().equals(JsonValue.ValueType.OBJECT)){
            throw new MalformedMessageException(op+" message does not contain a data field!");
        }
        JsonObject payload = json.getJsonObject("data");
        final String jsonKey = "index";
        if(payload.containsKey(jsonKey) && payload.get(jsonKey).getValueType().equals(JsonValue.ValueType.NUMBER)){
            return payload.getInt(jsonKey);
        }
        else {
            throw new MalformedMessageException(op+" message data does not contain a "+jsonKey+" field!");
        }
    }

    //TODO: Need to refactor 'config' field in Paragraph to take specific Config objects instead of Generic Map<String,Object>. Meanwhile we must rely on GSON's auto-parsing to maintain compatibility.
    public Map<String, Object> config() {
        return new Gson().fromJson(json.getJsonObject("config").toString(),Map.class);
    }

    @Override
    public String op() {
        return op;
    }

    @Override
    public JsonObject asJson() {
        final JsonObjectBuilder json = Json.createObjectBuilder();
            json.add("op",op);
            json.add("data",Json.createObjectBuilder()
                            .add("index",index()));
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
            final InsertParagraphMessage that = (InsertParagraphMessage) o;
            equals = Objects.equals(op, that.op) && Objects.equals(msgId, that.msgId) && Objects.equals(json, that.json);
        }
        return equals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, msgId, json);
    }
}
