package com.teragrep.zep_01.common.message;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class ReloadNoteMessageTest {

    @Test
    public void testSerialization(){

        final String expectedOp = "RELOAD_NOTE";
        final String expectedNoteId = "testNote";
        final JsonValue expectedData = Json.createObjectBuilder().add("id",expectedNoteId).build();
        JsonObject expectedJson = Json.createObjectBuilder()
                .add("op",expectedOp)
                .add("data",expectedData)
                .build();


        JsonObject inputJson = Json.createObjectBuilder()
                .add("noteId",expectedNoteId).build();

        final ReloadNoteMessage reloadNoteMessage = new ReloadNoteMessage(inputJson);
        Assertions.assertEquals(expectedJson,reloadNoteMessage.asJson());
    }

    @Test
    public void testContract(){
        EqualsVerifier.forClass(ReloadNoteMessage.class).verify();
    }
}