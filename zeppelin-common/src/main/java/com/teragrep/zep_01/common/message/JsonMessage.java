package com.teragrep.zep_01.common.message;

import com.teragrep.zep_01.common.Jsonable;
import jakarta.json.JsonValue;

public interface JsonMessage extends Jsonable {
    public String op();
    public String ticket();
    public String principal();
    public String roles();
}
