package com.teragrep.zep_01.common.message;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class GetNoteMessageTest {

    @Test
    public void testSerialization(){

        final String expectedOp = "GET_NOTE";
        final String expectedNoteId = "testNote";
        final JsonValue expectedData = Json.createObjectBuilder().add("noteId",expectedNoteId).build();
        JsonObject expectedJson = Json.createObjectBuilder()
                .add("op",expectedOp)
                .add("data",expectedData)
                .build();

        JsonObject inputJson = Json.createObjectBuilder()
                .add("noteId",expectedNoteId).build();

        final GetNoteMessage getNoteMessage = new GetNoteMessage(inputJson);
        Assertions.assertEquals(expectedJson,getNoteMessage.asJson());
    }

    @Test
    public void testContract(){
        EqualsVerifier.forClass(GetNoteMessage.class).verify();
    }
}