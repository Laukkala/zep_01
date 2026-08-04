package com.teragrep.zep_01.common;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public final class MessageIdImplTest {
    @Test
    void testAsString() {
        final String id = "testId";
        final MessageIdImpl messageId = new MessageIdImpl(id);
        Assertions.assertEquals(id,messageId.asString());
    }

    @Test
    void testContract(){
        EqualsVerifier.forClass(MessageIdImpl.class);
    }
}