package com.teragrep.zep_01.message;

import com.teragrep.zep_01.common.MessageId;
import com.teragrep.zep_01.common.message.Message;
import com.teragrep.zep_01.notebook.Paragraph;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;

import java.io.StringReader;
import java.util.Objects;

public final class ParagraphMessage implements Message {

    final String op;
    // This class must reside in zeppelin-zengine in order to get access to Paragraph object.
    final Paragraph paragraph;
    final MessageId msgId;

    public ParagraphMessage(Paragraph paragraph, MessageId msgId){
        this.op = "PARAGRAPH";
        this.paragraph = paragraph;
        this.msgId = msgId;
    }

    @Override
    public String op() {
        return op;
    }

    @Override
    public JsonObject asJson() {
        final JsonObjectBuilder json = Json.createObjectBuilder();
        try(JsonReader paragraphJsonReader = Json.createReader(new StringReader(paragraph.toJson()))){
            json.add("op",op);
            json.add("data",Json.createObjectBuilder().add("paragraph",paragraphJsonReader.readObject()));
            return json.build();
        }
    }

    @Override
    public MessageId msgId() {
        return msgId;
    }

    @Override
    public boolean equals(final Object o) {
        final boolean equals;
        if (this == o) {
            equals = true;
        } else if (o == null || getClass() != o.getClass()) {
            equals = false;
        } else {
            final ParagraphMessage that = (ParagraphMessage) o;
            equals = Objects.equals(op, that.op) && Objects.equals(paragraph, that.paragraph) && Objects.equals(msgId, that.msgId);
        }
        return equals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, paragraph, msgId);
    }
}
