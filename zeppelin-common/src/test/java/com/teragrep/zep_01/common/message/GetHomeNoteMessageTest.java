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
        final String expectedPrincipal = "anonymous";
        final String expectedTicket = "anonymous";
        final String expectedRoles = "";

        JsonObject json = Json.createObjectBuilder()
                .add("op",expectedOp)
                .add("data",expectedData)
                .add("principal",expectedPrincipal)
                .add("ticket",expectedTicket)
                .add("roles",expectedRoles).build();

        final GetHomeNoteMessage getHomeNoteMessage = new GetHomeNoteMessage(expectedPrincipal,expectedTicket,expectedRoles);
        Assertions.assertEquals(json,getHomeNoteMessage.asJsonObject());
    }
    @Test
    public void testContract(){
        EqualsVerifier.forClass(GetHomeNoteMessage.class).verify();
    }
}