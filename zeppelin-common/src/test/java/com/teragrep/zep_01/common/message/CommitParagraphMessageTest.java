package com.teragrep.zep_01.common.message;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.Map;

public class CommitParagraphMessageTest {

    @Test
    public void testSerialization(){

        final String expectedOp = "COMMIT_PARAGRAPH";
        final String expectedNoteId = "test_note";
        final String expectedParagraphId = "test_paragraph";
        final String expectedTitle = "title";
        final Map<String,Object> expectedConfig = new HashMap<String,Object>();
        final Map<String,Object> expectedParams = new HashMap<String,Object>();

        JsonObject inputJson = Json.createObjectBuilder()
                .add("id",expectedParagraphId)
                .add("noteId",expectedNoteId)
                .add("title",expectedTitle)
                .add("config",JsonValue.EMPTY_JSON_OBJECT)
                .add("params",JsonValue.EMPTY_JSON_OBJECT)
                .build();

        final CommitParagraphMessage commitParagraphMessage = new CommitParagraphMessage(inputJson);
        Assertions.assertEquals(expectedOp,commitParagraphMessage.op());
        Assertions.assertEquals(expectedNoteId,commitParagraphMessage.noteId());
        Assertions.assertEquals(expectedParagraphId,commitParagraphMessage.paragraphId());
        Assertions.assertEquals(expectedTitle,commitParagraphMessage.title());
        Assertions.assertEquals(expectedConfig,commitParagraphMessage.config());
        Assertions.assertEquals(expectedParams,commitParagraphMessage.params());
    }
    @Test
    public void testContract(){
        EqualsVerifier.forClass(GetHomeNoteMessage.class).verify();
    }
}