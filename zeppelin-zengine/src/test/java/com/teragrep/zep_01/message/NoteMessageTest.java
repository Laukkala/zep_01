package com.teragrep.zep_01.message;

import com.teragrep.zep_01.notebook.Note;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class NoteMessageTest {

    @Test
    public void testSerialization(){
        String expectedOp = "NOTE";
        String expectedPrincipal = "anonymous";
        String expectedTicket = "anonymous";
        String expectedRoles = "";
        Note note = new Note();
        String expectedNoteId = note.getId();
        String expectedName = "";
        JsonArray expectedParagraphs = JsonValue.EMPTY_JSON_ARRAY;
        JsonObject expectedParams = JsonValue.EMPTY_JSON_OBJECT;
        JsonObject expectedForms = JsonValue.EMPTY_JSON_OBJECT;
        JsonObject expectedAngularObjects = JsonValue.EMPTY_JSON_OBJECT;
        JsonObject expectedConfig = JsonValue.EMPTY_JSON_OBJECT;
        JsonObject expectedInfo = JsonValue.EMPTY_JSON_OBJECT;

        NoteMessage noteMessage = new NoteMessage(expectedPrincipal,expectedTicket,expectedRoles,note);
        JsonObject noteJson = noteMessage.asJsonObject();

        Assertions.assertEquals(expectedOp,noteJson.getString("op"));
        Assertions.assertEquals(expectedPrincipal,noteJson.getString("principal"));
        Assertions.assertEquals(expectedTicket,noteJson.getString("ticket"));
        Assertions.assertEquals(expectedRoles,noteJson.getString("roles"));

        Assertions.assertEquals(expectedNoteId,noteJson.getJsonObject("data").getJsonObject("note").getString("id"));
        Assertions.assertEquals(expectedName,noteJson.getJsonObject("data").getJsonObject("note").getString("name"));
        Assertions.assertEquals(expectedParagraphs,noteJson.getJsonObject("data").getJsonObject("note").getJsonArray("paragraphs"));
        Assertions.assertEquals(expectedParams,noteJson.getJsonObject("data").getJsonObject("note").getJsonObject("noteParams"));
        Assertions.assertEquals(expectedForms,noteJson.getJsonObject("data").getJsonObject("note").getJsonObject("noteForms"));
        Assertions.assertEquals(expectedAngularObjects,noteJson.getJsonObject("data").getJsonObject("note").getJsonObject("angularObjects"));
        Assertions.assertEquals(expectedConfig,noteJson.getJsonObject("data").getJsonObject("note").getJsonObject("config"));
        Assertions.assertEquals(expectedInfo,noteJson.getJsonObject("data").getJsonObject("note").getJsonObject("info"));
    }
}