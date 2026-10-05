package com.teragrep.zep_01.common.message;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class AuthorizedMessage implements Message{
    private final Message origin;
    private final String principal;
    private final String ticket;
    private final String roles;

    public AuthorizedMessage(Message origin, String principal, String ticket, String roles) {
        this.principal = principal;
        this.ticket = ticket;
        this.roles = roles;
        this.origin = origin;
    }

    public String principal() {
        return principal;
    }

    public String ticket() {
        return ticket;
    }

    public String roles() {
        return roles;
    }

    @Override
    public String op() {
        return origin.op();
    }

    @Override
    public JsonObject asJson() {
        final JsonObjectBuilder builder = Json.createObjectBuilder(origin.asJson());
        builder.add("principal",principal());
        builder.add("ticket",ticket());
        builder.add("roles",roles());
        return builder.build();
    }
}
