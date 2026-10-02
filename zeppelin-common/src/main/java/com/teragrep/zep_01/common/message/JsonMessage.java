package com.teragrep.zep_01.common.message;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public interface JsonMessage {
    public String op();
    public String ticket();
    public String principal();
    public String roles();
    public JsonObject asJsonObject();
}
