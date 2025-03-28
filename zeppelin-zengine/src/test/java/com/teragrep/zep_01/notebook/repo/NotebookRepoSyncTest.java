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

package com.teragrep.zep_01.notebook.repo;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import com.teragrep.zep_01.conf.ZeppelinConfiguration;
import com.teragrep.zep_01.conf.ZeppelinConfiguration.ConfVars;
import com.teragrep.zep_01.display.AngularObjectRegistryListener;
import com.teragrep.zep_01.interpreter.InterpreterFactory;
import com.teragrep.zep_01.interpreter.InterpreterSettingManager;
import com.teragrep.zep_01.interpreter.remote.RemoteInterpreterProcessListener;
import com.teragrep.zep_01.notebook.AuthorizationService;
import com.teragrep.zep_01.notebook.Note;
import com.teragrep.zep_01.notebook.NoteInfo;
import com.teragrep.zep_01.notebook.NoteManager;
import com.teragrep.zep_01.notebook.Notebook;
import com.teragrep.zep_01.notebook.Paragraph;
import com.teragrep.zep_01.search.SearchService;
import com.teragrep.zep_01.storage.ConfigStorage;
import com.teragrep.zep_01.user.AuthenticationInfo;
import com.teragrep.zep_01.user.Credentials;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotebookRepoSyncTest {

  private File ZEPPELIN_HOME;
  private ZeppelinConfiguration conf;
  private File mainNotebookDir;
  private File secNotebookDir;
  private Notebook notebook;
  private NotebookRepoSync notebookRepoSync;
  private InterpreterFactory factory;
  private InterpreterSettingManager interpreterSettingManager;
  private SearchService search;
  private Credentials credentials;
  private AuthenticationInfo anonymous;
  private NoteManager noteManager;
  private AuthorizationService authorizationService;
  private static final Logger LOG = LoggerFactory.getLogger(NotebookRepoSyncTest.class);

  @Before
  public void setUp() throws Exception {
    System.setProperty("zeppelin.isTest", "true");
    ZEPPELIN_HOME = new File("target/home-" + Instant.now().toEpochMilli()).getAbsoluteFile();
    new File(ZEPPELIN_HOME, "conf").mkdirs();
    String mainNotePath = ZEPPELIN_HOME.getAbsolutePath() + "/notebook";
    String secNotePath = ZEPPELIN_HOME.getAbsolutePath() + "/notebook_secondary";
    mainNotebookDir = new File(mainNotePath);
    secNotebookDir = new File(secNotePath);
    mainNotebookDir.mkdirs();
    secNotebookDir.mkdirs();

    System.setProperty(ConfVars.ZEPPELIN_HOME.getVarName(), ZEPPELIN_HOME.getAbsolutePath());
    System.setProperty(ConfVars.ZEPPELIN_NOTEBOOK_DIR.getVarName(), mainNotebookDir.getAbsolutePath());
    System.setProperty(ConfVars.ZEPPELIN_NOTEBOOK_STORAGE.getVarName(), "com.teragrep.zep_01.notebook.repo.VFSNotebookRepo,com.teragrep.zep_01.notebook.repo.mock.VFSNotebookRepoMock");
    System.setProperty(ConfVars.ZEPPELIN_NOTEBOOK_ONE_WAY_SYNC.getVarName(), "false");
    System.setProperty(ConfVars.ZEPPELIN_CONFIG_FS_DIR.getVarName(), ZEPPELIN_HOME.getAbsolutePath() + "/conf");
    System.setProperty(ConfVars.ZEPPELIN_PLUGINS_DIR.getVarName(), new File("../../../plugins").getAbsolutePath());

    LOG.debug("main Note dir : " + mainNotePath);
    LOG.debug("secondary note dir : " + secNotePath);
    conf = ZeppelinConfiguration.create();

    ConfigStorage.reset();

    interpreterSettingManager = new InterpreterSettingManager(conf,
        mock(AngularObjectRegistryListener.class), mock(RemoteInterpreterProcessListener.class));
    factory = new InterpreterFactory(interpreterSettingManager);

    search = mock(SearchService.class);
    notebookRepoSync = new NotebookRepoSync(conf);
    noteManager = new NoteManager(notebookRepoSync);
    authorizationService = new AuthorizationService(noteManager, conf);
    credentials = new Credentials(conf);
    notebook = new Notebook(conf, authorizationService, notebookRepoSync, noteManager, factory, interpreterSettingManager, search, credentials, null);
    anonymous = new AuthenticationInfo("anonymous");
  }

  @After
  public void tearDown() throws Exception {
    System.clearProperty("zeppelin.isTest");
  }

  @Test
  public void testRepoCount() throws IOException {
    assertTrue(notebookRepoSync.getMaxRepoNum() >= notebookRepoSync.getRepoCount());
  }

  @Test
  public void testSyncOnCreate() throws IOException {
    /* check that both storage systems are empty */
    assertTrue(notebookRepoSync.getRepoCount() > 1);
    assertEquals(0, notebookRepoSync.list(0, anonymous).size());
    assertEquals(0, notebookRepoSync.list(1, anonymous).size());

    /* create note */
    Note note = notebook.createNote("test", "", anonymous);

    // check that automatically saved on both storages
    assertEquals(1, notebookRepoSync.list(0, anonymous).size());
    assertEquals(1, notebookRepoSync.list(1, anonymous).size());
    assertEquals(notebookRepoSync.list(0, anonymous).get(0).getId(), notebookRepoSync.list(1, anonymous).get(0).getId());

    NoteInfo noteInfo = notebookRepoSync.list(0, null).get(0);
  }

  @Test
  public void testSyncOnDelete() throws IOException {
    /* create note */
    assertTrue(notebookRepoSync.getRepoCount() > 1);
    assertEquals(0, notebookRepoSync.list(0, anonymous).size());
    assertEquals(0, notebookRepoSync.list(1, anonymous).size());

    Note note = notebook.createNote("test", "", anonymous);

    /* check that created in both storage systems */
    assertEquals(1, notebookRepoSync.list(0, anonymous).size());
    assertEquals(1, notebookRepoSync.list(1, anonymous).size());
    assertEquals(notebookRepoSync.list(0, anonymous).get(0).getId(), notebookRepoSync.list(1, anonymous).get(0).getId());

    /* remove Note */
    NoteInfo noteInfo = notebookRepoSync.list(0, null).get(0);
    notebook.removeNote(notebookRepoSync.get(noteInfo.getId(), noteInfo.getPath(), anonymous), anonymous);

    /* check that deleted in both storages */
    assertEquals(0, notebookRepoSync.list(0, anonymous).size());
    assertEquals(0, notebookRepoSync.list(1, anonymous).size());

  }

  @Test
  public void testSyncUpdateMain() throws IOException {

    /* create note */
    Note note = notebook.createNote("/test", "test", anonymous);
    note.setInterpreterFactory(mock(InterpreterFactory.class));
    Paragraph p1 = note.addNewParagraph(AuthenticationInfo.ANONYMOUS);
    Map config = p1.getConfig();
    config.put("enabled", true);
    p1.setConfig(config);
    p1.setText("hello world");

    /* new paragraph exists in note instance */
    assertEquals(1, note.getParagraphs().size());

    /* new paragraph not yet saved into storages */
    assertEquals(0, notebookRepoSync.get(0,
        notebookRepoSync.list(0, anonymous).get(0).getId(),
        notebookRepoSync.list(0, anonymous).get(0).getPath(), anonymous).getParagraphs().size());
    assertEquals(0, notebookRepoSync.get(1,
        notebookRepoSync.list(1, anonymous).get(0).getId(),
        notebookRepoSync.list(1, anonymous).get(0).getPath(), anonymous).getParagraphs().size());

    /* save to storage under index 0 (first storage) */
    notebookRepoSync.save(0, note, anonymous);

    /* check paragraph saved to first storage */
    assertEquals(1, notebookRepoSync.get(0,
        notebookRepoSync.list(0, anonymous).get(0).getId(),
        notebookRepoSync.list(0, anonymous).get(0).getPath(),
        anonymous).getParagraphs().size());
    /* check paragraph isn't saved to second storage */
    assertEquals(0, notebookRepoSync.get(1,
        notebookRepoSync.list(1, anonymous).get(0).getId(),
        notebookRepoSync.list(0, anonymous).get(0).getPath(),
        anonymous).getParagraphs().size());
    /* apply sync */
    notebookRepoSync.sync(null);
    /* check whether added to second storage */
    assertEquals(1, notebookRepoSync.get(1,
        notebookRepoSync.list(1, anonymous).get(0).getId(),
        notebookRepoSync.list(1, anonymous).get(0).getPath(), anonymous).getParagraphs().size());
    /* check whether same paragraph id */
    assertEquals(p1.getId(), notebookRepoSync.get(0,
        notebookRepoSync.list(0, anonymous).get(0).getId(),
        notebookRepoSync.list(0, anonymous).get(0).getPath(), anonymous).getLastParagraph().getId());
    assertEquals(p1.getId(), notebookRepoSync.get(1,
        notebookRepoSync.list(1, anonymous).get(0).getId(),
        notebookRepoSync.list(1, anonymous).get(0).getPath(), anonymous).getLastParagraph().getId());
  }

  @Test
  public void testSyncOnReloadedList() throws Exception {
    /* check that both storage repos are empty */
    assertTrue(notebookRepoSync.getRepoCount() > 1);
    assertEquals(0, notebookRepoSync.list(0, anonymous).size());
    assertEquals(0, notebookRepoSync.list(1, anonymous).size());

    File srcDir = new File("src/test/resources/notebook");
    File destDir = secNotebookDir;

    /* copy manually new notebook into secondary storage repo and check repos */
    try {
      FileUtils.copyDirectory(srcDir, destDir);
    } catch (IOException e) {
      fail("Failure: " + e.getMessage());
    }

    assertEquals(0, notebookRepoSync.list(0, anonymous).size());
    assertEquals(2, notebookRepoSync.list(1, anonymous).size());

    // After reloading notebooks repos should be synchronized
    notebook.reloadAllNotes(anonymous);
    assertEquals(2, notebookRepoSync.list(0, anonymous).size());
    assertEquals(2, notebookRepoSync.list(1, anonymous).size());
  }

  @Test
  public void testOneWaySyncOnReloadedList() throws IOException, SchedulerException {
    System.setProperty(ConfVars.ZEPPELIN_NOTEBOOK_DIR.getVarName(), mainNotebookDir.getAbsolutePath());
    System.setProperty(ConfVars.ZEPPELIN_NOTEBOOK_ONE_WAY_SYNC.getVarName(), "true");
    conf = ZeppelinConfiguration.create();
    notebookRepoSync = new NotebookRepoSync(conf);
    notebook = new Notebook(conf, mock(AuthorizationService.class), notebookRepoSync, new NoteManager(notebookRepoSync), factory, interpreterSettingManager, search, credentials, null);

    // check that both storage repos are empty
    assertTrue(notebookRepoSync.getRepoCount() > 1);
    assertEquals(0, notebookRepoSync.list(0, null).size());
    assertEquals(0, notebookRepoSync.list(1, null).size());

    File srcDir = new File("src/test/resources/notebook");
    File destDir = secNotebookDir;

    /* copy manually new notebook into secondary storage repo and check repos */
    try {
      FileUtils.copyDirectory(srcDir, destDir);
    } catch (IOException e) {
      fail("Failure: " + e.getMessage());
    }
    assertEquals(0, notebookRepoSync.list(0, null).size());
    assertEquals(2, notebookRepoSync.list(1, null).size());

    // after reloading the notebook should be wiped from secondary storage
    notebook.reloadAllNotes(null);
    assertEquals(0, notebookRepoSync.list(0, null).size());
    assertEquals(0, notebookRepoSync.list(1, null).size());

    destDir = mainNotebookDir;

    // copy manually new notebook into primary storage repo and check repos
    try {
      FileUtils.copyDirectory(srcDir, destDir);
    } catch (IOException e) {
      fail("Failure: " + e.getMessage());
    }
    assertEquals(2, notebookRepoSync.list(0, null).size());
    assertEquals(0, notebookRepoSync.list(1, null).size());

    // after reloading notebooks repos should be synchronized
    notebook.reloadAllNotes(null);
    assertEquals(2, notebookRepoSync.list(0, null).size());
    assertEquals(2, notebookRepoSync.list(1, null).size());
  }

  @Test
  public void testCheckpointOneStorage() throws IOException, SchedulerException {
    System.setProperty(ConfVars.ZEPPELIN_NOTEBOOK_STORAGE.getVarName(), "com.teragrep.zep_01.notebook.repo.GitNotebookRepo");
    ZeppelinConfiguration vConf = ZeppelinConfiguration.create();

    NotebookRepoSync vRepoSync = new NotebookRepoSync(vConf);
    Notebook vNotebookSync = new Notebook(vConf, mock(AuthorizationService.class), vRepoSync, new NoteManager(vRepoSync), factory, interpreterSettingManager, search, credentials, null);

    // one git versioned storage initialized
    assertThat(vRepoSync.getRepoCount()).isEqualTo(1);
    assertThat(vRepoSync.getRepo(0)).isInstanceOf(GitNotebookRepo.class);

    GitNotebookRepo gitRepo = (GitNotebookRepo) vRepoSync.getRepo(0);

    // no notes
    assertThat(vRepoSync.list(anonymous).size()).isEqualTo(0);
    // create note
    Note note = vNotebookSync.createNote("/test", "test", anonymous);
    assertThat(vRepoSync.list(anonymous).size()).isEqualTo(1);

    NoteInfo noteInfo = vRepoSync.list(anonymous).values().iterator().next();
    String noteId = noteInfo.getId();
    String notePath = noteInfo.getPath();
    // first checkpoint
    vRepoSync.checkpoint(noteId, notePath, "checkpoint message", anonymous);
    int vCount = gitRepo.revisionHistory(noteId, notePath, anonymous).size();
    assertThat(vCount).isEqualTo(1);

    note.setInterpreterFactory(mock(InterpreterFactory.class));
    Paragraph p = note.addNewParagraph(AuthenticationInfo.ANONYMOUS);
    Map<String, Object> config = p.getConfig();
    config.put("enabled", true);
    p.setConfig(config);
    p.setText("%md checkpoint test");

    // save and checkpoint again
    vRepoSync.save(note, anonymous);
    vRepoSync.checkpoint(noteId, notePath, "checkpoint message 2", anonymous);
    assertThat(gitRepo.revisionHistory(noteId, notePath, anonymous).size()).isEqualTo(vCount + 1);
  }

  @Test
  public void testSyncWithAcl() throws IOException {
    /* scenario 1 - note exists with acl on main storage */
    AuthenticationInfo user1 = new AuthenticationInfo("user1");
    Note note = notebook.createNote("/test", "test", user1);
    assertEquals(0, note.getParagraphs().size());

    // saved on both storages
    assertEquals(1, notebookRepoSync.list(0, null).size());
    assertEquals(1, notebookRepoSync.list(1, null).size());

    /* check that user1 is the only owner */
    Set<String> entity = new HashSet<String>();
    entity.add(user1.getUser());
    assertTrue(authorizationService.isOwner(note.getId(), entity));
    assertEquals(1, authorizationService.getOwners(note.getId()).size());
    assertEquals(0, authorizationService.getReaders(note.getId()).size());
    assertEquals(0, authorizationService.getRunners(note.getId()).size());
    assertEquals(0, authorizationService.getWriters(note.getId()).size());

    /* update note and save on secondary storage */
    note.setInterpreterFactory(mock(InterpreterFactory.class));
    Paragraph p1 = note.addNewParagraph(AuthenticationInfo.ANONYMOUS);
    p1.setText("hello world");
    assertEquals(1, note.getParagraphs().size());
    notebookRepoSync.save(1, note, null);

    /* check paragraph isn't saved into first storage */
    assertEquals(0, notebookRepoSync.get(0,
        notebookRepoSync.list(0, null).get(0).getId(),
        notebookRepoSync.list(0, null).get(0).getPath(), null).getParagraphs().size());
    /* check paragraph is saved into second storage */
    assertEquals(1, notebookRepoSync.get(1,
        notebookRepoSync.list(1, null).get(0).getId(),
        notebookRepoSync.list(1, null).get(0).getPath(), null).getParagraphs().size());

    /* now sync by user1 */
    notebookRepoSync.sync(user1);

    /* check that note updated and acl are same on main storage*/
    assertEquals(1, notebookRepoSync.get(0,
        notebookRepoSync.list(0, null).get(0).getId(),
        notebookRepoSync.list(0, null).get(0).getPath(), null).getParagraphs().size());
    assertTrue(authorizationService.isOwner(note.getId(), entity));
    assertEquals(1, authorizationService.getOwners(note.getId()).size());
    assertEquals(0, authorizationService.getReaders(note.getId()).size());
    assertEquals(0, authorizationService.getRunners(note.getId()).size());
    assertEquals(0, authorizationService.getWriters(note.getId()).size());

    /* scenario 2 - note doesn't exist on main storage */
    /* remove from main storage */
    notebookRepoSync.remove(0, note.getId(), note.getPath(), user1);
    assertEquals(0, notebookRepoSync.list(0, null).size());
    assertEquals(1, notebookRepoSync.list(1, null).size());

    /* now sync - should bring note from secondary storage with added acl */
    notebookRepoSync.sync(user1);
    assertEquals(1, notebookRepoSync.list(0, null).size());
    assertEquals(1, notebookRepoSync.list(1, null).size());
    assertEquals(1, authorizationService.getOwners(note.getId()).size());
    assertEquals(0, authorizationService.getReaders(note.getId()).size());
    assertEquals(0, authorizationService.getRunners(note.getId()).size());
    assertEquals(0, authorizationService.getWriters(note.getId()).size());
  }
}
