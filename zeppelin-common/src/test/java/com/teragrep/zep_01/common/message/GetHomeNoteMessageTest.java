package com.teragrep.zep_01.common.message;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class GetHomeNoteMessageTest {

    @Test
    public void testSerialization(){

        final String expectedOp = "GET_HOME_NOTE";
        final JsonValue expectedData = JsonValue.EMPTY_JSON_OBJECT;

        JsonObject expectedJson = Json.createObjectBuilder()
                .add("op",expectedOp)
                .add("data",expectedData)
                .build();

        JsonObject inputJson = Json.createObjectBuilder().build();

        final GetHomeNoteMessage getHomeNoteMessage = new GetHomeNoteMessage(inputJson);
        Assertions.assertEquals(expectedOp,getHomeNoteMessage.op);
        Assertions.assertEquals(expectedJson,getHomeNoteMessage.asJson());
    }
    @Test
    public void testContract(){
        EqualsVerifier.forClass(GetHomeNoteMessage.class).verify();
    }
}