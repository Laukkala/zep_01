package com.teragrep.zep_01.common.message;

import jakarta.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public final class IdentifiableMessageTest {

    @Test
    void decorationTest() {
        final String principal = "anonymous";
        final String ticket = "anonymous";
        final String roles = "";
        final String messageId = "msgId";
        final JsonMessage originalMessage = new GetHomeNoteMessage(principal,ticket,roles);
        final IdentifiableMessage decoratedMessage = new IdentifiableMessage(originalMessage,messageId);
        final JsonObject decoratedJson = Assertions.assertDoesNotThrow(()->decoratedMessage.asJson().asJsonObject());
        Assertions.assertEquals(principal,decoratedJson.getString("principal"));
        Assertions.assertEquals(ticket,decoratedJson.getString("ticket"));
        Assertions.assertEquals(roles,decoratedJson.getString("roles"));
        Assertions.assertEquals(messageId,decoratedJson.getString("msgId"));
    }
}