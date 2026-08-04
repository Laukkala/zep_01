package com.teragrep.zep_01.common;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;

public final class MessageTest {

    @Test
    public void msgIdtest(){
        final String testId = "testId";
        final MessageIdImpl id = new MessageIdImpl(testId);
        final Message message = new Message(Message.OP.PING, id, new HashMap<>(),"anonymous","anonymous","");
        Assertions.assertEquals(testId,message.msgId().asString());
    }

    @Test
    public void gsonSerializationTest(){
        final String testId = "testId";
        final MessageIdImpl id = new MessageIdImpl(testId);
        final Message message1 = new Message(Message.OP.PING, id, new HashMap<>(),"anonymous","anonymous","");
        final String message1Serialized = message1.toJson();

        final Message message2 = new Message(Message.OP.PING);
        final String message2Serialized = message2.toJson();

        final Message message3 = Message.fromJson(message1Serialized);
        final Message message4 = Message.fromJson(message2Serialized);

        Assertions.assertEquals(message1,message3);
        Assertions.assertEquals(message2,message4);
    }

    @Test
    public void testContract(){
        EqualsVerifier.forClass(Message.class).verify();
    }
}