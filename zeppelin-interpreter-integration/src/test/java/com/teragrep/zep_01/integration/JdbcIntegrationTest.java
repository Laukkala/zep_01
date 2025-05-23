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

package com.teragrep.zep_01.integration;

import com.teragrep.zep_01.interpreter.ExecutionContext;
import com.teragrep.zep_01.interpreter.Interpreter;
import com.teragrep.zep_01.interpreter.InterpreterContext;
import com.teragrep.zep_01.interpreter.InterpreterException;
import com.teragrep.zep_01.interpreter.InterpreterFactory;
import com.teragrep.zep_01.interpreter.InterpreterResult;
import com.teragrep.zep_01.interpreter.InterpreterSetting;
import com.teragrep.zep_01.interpreter.InterpreterSettingManager;
import com.teragrep.zep_01.user.AuthenticationInfo;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JdbcIntegrationTest {

  private static MiniZeppelin zeppelin;
  private static InterpreterFactory interpreterFactory;
  private static InterpreterSettingManager interpreterSettingManager;


  @BeforeClass
  public static void setUp() throws IOException {
    zeppelin = new MiniZeppelin();
    zeppelin.start(JdbcIntegrationTest.class);
    interpreterFactory = zeppelin.getInterpreterFactory();
    interpreterSettingManager = zeppelin.getInterpreterSettingManager();
  }

  @AfterClass
  public static void tearDown() throws IOException {
    if (zeppelin != null) {
      zeppelin.stop();
    }
  }

  @Ignore(value="java.lang.ClassNotFoundException: com.mysql.jdbc.Driver as the dependency downloader has been deprecated")
  @Test
  public void testMySql() throws InterpreterException, InterruptedException {
    InterpreterSetting interpreterSetting = interpreterSettingManager.getInterpreterSettingByName("jdbc");
    interpreterSetting.setProperty("default.driver", "com.mysql.jdbc.Driver");
    interpreterSetting.setProperty("default.url", "jdbc:mysql://localhost:3306/");
    interpreterSetting.setProperty("default.user", "root");
    interpreterSetting.setProperty("default.password", "root");

    interpreterSettingManager.restart(interpreterSetting.getId());
    interpreterSetting.waitForReady(60 * 1000);
    Interpreter jdbcInterpreter = interpreterFactory.getInterpreter("jdbc", new ExecutionContext("user1", "note1", "test"));
    assertNotNull("JdbcInterpreter is null", jdbcInterpreter);

    InterpreterContext context = new InterpreterContext.Builder()
            .setNoteId("note1")
            .setParagraphId("paragraph_1")
            .setAuthenticationInfo(AuthenticationInfo.ANONYMOUS)
            .build();
    InterpreterResult interpreterResult = jdbcInterpreter.interpret("show databases;", context);
    assertEquals(interpreterResult.toString(), InterpreterResult.Code.SUCCESS, interpreterResult.code());

    context.getLocalProperties().put("saveAs", "table_1");
    interpreterResult = jdbcInterpreter.interpret("SELECT 1 as c1, 2 as c2;", context);
    assertEquals(interpreterResult.toString(), InterpreterResult.Code.SUCCESS, interpreterResult.code());
    assertEquals(1, interpreterResult.message().size());
    assertEquals(InterpreterResult.Type.TABLE, interpreterResult.message().get(0).getType());
    assertEquals("c1\tc2\n1\t2\n", interpreterResult.message().get(0).getData());

    // read table_1 from python interpreter
    InterpreterSetting pythonInterpreterSetting = interpreterSettingManager.getInterpreterSettingByName("python");
    pythonInterpreterSetting.setProperty("zeppelin.python.gatewayserver_address", "127.0.0.1");

    Interpreter pythonInterpreter = interpreterFactory.getInterpreter("python", new ExecutionContext("user1", "note1", "test"));
    assertNotNull("PythonInterpreter is null", pythonInterpreter);

    context = new InterpreterContext.Builder()
            .setNoteId("note1")
            .setParagraphId("paragraph_1")
            .setAuthenticationInfo(AuthenticationInfo.ANONYMOUS)
            .build();
    interpreterResult = pythonInterpreter.interpret("df=z.getAsDataFrame('table_1')\nz.show(df)", context);
    assertEquals(interpreterResult.toString(), InterpreterResult.Code.SUCCESS, interpreterResult.code());
    assertEquals(1, interpreterResult.message().size());
    assertEquals(InterpreterResult.Type.TABLE, interpreterResult.message().get(0).getType());
    assertEquals("c1\tc2\n1\t2\n", interpreterResult.message().get(0).getData());
  }
}
