package com.teragrep.zep_01.message;

import com.teragrep.zep_01.notebook.Note;
import com.teragrep.zep_01.notebook.Paragraph;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class ParagraphMessageTest {

    @Test
    public void testSerialization(){
        String expectedOp = "PARAGRAPH";
        String expectedPrincipal = "anonymous";
        String expectedTicket = "anonymous";
        String expectedRoles = "";
        Note note = new Note();
        Paragraph paragraph = new Paragraph(note,null);

        String expectedId = paragraph.getId();
        String expectedParagraphTitle = "testTitle";
        paragraph.setTitle(expectedParagraphTitle);
        int expectedProgress = paragraph.progress();
        JsonObject expectedConfig = JsonValue.EMPTY_JSON_OBJECT;
        JsonObject expectedParams = JsonValue.EMPTY_JSON_OBJECT;
        JsonObject expectedForms = JsonValue.EMPTY_JSON_OBJECT;
        JsonObject expectedRuntimeInfos = JsonValue.EMPTY_JSON_OBJECT;
        int expectedProgressIntervalMs = 500;
        String expectedJobName = paragraph.getJobName();
        String expectedStatus = paragraph.getStatus().name();

        ParagraphMessage paragraphMessage = new ParagraphMessage(expectedPrincipal,expectedTicket,expectedRoles,paragraph);
        JsonObject paragraphJson = paragraphMessage.asJsonObject();

        Assertions.assertEquals(expectedOp,paragraphJson.getString("op"));
        Assertions.assertEquals(expectedPrincipal,paragraphJson.getString("principal"));
        Assertions.assertEquals(expectedTicket,paragraphJson.getString("ticket"));
        Assertions.assertEquals(expectedRoles,paragraphJson.getString("roles"));

        Assertions.assertEquals(expectedId,paragraphJson.getJsonObject("data").getJsonObject("paragraph").getString("id"));
        Assertions.assertEquals(expectedParagraphTitle,paragraphJson.getJsonObject("data").getJsonObject("paragraph").getString("title"));
        Assertions.assertEquals(expectedProgress,paragraphJson.getJsonObject("data").getJsonObject("paragraph").getInt("progress"));
        Assertions.assertEquals(expectedConfig,paragraphJson.getJsonObject("data").getJsonObject("paragraph").getJsonObject("config"));
        Assertions.assertEquals(expectedParams,paragraphJson.getJsonObject("data").getJsonObject("paragraph").getJsonObject("settings").getJsonObject("params"));
        Assertions.assertEquals(expectedForms,paragraphJson.getJsonObject("data").getJsonObject("paragraph").getJsonObject("settings").getJsonObject("forms"));
        Assertions.assertEquals(expectedRuntimeInfos,paragraphJson.getJsonObject("data").getJsonObject("paragraph").getJsonObject("runtimeInfos"));
        Assertions.assertEquals(expectedProgressIntervalMs,paragraphJson.getJsonObject("data").getJsonObject("paragraph").getInt("progressUpdateIntervalMs"));
        Assertions.assertEquals(expectedJobName,paragraphJson.getJsonObject("data").getJsonObject("paragraph").getString("jobName"));
        Assertions.assertEquals(expectedStatus,paragraphJson.getJsonObject("data").getJsonObject("paragraph").getString("status"));
    }
}