package com.teragrep.zep_01.interpreter.status;

import com.teragrep.stb_01.Stubable;
import jakarta.json.JsonObject;

public interface InterpreterStatus extends Stubable {


    public JsonObject asJson();
}
