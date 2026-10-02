package com.teragrep.zep_01.message;

import com.teragrep.zep_01.common.message.JsonMessage;
import com.teragrep.zep_01.notebook.Note;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;

import java.io.StringReader;
import java.util.Objects;

public final class NoteMessage implements JsonMessage {

    final String op;
    final String principal;
    final String ticket;
    final String roles;
    // This class must reside in zeppelin-zengine in order to get access to Note object.
    final Note note;

    public NoteMessage(String principal, String ticket, String roles, Note note){
        this.op = "NOTE";
        this.principal = principal;
        this.ticket = ticket;
        this.roles = roles;
        this.note = note;
    }

    @Override
    public String op() {
        return op;
    }

    @Override
    public String principal() {
        return principal;
    }

    @Override
    public String ticket() {
        return ticket;
    }

    @Override
    public String roles() {
        return roles;
    }

    @Override
    public JsonObject asJsonObject() {
        final JsonObjectBuilder json = Json.createObjectBuilder();
        try(JsonReader noteJsonReader = Json.createReader(new StringReader(note.toJson()))){
            json.add("op",op);
            json.add("data",Json.createObjectBuilder().add("note",noteJsonReader.readObject()));
            json.add("ticket",ticket);
            json.add("principal",principal);
            json.add("roles",roles);
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
            equals = Objects.equals(op, that.op) && Objects.equals(principal, that.principal) && Objects.equals(ticket, that.ticket) && Objects.equals(roles, that.roles) && Objects.equals(note, that.note);
        }
        return equals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, principal, ticket, roles, note);
    }
}
