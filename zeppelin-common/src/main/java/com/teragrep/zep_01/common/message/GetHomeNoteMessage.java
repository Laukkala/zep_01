package com.teragrep.zep_01.common.message;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

import java.util.Objects;

public final class GetHomeNoteMessage implements JsonMessage {

    final String op;
    final JsonValue data;
    final String principal;
    final String ticket;
    final String roles;

    public GetHomeNoteMessage(String principal, String ticket, String roles){
        this.op = "GET_HOME_NOTE";
        this.data = JsonValue.EMPTY_JSON_OBJECT;
        this.principal = principal;
        this.ticket = ticket;
        this.roles = roles;
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
    public JsonValue asJson() {
        final JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("op",op);
        json.add("data",data);
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
            final GetHomeNoteMessage that = (GetHomeNoteMessage) o;
            equals = Objects.equals(op, that.op) && Objects.equals(data, that.data) && Objects.equals(principal, that.principal) && Objects.equals(ticket, that.ticket) && Objects.equals(roles, that.roles);
        }
        return equals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, data, principal, ticket, roles);
    }
}
