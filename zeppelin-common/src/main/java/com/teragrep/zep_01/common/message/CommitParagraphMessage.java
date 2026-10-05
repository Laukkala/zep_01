package com.teragrep.zep_01.common.message;

import com.google.gson.Gson;
import com.teragrep.zep_01.common.exception.MalformedMessageException;
import jakarta.json.*;

import java.io.StringReader;
import java.util.Map;
import java.util.Objects;

public final class CommitParagraphMessage implements Message {

    final String op;
    // This class must reside in zeppelin-zengine in order to get access to Paragraph object.
    final JsonObject json;

    public CommitParagraphMessage(JsonObject json){
        this.op = "COMMIT_PARAGRAPH";
        this.json = json;
    }

    public String paragraphId() {
        final String jsonKey = "id";
        if(json.containsKey(jsonKey) && json.get(jsonKey).getValueType().equals(JsonValue.ValueType.STRING)){
            return json.getString(jsonKey);
        }
        else {
            throw new MalformedMessageException(op+" message does not contain a "+jsonKey+" field!");
        }
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

    public String title() {
        final String jsonKey = "title";
        if(json.containsKey(jsonKey) && json.get(jsonKey).getValueType().equals(JsonValue.ValueType.STRING)){
            return json.getString(jsonKey);
        }
        else {
            throw new MalformedMessageException(op+" message does not contain a "+jsonKey+" field!");
        }
    }

    public String paragraphText() {
        final String jsonKey = "paragraph";
        if(json.containsKey(jsonKey) && json.get(jsonKey).getValueType().equals(JsonValue.ValueType.STRING)){
            return json.getString(jsonKey);
        }
        else {
            throw new MalformedMessageException(op+" message does not contain a "+jsonKey+" field!");
        }
    }

    //TODO: Need to refactor 'config' field in Paragraph to take specific Config objects instead of Generic Map<String,Object>. Meanwhile we must rely on GSON's auto-parsing to maintain compatibility.
    public Map<String, Object> config() {
        return new Gson().fromJson(json.getJsonObject("config").toString(),Map.class);
    }
    //TODO: Need to refactor 'params' field in Paragraph to take specific Params objects instead of Generic Map<String,Object>. Meanwhile we must rely on GSON's auto-parsing to maintain compatibility.
    public Map<String, Object> params() {
        return new Gson().fromJson(json.getJsonObject("params").toString(),Map.class);
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
            return json.build();
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
            final CommitParagraphMessage that = (CommitParagraphMessage) o;
            equals = Objects.equals(op, that.op) && Objects.equals(json, that.json);
        }
        return equals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, json);
    }
}
