package com.teragrep.zep_01.common;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public final class MessageTest {

    @Test
    public void msgIdtest(){
        final String testId = "testId";
        final MessageIdImpl id = new MessageIdImpl(testId);
        final Message message = new Message(Message.OP.PING, id);
        Assertions.assertEquals(testId,message.msgId().asString());
    }

    @Test
    public void gsonSerializationTest(){
        final String testId = "testId";
        final MessageIdImpl id = new MessageIdImpl(testId);
        final Message message = new Message(Message.OP.PING, id);
        final String message1Serialized = message.toJson();

        final Message message2 = new Message(Message.OP.PING);
        final String message2Serialized = message2.toJson();

        final Message message3 = Message.fromJson(message1Serialized);
        final Message message4 = Message.fromJson(message2Serialized);

        Assertions.assertEquals(message.toString(),message3.toString());
        Assertions.assertEquals(message2.toString(),message4.toString());
    }
}