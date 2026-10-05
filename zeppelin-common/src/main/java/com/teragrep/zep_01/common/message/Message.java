package com.teragrep.zep_01.common.message;

import jakarta.json.JsonObject;

public interface Message {
    public String op();
    public JsonObject asJson();
}
