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

package com.teragrep.zep_01.scheduler;

import com.teragrep.zep_01.interpreter.AbstractInterpreterTest;
import com.teragrep.zep_01.interpreter.InterpreterContext;
import com.teragrep.zep_01.interpreter.InterpreterException;
import com.teragrep.zep_01.interpreter.InterpreterResult;
import com.teragrep.zep_01.interpreter.InterpreterSetting;
import com.teragrep.zep_01.interpreter.remote.RemoteInterpreter;
import com.teragrep.zep_01.interpreter.remote.RemoteInterpreterProcessListener;
import com.teragrep.zep_01.interpreter.thrift.ParagraphInfo;
import com.teragrep.zep_01.notebook.Note;
import com.teragrep.zep_01.notebook.NoteInfo;
import com.teragrep.zep_01.resource.LocalResourcePool;
import com.teragrep.zep_01.scheduler.Job.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class RemoteSchedulerTest extends AbstractInterpreterTest
    implements RemoteInterpreterProcessListener {

  private InterpreterSetting interpreterSetting;
  private SchedulerFactory schedulerSvc;
  private static final int TICK_WAIT = 100;
  private static final int MAX_WAIT_CYCLES = 100;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    Note note1 = new Note(new NoteInfo("note1", "/note_1"));
    when(mockNotebook.getNote("note1")).thenReturn(note1);

    schedulerSvc = SchedulerFactory.singleton();
    interpreterSetting = interpreterSettingManager.getInterpreterSettingByName("test");
  }

  @Override
  @After
  public void tearDown() {
    interpreterSetting.close();
  }

  @Ignore(value="Contains sleep, timeout, while loops or something similar waiting/cycleburning")
  @Test
  public void test() throws Exception {
    final RemoteInterpreter intpA = (RemoteInterpreter) interpreterSetting.getInterpreter("user1", "note1", "mock");

    intpA.open();

    Scheduler scheduler = intpA.getScheduler();

    Job<Object> job = new Job<Object>("jobId", "jobName", null) {
      Object results;

      @Override
      public Object getReturn() {
        return results;
      }

      @Override
      public int progress() {
        return 0;
      }

      @Override
      public Map<String, Object> info() {
        return null;
      }

      @Override
      protected Object jobRun() throws Throwable {
        intpA.interpret("1000", InterpreterContext.builder()
                .setNoteId("noteId")
                .setParagraphId("jobId")
                .setResourcePool(new LocalResourcePool("pool1"))
                .build());
        return "1000";
      }

      @Override
      protected boolean jobAbort() {
        return false;
      }

      @Override
      public void setResult(Object results) {
        this.results = results;
      }
    };
    scheduler.submit(job);

    int cycles = 0;
    while (!job.isRunning() && cycles < MAX_WAIT_CYCLES) {
      LOGGER.debug("Status:" + job.getStatus());
      Thread.sleep(TICK_WAIT);
      cycles++;
    }
    assertTrue(job.isRunning());

    Thread.sleep(5 * TICK_WAIT);

    cycles = 0;
    while (!job.isTerminated() && cycles < MAX_WAIT_CYCLES) {
      Thread.sleep(TICK_WAIT);
      cycles++;
    }

    assertTrue(job.isTerminated());

    intpA.close();
    schedulerSvc.removeScheduler("test");
  }

  @Ignore(value="Contains sleep, timeout, while loops or something similar waiting/cycleburning")
  @Test
  public void testAbortOnPending() throws Exception {
    final RemoteInterpreter intpA = (RemoteInterpreter) interpreterSetting.getInterpreter("user1", "note1", "mock");
    intpA.open();

    Scheduler scheduler = intpA.getScheduler();

    Job<Object> job1 = new Job<Object>("jobId1", "jobName1", null) {
      Object results;
      InterpreterContext context = InterpreterContext.builder()
          .setNoteId("noteId")
          .setParagraphId("jobId1")
          .setResourcePool(new LocalResourcePool("pool1"))
          .build();

      @Override
      public Object getReturn() {
        return results;
      }

      @Override
      public int progress() {
        return 0;
      }

      @Override
      public Map<String, Object> info() {
        return null;
      }

      @Override
      protected Object jobRun() throws Throwable {
        intpA.interpret("1000", context);
        return "1000";
      }

      @Override
      protected boolean jobAbort() {
        if (isRunning()) {
          try {
            intpA.cancel(context);
          } catch (InterpreterException e) {
            fail("Failure happened: " + e.getMessage());
          }
        }
        return true;
      }

      @Override
      public void setResult(Object results) {
        this.results = results;
      }
    };

    Job<Object> job2 = new Job<Object>("jobId2", "jobName2", null) {
      public Object results;
      InterpreterContext context = InterpreterContext.builder()
          .setNoteId("noteId")
          .setParagraphId("jobId2")
          .setResourcePool(new LocalResourcePool("pool1"))
          .build();

      @Override
      public Object getReturn() {
        return results;
      }

      @Override
      public int progress() {
        return 0;
      }

      @Override
      public Map<String, Object> info() {
        return null;
      }

      @Override
      protected Object jobRun() throws Throwable {
        intpA.interpret("1000", context);
        return "1000";
      }

      @Override
      protected boolean jobAbort() {
        if (isRunning()) {
          try {
            intpA.cancel(context);
          } catch (InterpreterException e) {
            fail("Failure happened: " + e.getMessage());
          }
        }
        return true;
      }

      @Override
      public void setResult(Object results) {
        this.results = results;
      }
    };

    job2.setResult("result2");

    scheduler.submit(job1);
    scheduler.submit(job2);


    int cycles = 0;
    while (!job1.isRunning() && cycles < MAX_WAIT_CYCLES) {
      Thread.sleep(TICK_WAIT);
      cycles++;
    }
    assertTrue(job1.isRunning());
    assertEquals(Status.PENDING, job2.getStatus());

    job2.abort();

    cycles = 0;
    while (!job1.isTerminated() && cycles < MAX_WAIT_CYCLES) {
      Thread.sleep(TICK_WAIT);
      cycles++;
    }

    assertNotNull(job1.getDateFinished());
    assertTrue(job1.isTerminated());
    assertNull(job2.getDateFinished());
    assertTrue(job2.isTerminated());
    assertEquals("result2", job2.getReturn());

    intpA.close();
    schedulerSvc.removeScheduler("test");
  }

  @Override
  public void onOutputAppend(String noteId, String paragraphId, int index, String output) {

  }

  @Override
  public void onOutputUpdated(String noteId, String paragraphId, int index, InterpreterResult.Type type, String output) {

  }

  @Override
  public void onOutputClear(String noteId, String paragraphId) {

  }

  @Override
  public void runParagraphs(String noteId, List<Integer> paragraphIndices, List<String> paragraphIds, String curParagraphId) throws IOException {

  }

  @Override
  public void onParaInfosReceived(String noteId, String paragraphId,
                                  String interpreterSettingId, Map<String, String> metaInfos) {
  }

  @Override
  public List<ParagraphInfo> getParagraphList(String user, String noteId) {
    return null;
  }

  @Override
  public void checkpointOutput(String noteId, String paragraphId) {

  }
}
