package com.teragrep.zep_01.common.message;

import com.teragrep.zep_01.common.MessageId;
import jakarta.json.JsonObject;

public interface Message {
    public String op();
    public JsonObject asJson();
    public MessageId msgId();
}
