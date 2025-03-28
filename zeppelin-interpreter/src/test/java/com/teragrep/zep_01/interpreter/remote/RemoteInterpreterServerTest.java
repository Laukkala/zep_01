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

import org.apache.thrift.TException;
import com.teragrep.zep_01.interpreter.Interpreter;
import com.teragrep.zep_01.interpreter.InterpreterContext;
import com.teragrep.zep_01.interpreter.InterpreterException;
import com.teragrep.zep_01.interpreter.InterpreterResult;
import com.teragrep.zep_01.interpreter.LazyOpenInterpreter;
import com.teragrep.zep_01.interpreter.thrift.RemoteInterpreterContext;
import com.teragrep.zep_01.interpreter.thrift.RemoteInterpreterResult;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

@Ignore("Contains bunch of sleeps and timeouts")
public class RemoteInterpreterServerTest {

  @Test
  public void testStartStop() throws Exception {
    RemoteInterpreterServer server = new RemoteInterpreterServer("localhost",
        RemoteInterpreterUtils.findRandomAvailablePortOnAllLocalInterfaces(), ":", "groupId", true);

    startRemoteInterpreterServer(server, 10 * 1000);
    stopRemoteInterpreterServer(server, 10 * 10000);
  }

  @Test
  public void testStartStopWithQueuedEvents() throws Exception {
    RemoteInterpreterServer server = new RemoteInterpreterServer("localhost",
        RemoteInterpreterUtils.findRandomAvailablePortOnAllLocalInterfaces(), ":", "groupId", true);
    server.intpEventClient = mock(RemoteInterpreterEventClient.class);
    startRemoteInterpreterServer(server, 10 * 1000);

    stopRemoteInterpreterServer(server, 10 * 10000);
  }

  private void startRemoteInterpreterServer(RemoteInterpreterServer server, int timeout)
          throws InterruptedException, TException {
    assertFalse(server.isRunning());
    server.start();
    long startTime = System.currentTimeMillis();
    while (System.currentTimeMillis() - startTime < timeout) {
      if (server.isRunning()) {
        break;
      }
      Thread.sleep(200);
    }
    assertTrue(server.isRunning());
    assertTrue(RemoteInterpreterUtils.checkIfRemoteEndpointAccessible("localhost",
              server.getPort()));

    server.init(new HashMap<>());
    assertNotNull(server.getConf());
    assertNotNull(server.getLifecycleManager());
  }

  private void stopRemoteInterpreterServer(RemoteInterpreterServer server, int timeout)
      throws TException, InterruptedException {
    assertTrue(server.isRunning());
    server.shutdown();
    long startTime = System.currentTimeMillis();
    while (System.currentTimeMillis() - startTime < timeout) {
      if (!server.isRunning()) {
        break;
      }
      Thread.sleep(200);
    }
    assertFalse(server.isRunning());
    assertFalse(RemoteInterpreterUtils.checkIfRemoteEndpointAccessible("localhost",
              server.getPort()));
  }

