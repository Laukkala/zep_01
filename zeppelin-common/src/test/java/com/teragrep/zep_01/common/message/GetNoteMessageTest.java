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
        final JsonValue expectedData = Json.createObjectBuilder().add("id",expectedNoteId).build();
        final String expectedPrincipal = "anonymous";
        final String expectedTicket = "anonymous";
        final String expectedRoles = "";

        JsonObject json = Json.createObjectBuilder()
                .add("op",expectedOp)
                .add("data",expectedData)
                .add("principal",expectedPrincipal)
                .add("ticket",expectedTicket)
                .add("roles",expectedRoles).build();

        final GetNoteMessage getNoteMessage = new GetNoteMessage(expectedPrincipal,expectedTicket,expectedRoles,expectedNoteId);
        Assertions.assertEquals(json,getNoteMessage.asJsonObject());
    }

    @Test
    public void testContract(){
        EqualsVerifier.forClass(GetNoteMessage.class).verify();
    }
}