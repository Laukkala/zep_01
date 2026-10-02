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
        final String expectedPrincipal = "anonymous";
        final String expectedTicket = "anonymous";
        final String expectedRoles = "";

        JsonObject json = Json.createObjectBuilder()
                .add("op",expectedOp)
                .add("data",expectedData)
                .add("principal",expectedPrincipal)
                .add("ticket",expectedTicket)
                .add("roles",expectedRoles).build();

        final ReloadNoteMessage reloadNoteMessage = new ReloadNoteMessage(expectedPrincipal,expectedTicket,expectedRoles,expectedNoteId);
        Assertions.assertEquals(json,reloadNoteMessage.asJsonObject());
    }

    @Test
    public void testContract(){
        EqualsVerifier.forClass(ReloadNoteMessage.class).verify();
    }
}