  @Test
  public void testInterpreter() throws Exception {
    final RemoteInterpreterServer server = new RemoteInterpreterServer("localhost",
        RemoteInterpreterUtils.findRandomAvailablePortOnAllLocalInterfaces(), ":", "groupId", true);
    server.init(new HashMap<>());
    server.intpEventClient = mock(RemoteInterpreterEventClient.class);

    Map<String, String> intpProperties = new HashMap<>();
    intpProperties.put("property_1", "value_1");
    intpProperties.put("zeppelin.interpreter.localRepo", "/tmp");

    // create Test1Interpreter in session_1
    server.createInterpreter("group_1", "session_1", Test1Interpreter.class.getName(),
        intpProperties, "user_1");
    Test1Interpreter interpreter1 = (Test1Interpreter)
        ((LazyOpenInterpreter) server.getInterpreterGroup().get("session_1").get(0))
            .getInnerInterpreter();
    assertEquals(1, server.getInterpreterGroup().getSessionNum());
    assertEquals(1, server.getInterpreterGroup().get("session_1").size());
    assertEquals(2, interpreter1.getProperties().size());
    assertEquals("value_1", interpreter1.getProperty("property_1"));

    // create Test2Interpreter in session_1
    server.createInterpreter("group_1", "session_1", Test1Interpreter.class.getName(),
        intpProperties, "user_1");
    assertEquals(2, server.getInterpreterGroup().get("session_1").size());

    // create Test1Interpreter in session_2
    server.createInterpreter("group_1", "session_2", Test1Interpreter.class.getName(),
        intpProperties, "user_1");
    assertEquals(2, server.getInterpreterGroup().getSessionNum());
    assertEquals(2, server.getInterpreterGroup().get("session_1").size());
    assertEquals(1, server.getInterpreterGroup().get("session_2").size());

    final RemoteInterpreterContext intpContext = new RemoteInterpreterContext();
    intpContext.setNoteId("note_1");
    intpContext.setParagraphId("paragraph_1");
    intpContext.setGui("{}");
    intpContext.setNoteGui("{}");
    intpContext.setLocalProperties(new HashMap<>());

    // single output of SUCCESS
    RemoteInterpreterResult result = server.interpret("session_1", Test1Interpreter.class.getName(),
        "SINGLE_OUTPUT_SUCCESS", intpContext);
    assertEquals("SUCCESS", result.code);
    assertEquals(1, result.getMsg().size());
    assertEquals("SINGLE_OUTPUT_SUCCESS", result.getMsg().get(0).getData());

    // combo output of SUCCESS
    result = server.interpret("session_1", Test1Interpreter.class.getName(), "COMBO_OUTPUT_SUCCESS",
        intpContext);
    assertEquals("SUCCESS", result.code);
    assertEquals(2, result.getMsg().size());
    assertEquals("INTERPRETER_OUT", result.getMsg().get(0).getData());
    assertEquals("SINGLE_OUTPUT_SUCCESS", result.getMsg().get(1).getData());

    // single output of ERROR
    result = server.interpret("session_1", Test1Interpreter.class.getName(), "SINGLE_OUTPUT_ERROR",
        intpContext);
    assertEquals("ERROR", result.code);
    assertEquals(1, result.getMsg().size());
    assertEquals("SINGLE_OUTPUT_ERROR", result.getMsg().get(0).getData());

    // getFormType
    String formType = server.getFormType("session_1", Test1Interpreter.class.getName());
    assertEquals("NATIVE", formType);

    // cancel
    Thread sleepThread = new Thread() {
      @Override
      public void run() {
        try {
          server.interpret("session_1", Test1Interpreter.class.getName(), "SLEEP", intpContext);
        } catch (TException e) {
          fail("Failure happened: " + e.getMessage());
        }
      }
    };
    sleepThread.start();

    Thread.sleep(1000);
    assertFalse(interpreter1.cancelled.get());
    server.cancel("session_1", Test1Interpreter.class.getName(), intpContext);
    // Sleep 1 second, because cancel is async.
    Thread.sleep(1000);
    assertTrue(interpreter1.cancelled.get());

    // getProgress
    assertEquals(10, server.getProgress("session_1", Test1Interpreter.class.getName(),
        intpContext));

    // close
    server.close("session_1", Test1Interpreter.class.getName());
    assertTrue(interpreter1.closed.get());
  }

  public static class Test1Interpreter extends Interpreter {

    AtomicBoolean cancelled = new AtomicBoolean();
    AtomicBoolean closed = new AtomicBoolean();

    public Test1Interpreter(Properties properties) {
      super(properties);
    }

    @Override
    public void open() {

    }

    @Override
    public InterpreterResult interpret(String st, InterpreterContext context) {
      if (st.equals("SINGLE_OUTPUT_SUCCESS")) {
        return new InterpreterResult(InterpreterResult.Code.SUCCESS, "SINGLE_OUTPUT_SUCCESS");
      } else if (st.equals("SINGLE_OUTPUT_ERROR")) {
        return new InterpreterResult(InterpreterResult.Code.ERROR, "SINGLE_OUTPUT_ERROR");
      } else if (st.equals("COMBO_OUTPUT_SUCCESS")) {
        try {
          context.out.write("INTERPRETER_OUT");
        } catch (IOException e) {
          fail("Failure happened: " + e.getMessage());
        }
        return new InterpreterResult(InterpreterResult.Code.SUCCESS, "SINGLE_OUTPUT_SUCCESS");
      } else if (st.equals("SLEEP")) {
        try {
          Thread.sleep(3 * 1000);
        } catch (InterruptedException e) {
          fail("Failure happened: " + e.getMessage());
        }
        return new InterpreterResult(InterpreterResult.Code.SUCCESS, "SLEEP_SUCCESS");
      }
      return null;
    }

    @Override
    public void cancel(InterpreterContext context) throws InterpreterException {
      cancelled.set(true);
    }

    @Override
    public FormType getFormType() throws InterpreterException {
      return FormType.NATIVE;
    }

    @Override
    public int getProgress(InterpreterContext context) throws InterpreterException {
      return 10;
    }

    @Override
    public void close() {
      closed.set(true);
    }

  }

  public static class Test2Interpreter extends Interpreter {


    public Test2Interpreter(Properties properties) {
      super(properties);
    }

    @Override
    public void open() {

    }

    @Override
    public InterpreterResult interpret(String st, InterpreterContext context) {
      return null;
    }

    @Override
    public void cancel(InterpreterContext context) throws InterpreterException {

    }

    @Override
    public FormType getFormType() throws InterpreterException {
      return FormType.NATIVE;
    }

    @Override
    public int getProgress(InterpreterContext context) throws InterpreterException {
      return 0;
    }

    @Override
    public void close() {

    }

  }
}
