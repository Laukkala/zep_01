package com.teragrep.zep_01.common.message;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

import java.util.Objects;

public final class GetNoteMessage implements JsonMessage {

    final String op;
    final String principal;
    final String ticket;
    final String roles;
    final String noteId;

    public GetNoteMessage(String principal, String ticket, String roles, String noteId){
        this.op = "GET_NOTE";
        this.principal = principal;
        this.ticket = ticket;
        this.roles = roles;
        this.noteId = noteId;
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
        json.add("op",op);
        json.add("data",Json.createObjectBuilder().add("id",noteId));
        json.add("ticket",ticket);
        json.add("principal",principal);
        json.add("roles",roles);
        return json.build();
    }

    @Override
    public boolean equals(final Object o) {
        final boolean equals;
        if (this == o) {
            equals = true;
        } else if (o == null || getClass() != o.getClass()) {
            equals = false;
        } else {
            final GetNoteMessage that = (GetNoteMessage) o;
            equals = Objects.equals(op, that.op) && Objects.equals(principal, that.principal) && Objects.equals(ticket, that.ticket) && Objects.equals(roles, that.roles) && Objects.equals(noteId, that.noteId);
        }
        return equals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, principal, ticket, roles, noteId);
    }
}
