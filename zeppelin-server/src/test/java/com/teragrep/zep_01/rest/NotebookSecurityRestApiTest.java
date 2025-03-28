/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.teragrep.zep_01.rest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import com.teragrep.zep_01.notebook.Note;
import com.teragrep.zep_01.notebook.Notebook;
import com.teragrep.zep_01.utils.TestUtils;
import org.hamcrest.Matcher;
import org.junit.*;

public class NotebookSecurityRestApiTest extends AbstractTestRestApi {
  Gson gson = new Gson();

  @BeforeClass
  public static void init() throws Exception {
    AbstractTestRestApi.startUpWithAuthenticationEnable(
            NotebookSecurityRestApiTest.class.getSimpleName());
  }

  @AfterClass
  public static void destroy() throws Exception {
    AbstractTestRestApi.shutDown();
  }

  @Test
  public void testThatUserCanCreateAndRemoveNote() throws IOException {
    String noteId = createNoteForUser("test_1", "admin", "password1");
    assertNotNull(noteId);
    String id = getNoteIdForUser(noteId, "admin", "password1");
    assertThat(id, is(noteId));
    deleteNoteForUser(noteId, "admin", "password1");
  }

  @Test
  public void testThatOtherUserCanAccessNoteIfPermissionNotSet() throws IOException {
    String noteId = createNoteForUser("test_2", "admin", "password1");

    userTryGetNote(noteId, "user1", "password2", isAllowed());

    deleteNoteForUser(noteId, "admin", "password1");
  }

  @Test
  public void testThatOtherUserCannotAccessNoteIfPermissionSet() throws IOException {
    String noteId = createNoteForUser("test_3", "admin", "password1");

    //set permission
    String payload = "{ \"owners\": [\"admin\"], \"readers\": [\"user2\"], " +
            "\"runners\": [\"user2\"], \"writers\": [\"user2\"] }";
    CloseableHttpResponse put = httpPut("/notebook/" + noteId + "/permissions", payload , "admin", "password1");
    assertThat("test set note permission method:", put, isAllowed());
    put.close();

    userTryGetNote(noteId, "user1", "password2", isForbidden());

    userTryGetNote(noteId, "user2", "password3", isAllowed());

    deleteNoteForUser(noteId, "admin", "password1");
  }

  @Test
  public void testThatWriterCannotRemoveNote() throws IOException {
    String noteId = createNoteForUser("test_4", "admin", "password1");

    //set permission
    String payload = "{ \"owners\": [\"admin\", \"user1\"], \"readers\": [\"user2\"], " +
            "\"runners\": [\"user2\"], \"writers\": [\"user2\"] }";
    CloseableHttpResponse put = httpPut("/notebook/" + noteId + "/permissions", payload , "admin", "password1");
    assertThat("test set note permission method:", put, isAllowed());
    put.close();

    userTryRemoveNote(noteId, "user2", "password3", isForbidden());
    userTryRemoveNote(noteId, "user1", "password2", isAllowed());

    Note deletedNote = TestUtils.getInstance(Notebook.class).getNote(noteId);
    assertNull("Deleted note should be null", deletedNote);
  }

  private void userTryRemoveNote(String noteId, String user, String pwd,
          Matcher<? super CloseableHttpResponse> m) throws IOException {
    CloseableHttpResponse delete = httpDelete(("/notebook/" + noteId), user, pwd);
    assertThat(delete, m);
    delete.close();
  }

  private void userTryGetNote(String noteId, String user, String pwd,
          Matcher<? super CloseableHttpResponse> m) throws IOException {
    CloseableHttpResponse get = httpGet("/notebook/" + noteId, user, pwd);
    assertThat(get, m);
    get.close();
  }

  private String getNoteIdForUser(String noteId, String user, String pwd) throws IOException {
    CloseableHttpResponse get = httpGet("/notebook/" + noteId, user, pwd);
    assertThat("test note create method:", get, isAllowed());
    Map<String, Object> resp = gson.fromJson(EntityUtils.toString(get.getEntity(), StandardCharsets.UTF_8),
            new TypeToken<Map<String, Object>>() {}.getType());
    get.close();
    return (String) ((Map<String, Object>) resp.get("body")).get("id");
  }

  private String createNoteForUser(String noteName, String user, String pwd) throws IOException {
    String jsonRequest = "{\"name\":\"" + noteName + "\"}";
    CloseableHttpResponse post = httpPost("/notebook/", jsonRequest, user, pwd);
    assertThat("test note create method:", post, isAllowed());
    Map<String, Object> resp =
        gson.fromJson(
          EntityUtils.toString(post.getEntity(), StandardCharsets.UTF_8), new TypeToken<Map<String, Object>>() {}.getType());
    post.close();
    String newNoteId = (String) resp.get("body");
    Notebook notebook = TestUtils.getInstance(Notebook.class);
    Note newNote = notebook.getNote(newNoteId);
    assertNotNull("Can not find new note by id", newNote);
    return newNoteId;
  }

  private void deleteNoteForUser(String noteId, String user, String pwd) throws IOException {
    CloseableHttpResponse delete = httpDelete(("/notebook/" + noteId), user, pwd);
    assertThat("Test delete method:", delete, isAllowed());
    delete.close();
    // make sure note is deleted
    Note deletedNote = TestUtils.getInstance(Notebook.class).getNote(noteId);
    assertNull("Deleted note should be null", deletedNote);
  }

  private void createParagraphForUser(String noteId, String user, String pwd,
          String title, String text) throws IOException {
    String payload = "{\"title\": \"" + title + "\",\"text\": \"" + text + "\"}";
    CloseableHttpResponse post = httpPost(("/notebook/" + noteId + "/paragraph"), payload, user, pwd);
    post.close();
  }

  private void setPermissionForNote(String noteId, String user, String pwd) throws IOException {
    String payload = "{\"owners\":[\"" + user + "\"],\"readers\":[\"" + user +
            "\"],\"runners\":[\"" + user + "\"],\"writers\":[\"" + user + "\"]}";
    CloseableHttpResponse put = httpPut(("/notebook/" + noteId + "/permissions"), payload, user, pwd);
    put.close();
  }
}
