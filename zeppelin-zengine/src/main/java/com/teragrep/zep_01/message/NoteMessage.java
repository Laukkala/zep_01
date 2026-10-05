package com.teragrep.zep_01.message;

import com.teragrep.zep_01.common.message.Message;
import com.teragrep.zep_01.notebook.Note;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;

import java.io.StringReader;
import java.util.Objects;

public final class NoteMessage implements Message {

    final String op;
    // This class must reside in zeppelin-zengine in order to get access to Note object.
    final Note note;

    public NoteMessage(Note note){
        this.op = "NOTE";
        this.note = note;
    }

    @Override
    public String op() {
        return op;
    }

    @Override
    public JsonObject asJson() {
        final JsonObjectBuilder json = Json.createObjectBuilder();
        try(JsonReader noteJsonReader = Json.createReader(new StringReader(note.toJson()))){
            json.add("op",op);
            json.add("data",Json.createObjectBuilder().add("note",noteJsonReader.readObject()));
            return json.build();
        }
    }

    @Override
    public boolean equals(final Object o) {
        final boolean equals;
        if (this == o) {
            equals = true;
        } else if (o == null || getClass() != o.getClass()) {
            equals = false;
        } else {
            final NoteMessage that = (NoteMessage) o;
            equals = Objects.equals(op, that.op) && Objects.equals(note, that.note);
        }
        return equals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, note);
    }
}
