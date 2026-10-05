package com.teragrep.zep_01.common.message;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public final class IdentifiableMessageTest {

    @Test
    void decorationTest() {
        final String expectedMsgId = "msgId";

        final String expectedOp = "GET_HOME_NOTE";
        final JsonValue expectedData = JsonValue.EMPTY_JSON_OBJECT;

        JsonObject expectedJson = Json.createObjectBuilder()
                .add("op",expectedOp)
                .add("data",expectedData)
                .add("msgId",expectedMsgId)
                .build();

        JsonObject inputJson = Json.createObjectBuilder().build();
        final Message originalMessage = new GetHomeNoteMessage(inputJson);

        final IdentifiableMessage decoratedMessage = new IdentifiableMessage(originalMessage,expectedMsgId);
        final JsonObject decoratedJson = Assertions.assertDoesNotThrow(()->decoratedMessage.asJson());
        Assertions.assertEquals(expectedJson,decoratedJson);
    }
}