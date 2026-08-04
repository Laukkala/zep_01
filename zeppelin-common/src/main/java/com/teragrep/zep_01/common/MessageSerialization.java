package com.teragrep.zep_01.common;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public final class MessageSerialization implements JsonDeserializer<Message>, JsonSerializer<Message> {

    @Override
    public Message deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        Gson gson = new Gson();
        JsonObject jsonObject = json.getAsJsonObject();
        Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
        if(jsonObject.has("msgId")){
            return new Message(Message.OP.valueOf(jsonObject.get("op").getAsString()),
                    new MessageIdImpl(jsonObject.get("msgId").getAsString()),
                    gson.fromJson(jsonObject.get("data").getAsJsonObject(),mapType),
                    jsonObject.get("principal").getAsString(),
                    jsonObject.get("ticket").getAsString(),
                    jsonObject.get("roles").getAsString());
        }
        else {
                return new Message(Message.OP.valueOf(jsonObject.get("op").getAsString()),
                        new MessageIdStub(),
                        gson.fromJson(jsonObject.get("data").getAsJsonObject(),mapType),
                        jsonObject.get("principal").getAsString(),
                        jsonObject.get("ticket").getAsString(),
                        jsonObject.get("roles").getAsString());
        }
    }
    @Override
    public JsonElement serialize(final Message src, final Type typeOfSrc, final JsonSerializationContext context) {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        if(!src.msgId().isStub()){
            jsonObject.addProperty("msgId",src.msgId().asString());
        }
        jsonObject.addProperty("op",src.op.name());
        jsonObject.add("data",gson.toJsonTree(src.data()));
        jsonObject.addProperty("principal",src.principal());
        jsonObject.addProperty("ticket",src.ticket());
        jsonObject.addProperty("roles",src.roles());
        return jsonObject;
    }
}
