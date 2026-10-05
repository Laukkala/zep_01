package com.teragrep.zep_01.common.message;

import com.google.gson.Gson;
import com.teragrep.zep_01.common.MessageId;
import com.teragrep.zep_01.common.exception.MalformedMessageException;
import jakarta.json.*;

import java.io.StringReader;
import java.util.Map;
import java.util.Objects;

public final class CopyParagraphMessage implements Message {

    final String op;
    final MessageId msgId;
    final JsonObject json;

    public CopyParagraphMessage(JsonObject json, MessageId msgId){
        this.op = "COPY_PARAGRAPH";
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

    public String paragraphId() {
        if(!json.containsKey("data") || !json.get("data").getValueType().equals(JsonValue.ValueType.OBJECT)){
            throw new MalformedMessageException(op+" message does not contain a data field!");
        }
        JsonObject payload = json.getJsonObject("data");
        final String jsonKey = "id";
        if(payload.containsKey(jsonKey) && payload.get(jsonKey).getValueType().equals(JsonValue.ValueType.STRING)){
            return payload.getString(jsonKey);
        }
        else {
            throw new MalformedMessageException(op+" message data does not contain a "+jsonKey+" field!");
        }
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

    public String title() {
        if(!json.containsKey("data") || !json.get("data").getValueType().equals(JsonValue.ValueType.OBJECT)){
            throw new MalformedMessageException(op+" message does not contain a data field!");
        }
        JsonObject payload = json.getJsonObject("data");
        final String jsonKey = "title";
        if(payload.containsKey(jsonKey) && payload.get(jsonKey).getValueType().equals(JsonValue.ValueType.STRING)){
            return payload.getString(jsonKey);
        }
        else {
            throw new MalformedMessageException(op+" message data does not contain a "+jsonKey+" field!");
        }
    }

    public String paragraphText() {
        if(!json.containsKey("data") || !json.get("data").getValueType().equals(JsonValue.ValueType.OBJECT)){
            throw new MalformedMessageException(op+" message does not contain a data field!");
        }
        JsonObject payload = json.getJsonObject("data");
        final String jsonKey = "paragraph";
        if(payload.containsKey(jsonKey) && payload.get(jsonKey).getValueType().equals(JsonValue.ValueType.STRING)){
            return payload.getString(jsonKey);
        }
        else {
            throw new MalformedMessageException(op+" message data does not contain a "+jsonKey+" field!");
        }
    }

    //TODO: Need to refactor 'config' field in Paragraph to take specific Config objects instead of Generic Map<String,Object>. Meanwhile we must rely on GSON's auto-parsing to maintain compatibility.
    public Map<String, Object> config() {
        return new Gson().fromJson(json.getJsonObject("data").getJsonObject("config").toString(),Map.class);
    }
    //TODO: Need to refactor 'params' field in Paragraph to take specific Params objects instead of Generic Map<String,Object>. Meanwhile we must rely on GSON's auto-parsing to maintain compatibility.
    public Map<String, Object> params() {
        return new Gson().fromJson(json.getJsonObject("data").getJsonObject("params").toString(),Map.class);
    }

    @Override
    public String op() {
        return op;
    }

    @Override
    public JsonObject asJson() {
        final JsonObjectBuilder json = Json.createObjectBuilder();
        try(JsonReader data = Json.createReader(new StringReader(paragraphText()))){
            json.add("op",op);
            json.add("data",Json.createObjectBuilder()
                            .add("id",paragraphId())
                            .add("noteId",noteId())
                            .add("paragraph",data.readObject())
                            .add("title",title())
                            .add("params",this.json.getJsonObject("params"))
                            .add("config",this.json.getJsonObject("config")));
            if(!msgId.isStub()){
                json.add("msgId",msgId.asJson());
            }
            return json.build();
        }
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
            final CopyParagraphMessage that = (CopyParagraphMessage) o;
            equals = Objects.equals(op, that.op) && Objects.equals(msgId, that.msgId) && Objects.equals(json, that.json);
        }
        return equals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, msgId, json);
    }
}
