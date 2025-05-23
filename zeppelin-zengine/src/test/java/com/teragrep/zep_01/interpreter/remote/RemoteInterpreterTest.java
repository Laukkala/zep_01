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

package com.teragrep.zep_01.interpreter.remote;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.thrift.transport.TTransportException;
import com.teragrep.zep_01.conf.ZeppelinConfiguration;
import com.teragrep.zep_01.display.AngularObjectRegistry;
import com.teragrep.zep_01.display.GUI;
import com.teragrep.zep_01.display.Input;
import com.teragrep.zep_01.display.ui.OptionInput;
import com.teragrep.zep_01.interpreter.AbstractInterpreterTest;
import com.teragrep.zep_01.interpreter.Interpreter;
import com.teragrep.zep_01.interpreter.InterpreterContext;
import com.teragrep.zep_01.interpreter.InterpreterException;
import com.teragrep.zep_01.interpreter.InterpreterOption;
import com.teragrep.zep_01.interpreter.InterpreterResult;
import com.teragrep.zep_01.interpreter.InterpreterResult.Code;
import com.teragrep.zep_01.interpreter.InterpreterSetting;
import com.teragrep.zep_01.notebook.Note;
import com.teragrep.zep_01.notebook.NoteInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class RemoteInterpreterTest extends AbstractInterpreterTest {

  private InterpreterSetting interpreterSetting;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    interpreterSetting = interpreterSettingManager.getInterpreterSettingByName("test");
    Note note1 = new Note(new NoteInfo("note1", "/note_1"));
    when(mockNotebook.getNote("note1")).thenReturn(note1);
  }

  @After
  public void cleanUp() {
    System.clearProperty(ZeppelinConfiguration.ConfVars.ZEPPELIN_INTERPRETER_REMOTE_RUNNER.getVarName());
    System.clearProperty(ZeppelinConfiguration.ConfVars.ZEPPELIN_INTERPRETER_CONNECT_TIMEOUT.getVarName());
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Test
  public void testSharedMode() throws InterpreterException, IOException {
    interpreterSetting.getOption().setPerUser(InterpreterOption.SHARED);

    Interpreter interpreter1 = interpreterSetting.getDefaultInterpreter("user1", "note1");
    Interpreter interpreter2 = interpreterSetting.getDefaultInterpreter("user2", "note1");
    assertTrue(interpreter1 instanceof RemoteInterpreter);
    RemoteInterpreter remoteInterpreter1 = (RemoteInterpreter) interpreter1;
    assertTrue(interpreter2 instanceof RemoteInterpreter);
    RemoteInterpreter remoteInterpreter2 = (RemoteInterpreter) interpreter2;

    assertEquals(remoteInterpreter1.getScheduler(), remoteInterpreter2.getScheduler());

    InterpreterContext context1 = createDummyInterpreterContext();
    assertEquals("hello", remoteInterpreter1.interpret("hello", context1).message().get(0).getData());
    assertEquals(Interpreter.FormType.NATIVE, interpreter1.getFormType());
    assertEquals(0, remoteInterpreter1.getProgress(context1));
    assertNotNull(remoteInterpreter1.getOrCreateInterpreterProcess());
    assertTrue(remoteInterpreter1.getInterpreterGroup().getRemoteInterpreterProcess().isRunning());

    assertEquals("hello", remoteInterpreter2.interpret("hello", context1).message().get(0).getData());
    assertEquals(remoteInterpreter1.getInterpreterGroup().getRemoteInterpreterProcess(),
        remoteInterpreter2.getInterpreterGroup().getRemoteInterpreterProcess());

    // Call InterpreterGroup.close instead of Interpreter.close, otherwise we will have the
    // RemoteInterpreterProcess leakage.
    remoteInterpreter1.getInterpreterGroup().close(remoteInterpreter1.getSessionId());
    assertNull(remoteInterpreter1.getInterpreterGroup().getRemoteInterpreterProcess());

    InterpreterResult result = remoteInterpreter1.interpret("hello", context1);
    assertEquals(Code.ERROR, result.code());
    assertEquals("Interpreter process is not running\n", result.message().get(0).getData());
  }

  @Test
  public void testScopedMode() throws InterpreterException, IOException {
    interpreterSetting.getOption().setPerUser(InterpreterOption.SCOPED);

    Interpreter interpreter1 = interpreterSetting.getDefaultInterpreter("user1", "note1");
    Interpreter interpreter2 = interpreterSetting.getDefaultInterpreter("user2", "note1");
    assertTrue(interpreter1 instanceof RemoteInterpreter);
    RemoteInterpreter remoteInterpreter1 = (RemoteInterpreter) interpreter1;
    assertTrue(interpreter2 instanceof RemoteInterpreter);
    RemoteInterpreter remoteInterpreter2 = (RemoteInterpreter) interpreter2;

    assertNotEquals(interpreter1.getScheduler(), interpreter2.getScheduler());

    InterpreterContext context1 = createDummyInterpreterContext();
    assertEquals("hello", remoteInterpreter1.interpret("hello", context1).message().get(0).getData());
    assertEquals("hello", remoteInterpreter2.interpret("hello", context1).message().get(0).getData());
    assertEquals(Interpreter.FormType.NATIVE, interpreter1.getFormType());
    assertEquals(0, remoteInterpreter1.getProgress(context1));

    assertNotNull(remoteInterpreter1.getOrCreateInterpreterProcess());
    assertTrue(remoteInterpreter1.getInterpreterGroup().getRemoteInterpreterProcess().isRunning());

    assertEquals(remoteInterpreter1.getInterpreterGroup().getRemoteInterpreterProcess(),
        remoteInterpreter2.getInterpreterGroup().getRemoteInterpreterProcess());
    // Call InterpreterGroup.close instead of Interpreter.close, otherwise we will have the
    // RemoteInterpreterProcess leakage.
    remoteInterpreter1.getInterpreterGroup().close(remoteInterpreter1.getSessionId());
    try {
      assertEquals("hello", remoteInterpreter1.interpret("hello", context1).message().get(0).getData());
      fail("Should not be able to call interpret after interpreter is closed");
    } catch (Exception ignored) {
    }

    assertTrue(remoteInterpreter2.getInterpreterGroup().getRemoteInterpreterProcess().isRunning());
    assertEquals("hello", remoteInterpreter2.interpret("hello", context1).message().get(0).getData());
    remoteInterpreter2.getInterpreterGroup().close(remoteInterpreter2.getSessionId());

    InterpreterResult result = remoteInterpreter2.interpret("hello", context1);
    assertEquals(Code.ERROR, result.code());
    assertEquals("Interpreter process is not running\n", result.message().get(0).getData());
    assertNull(remoteInterpreter2.getInterpreterGroup().getRemoteInterpreterProcess());
  }

  @Test
  public void testIsolatedMode() throws InterpreterException, IOException {
    interpreterSetting.getOption().setPerUser(InterpreterOption.ISOLATED);

    Interpreter interpreter1 = interpreterSetting.getDefaultInterpreter("user1", "note1");
    Interpreter interpreter2 = interpreterSetting.getDefaultInterpreter("user2", "note1");
    assertTrue(interpreter1 instanceof RemoteInterpreter);
    RemoteInterpreter remoteInterpreter1 = (RemoteInterpreter) interpreter1;
    assertTrue(interpreter2 instanceof RemoteInterpreter);
    RemoteInterpreter remoteInterpreter2 = (RemoteInterpreter) interpreter2;

    assertNotEquals(interpreter1.getScheduler(), interpreter2.getScheduler());

    InterpreterContext context1 = createDummyInterpreterContext();
    assertEquals("hello", remoteInterpreter1.interpret("hello", context1).message().get(0).getData());
    assertEquals("hello", remoteInterpreter2.interpret("hello", context1).message().get(0).getData());
    assertEquals(Interpreter.FormType.NATIVE, interpreter1.getFormType());
    assertEquals(0, remoteInterpreter1.getProgress(context1));
    assertNotNull(remoteInterpreter1.getOrCreateInterpreterProcess());
    assertTrue(remoteInterpreter1.getInterpreterGroup().getRemoteInterpreterProcess().isRunning());

    assertNotEquals(remoteInterpreter1.getInterpreterGroup().getRemoteInterpreterProcess(),
        remoteInterpreter2.getInterpreterGroup().getRemoteInterpreterProcess());
    // Call InterpreterGroup.close instead of Interpreter.close, otherwise we will have the
    // RemoteInterpreterProcess leakage.
    remoteInterpreter1.getInterpreterGroup().close(remoteInterpreter1.getSessionId());
    assertNull(remoteInterpreter1.getInterpreterGroup().getRemoteInterpreterProcess());
    assertTrue(remoteInterpreter2.getInterpreterGroup().getRemoteInterpreterProcess().isRunning());

    InterpreterResult result = remoteInterpreter1.interpret("hello", context1);
    assertEquals(Code.ERROR, result.code());
    assertEquals("Interpreter process is not running\n", result.message().get(0).getData());

    assertEquals("hello", remoteInterpreter2.interpret("hello", context1).message().get(0).getData());
    remoteInterpreter2.getInterpreterGroup().close(remoteInterpreter2.getSessionId());

    result = remoteInterpreter2.interpret("hello", context1);
    assertEquals(Code.ERROR, result.code());
    assertEquals("Interpreter process is not running\n", result.message().get(0).getData());

    assertNull(remoteInterpreter2.getInterpreterGroup().getRemoteInterpreterProcess());

  }

  @Ignore(value="Seems to be using SleepInterpreter")
  @Test
  public void testExecuteIncorrectPrecode() throws TTransportException, IOException, InterpreterException {
    interpreterSetting.getOption().setPerUser(InterpreterOption.SHARED);
    interpreterSetting.setProperty("zeppelin.SleepInterpreter.precode", "fail test");
    Interpreter interpreter1 = interpreterSetting.getInterpreter("user1", "note1", "sleep");
    InterpreterContext context1 = createDummyInterpreterContext();;
    assertEquals(Code.ERROR, interpreter1.interpret("10", context1).code());
  }

  @Ignore(value="Seems to be using SleepInterpreter")
  @Test
  public void testExecuteCorrectPrecode() throws TTransportException, IOException, InterpreterException {
    interpreterSetting.getOption().setPerUser(InterpreterOption.SHARED);
    interpreterSetting.setProperty("zeppelin.SleepInterpreter.precode", "1");
    Interpreter interpreter1 = interpreterSetting.getInterpreter("user1", "note1", "sleep");
    InterpreterContext context1 = createDummyInterpreterContext();
    assertEquals(Code.SUCCESS, interpreter1.interpret("10", context1).code());
  }

  @Test
  public void testRemoteInterperterErrorStatus() throws TTransportException, IOException, InterpreterException {
    interpreterSetting.setProperty("zeppelin.interpreter.echo.fail", "true");
    interpreterSetting.getOption().setPerUser(InterpreterOption.SHARED);

    Interpreter interpreter1 = interpreterSetting.getDefaultInterpreter("user1", "note1");
    assertTrue(interpreter1 instanceof RemoteInterpreter);
    RemoteInterpreter remoteInterpreter1 = (RemoteInterpreter) interpreter1;

    InterpreterContext context1 = createDummyInterpreterContext();;
    assertEquals(Code.ERROR, remoteInterpreter1.interpret("hello", context1).code());
  }

  @Ignore(value="Seems depend on SleepInterpreter")
  @Test
  public void testFIFOScheduler() throws InterruptedException, InterpreterException {
    interpreterSetting.getOption().setPerUser(InterpreterOption.SHARED);
    // by default SleepInterpreter would use FIFOScheduler

    final Interpreter interpreter1 = interpreterSetting.getInterpreter("user1", "note1", "sleep");
    final InterpreterContext context1 = createDummyInterpreterContext();
    // run this dummy interpret method first to launch the RemoteInterpreterProcess to avoid the
    // time overhead of launching the process.
    interpreter1.interpret("1", context1);
    Thread thread1 = new Thread() {
      @Override
      public void run() {
        try {
          assertEquals(Code.SUCCESS, interpreter1.interpret("100", context1).code());
        } catch (InterpreterException e) {
          fail("Failure happened: " + e.getMessage());
        }
      }
    };
    Thread thread2 = new Thread() {
      @Override
      public void run() {
        try {
          assertEquals(Code.SUCCESS, interpreter1.interpret("100", context1).code());
        } catch (InterpreterException e) {
          fail("Failure happened: " + e.getMessage());
        }
      }
    };
    long start = System.currentTimeMillis();
    thread1.start();
    thread2.start();
    thread1.join();
    thread2.join();
    long end = System.currentTimeMillis();
    assertTrue((end - start) >= 200);
  }

  @Ignore(value="Seems to be using SleepInterpreter")
  @Test
  public void testParallelScheduler() throws InterruptedException, InterpreterException {
    interpreterSetting.getOption().setPerUser(InterpreterOption.SHARED);
    interpreterSetting.setProperty("zeppelin.SleepInterpreter.parallel", "true");

    final Interpreter interpreter1 = interpreterSetting.getInterpreter("user1", "note1", "sleep");
    final InterpreterContext context1 = createDummyInterpreterContext();

    // run this dummy interpret method first to launch the RemoteInterpreterProcess to avoid the
    // time overhead of launching the process.
    interpreter1.interpret("1", context1);
    Thread thread1 = new Thread() {
      @Override
      public void run() {
        try {
          assertEquals(Code.SUCCESS, interpreter1.interpret("100", context1).code());
        } catch (InterpreterException e) {
          fail("Failure happened: " + e.getMessage());
        }
      }
    };
    Thread thread2 = new Thread() {
      @Override
      public void run() {
        try {
          assertEquals(Code.SUCCESS, interpreter1.interpret("100", context1).code());
        } catch (InterpreterException e) {
          fail("Failure happened: " + e.getMessage());
        }
      }
    };
    long start = System.currentTimeMillis();
    thread1.start();
    thread2.start();
    thread1.join();
    thread2.join();
    long end = System.currentTimeMillis();
    assertTrue((end - start) <= 200);
  }

  @Ignore(value="Seems to depend on SleepInterpreter")
  @Test
  public void testRemoteInterpreterSharesTheSameSchedulerInstanceInTheSameGroup() {
    interpreterSetting.getOption().setPerUser(InterpreterOption.SHARED);
    Interpreter interpreter1 = interpreterSetting.getInterpreter("user1", "note1", "sleep");
    Interpreter interpreter2 = interpreterSetting.getInterpreter("user1", "note1", "echo");
    assertEquals(interpreter1.getInterpreterGroup(), interpreter2.getInterpreterGroup());
    assertEquals(interpreter1.getScheduler(), interpreter2.getScheduler());
  }

  @Ignore(value="Seems to depend on SleepInterpreter")
  @Test
  public void testMultiInterpreterSession() {
    interpreterSetting.getOption().setPerUser(InterpreterOption.SCOPED);
    Interpreter interpreter1_user1 = interpreterSetting.getInterpreter("user1", "note1", "sleep");
    Interpreter interpreter2_user1 = interpreterSetting.getInterpreter("user1", "note1", "echo");
    assertEquals(interpreter1_user1.getInterpreterGroup(), interpreter2_user1.getInterpreterGroup());
    assertEquals(interpreter1_user1.getScheduler(), interpreter2_user1.getScheduler());

    Interpreter interpreter1_user2 = interpreterSetting.getInterpreter("user2", "note1", "sleep");
    Interpreter interpreter2_user2 = interpreterSetting.getInterpreter("user2", "note1", "echo");
    assertEquals(interpreter1_user2.getInterpreterGroup(), interpreter2_user2.getInterpreterGroup());
    assertEquals(interpreter1_user2.getScheduler(), interpreter2_user2.getScheduler());

    // scheduler is shared in session but not across session
    assertNotEquals(interpreter1_user1.getScheduler(), interpreter1_user2.getScheduler());
  }

  @Test
  public void should_push_local_angular_repo_to_remote() throws Exception {

    final AngularObjectRegistry registry = new AngularObjectRegistry("spark", null);
    registry.add("name_1", "value_1", "note_1", "paragraphId_1");
    registry.add("name_2", "value_2", "node_2", "paragraphId_2");
    Interpreter interpreter = interpreterSetting.getInterpreter("user1", "note1", "angular_obj");
    interpreter.getInterpreterGroup().setAngularObjectRegistry(registry);

    final InterpreterContext context = createDummyInterpreterContext();
    InterpreterResult result = interpreter.interpret("dummy", context);
    assertEquals(Code.SUCCESS, result.code());
    assertEquals("2", result.message().get(0).getData());
  }

  @Test
  public void testEnvStringPattern() {
    assertFalse(RemoteInterpreterUtils.isEnvString(null));
    assertFalse(RemoteInterpreterUtils.isEnvString(""));
    assertFalse(RemoteInterpreterUtils.isEnvString("abcDEF"));
    assertFalse(RemoteInterpreterUtils.isEnvString("ABC-DEF"));
    assertTrue(RemoteInterpreterUtils.isEnvString("ABCDEF"));
    assertTrue(RemoteInterpreterUtils.isEnvString("ABC_DEF"));
    assertTrue(RemoteInterpreterUtils.isEnvString("ABC_DEF123"));
  }

  @Test
  public void testEnvironmentAndProperty() throws InterpreterException {
    interpreterSetting.getOption().setPerUser(InterpreterOption.SHARED);
    interpreterSetting.setProperty("ENV_1", "VALUE_1");
    interpreterSetting.setProperty("property_1", "value_1");

    final Interpreter interpreter1 = interpreterSetting.getInterpreter("user1", "note1", "get");
    final InterpreterContext context1 = createDummyInterpreterContext();

    assertEquals("VALUE_1", interpreter1.interpret("getEnv ENV_1", context1).message().get(0).getData());
    assertEquals("null", interpreter1.interpret("getEnv ENV_2", context1).message().get(0).getData());

    assertEquals("value_1", interpreter1.interpret("getProperty property_1", context1).message().get(0).getData());
    assertEquals("null", interpreter1.interpret("getProperty not_existed_property", context1).message().get(0).getData());
  }

  @Test
  public void testConvertDynamicForms() throws InterpreterException {
    GUI gui = new GUI();
    OptionInput.ParamOption[] paramOptions = {
        new OptionInput.ParamOption("value1", "param1"),
        new OptionInput.ParamOption("value2", "param2")
    };
    List<Object> defaultValues = new ArrayList<>();
    defaultValues.add("default1");
    defaultValues.add("default2");
    gui.checkbox("checkbox_id", paramOptions, defaultValues);
    gui.select("select_id", paramOptions, "default");
    gui.textbox("textbox_id");
    Map<String, Input> expected = new LinkedHashMap<>(gui.getForms());
    Interpreter interpreter = interpreterSetting.getDefaultInterpreter("user1", "note1");
    InterpreterContext context = createDummyInterpreterContext();

    interpreter.interpret("text", context);
    assertArrayEquals(expected.values().toArray(), gui.getForms().values().toArray());
  }

  @Ignore(value="Seems to depend on SleepInterpreter")
  @Test
  public void testFailToLaunchInterpreterProcess_InvalidRunner() {
    System.setProperty(ZeppelinConfiguration.ConfVars.ZEPPELIN_INTERPRETER_REMOTE_RUNNER.getVarName(), "invalid_runner");
    final Interpreter interpreter1 = interpreterSetting.getInterpreter("user1", "note1", "sleep");
    final InterpreterContext context1 = createDummyInterpreterContext();
    // run this dummy interpret method first to launch the RemoteInterpreterProcess to avoid the
    // time overhead of launching the process.
    try {
      interpreter1.interpret("1", context1);
      fail("Should not be able to launch interpreter process");
    } catch (InterpreterException e) {
      assertTrue(ExceptionUtils.getStackTrace(e).contains("java.io.IOException"));
    }
  }

  @Ignore(value="Seems to depend on SleepInterpreter")
  @Test
  public void testFailToLaunchInterpreterProcess_ErrorInRunner() {
    System.setProperty(ZeppelinConfiguration.ConfVars.ZEPPELIN_INTERPRETER_REMOTE_RUNNER.getVarName(),
             zeppelinHome.getAbsolutePath() + "/zeppelin-zengine/src/test/resources/bin/interpreter_invalid.sh");
    final Interpreter interpreter1 = interpreterSetting.getInterpreter("user1", "note1", "sleep");
    final InterpreterContext context1 = createDummyInterpreterContext();
    // run this dummy interpret method first to launch the RemoteInterpreterProcess to avoid the
    // time overhead of launching the process.
    try {
      interpreter1.interpret("1", context1);
      fail("Should not be able to launch interpreter process");
    } catch (InterpreterException e) {
      assertTrue(ExceptionUtils.getStackTrace(e).contains("invalid_command:"));
    }
  }

  @Ignore(value="Has interpreter timeout stuff")
  @Test
  public void testFailToLaunchInterpreterProcess_Timeout() {
    System.setProperty(ZeppelinConfiguration.ConfVars.ZEPPELIN_INTERPRETER_REMOTE_RUNNER.getVarName(),
            zeppelinHome.getAbsolutePath() + "/zeppelin-zengine/src/test/resources/bin/interpreter_timeout.sh");
    System.setProperty(ZeppelinConfiguration.ConfVars.ZEPPELIN_INTERPRETER_CONNECT_TIMEOUT.getVarName(), "10000");
    final Interpreter interpreter1 = interpreterSetting.getInterpreter("user1", "note1", "sleep");
    final InterpreterContext context1 = createDummyInterpreterContext();
    // run this dummy interpret method first to launch the RemoteInterpreterProcess to avoid the
    // time overhead of launching the process.
    try {
      interpreter1.interpret("1", context1);
      fail("Should not be able to launch interpreter process");
    } catch (InterpreterException e) {
      assertTrue(ExceptionUtils.getStackTrace(e).contains("Interpreter Process creation is time out"));
    }
  }
}
