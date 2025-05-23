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

import org.apache.commons.io.IOUtils;
import com.teragrep.zep_01.client.ClientConfig;
import com.teragrep.zep_01.client.ExecuteResult;
import com.teragrep.zep_01.client.websocket.SimpleMessageHandler;
import com.teragrep.zep_01.client.Status;
import com.teragrep.zep_01.client.ZSession;
import com.teragrep.zep_01.conf.ZeppelinConfiguration;
import com.teragrep.zep_01.interpreter.lifecycle.TimeoutLifecycleManager;
import com.teragrep.zep_01.notebook.Note;
import com.teragrep.zep_01.notebook.Notebook;
import com.teragrep.zep_01.rest.AbstractTestRestApi;
import com.teragrep.zep_01.utils.TestUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Ignore;

public class ZSessionIntegrationTest extends AbstractTestRestApi {

  private static Notebook notebook;
  private static String sparkHome;
  private static String flinkHome;

  private ClientConfig clientConfig = new ClientConfig("http://localhost:8080");


  @BeforeClass
  public static void setUp() throws Exception {
    System.setProperty(ZeppelinConfiguration.ConfVars.ZEPPELIN_ALLOWED_ORIGINS.getVarName(), "*");
    System.setProperty(ZeppelinConfiguration.ConfVars.ZEPPELIN_SESSION_CHECK_INTERVAL.getVarName(), "5000");

    AbstractTestRestApi.startUp(ZSessionIntegrationTest.class.getSimpleName());
    ZeppelinConfiguration zConf = ZeppelinConfiguration.create();
    zConf.setProperty(ZeppelinConfiguration.ConfVars.ZEPPELIN_INTERPRETER_LIFECYCLE_MANAGER_CLASS.getVarName(), TimeoutLifecycleManager.class.getName());
    zConf.setProperty(ZeppelinConfiguration.ConfVars.ZEPPELIN_INTERPRETER_LIFECYCLE_MANAGER_TIMEOUT_CHECK_INTERVAL.getVarName(), "5000");
    zConf.setProperty(ZeppelinConfiguration.ConfVars.ZEPPELIN_INTERPRETER_LIFECYCLE_MANAGER_TIMEOUT_THRESHOLD.getVarName(), "10000");

    notebook = TestUtils.getInstance(Notebook.class);
    // sparkHome = DownloadUtils.downloadSpark("3.4.2", "3");
  }

  @AfterClass
  public static void destroy() throws Exception {
    AbstractTestRestApi.shutDown();
  }

  @Ignore(value="Depends on shell interpreter")
  @Test
  public void testZSession_Shell() throws Exception {
    ZSession session = ZSession.builder()
            .setClientConfig(clientConfig)
            .setInterpreter("sh")
            .build();

    try {
      session.start();
      assertNull(session.getWeburl());
      assertNotNull(session.getNoteId());

      Note note = notebook.getNote(session.getNoteId());
      assertEquals(2, note.getParagraphCount());
      assertTrue(note.getParagraph(0).getText(), note.getParagraph(0).getText().startsWith("%sh.conf"));

      ExecuteResult result = session.execute("pwd");
      assertEquals(result.toString(), Status.FINISHED, result.getStatus());
      assertEquals(1, result.getResults().size());
      assertEquals("TEXT", result.getResults().get(0).getType());

      result = session.execute("invalid_command");
      assertEquals(Status.ERROR, result.getStatus());
      assertEquals(2, result.getResults().size());
      assertEquals("TEXT", result.getResults().get(0).getType());
      assertTrue(result.getResults().get(0).getData(), result.getResults().get(0).getData().contains("command not found"));
      assertEquals("TEXT", result.getResults().get(1).getType());
      assertTrue(result.getResults().get(1).getData(), result.getResults().get(1).getData().contains("ExitValue"));

      assertEquals(4, note.getParagraphCount());
      assertEquals("%sh invalid_command", note.getParagraph(3).getText());

    } finally {
      session.stop();
    }
  }

  @Ignore(value="Depends on shell interpreter")
  @Test
  public void testZSession_Shell_Submit() throws Exception {
    ZSession session = ZSession.builder()
            .setClientConfig(clientConfig)
            .setInterpreter("sh")
            .build();

    try {
      session.start();
      assertNull(session.getWeburl());
      assertNotNull(session.getNoteId());

      Note note = notebook.getNote(session.getNoteId());
      assertEquals(2, note.getParagraphCount());
      assertTrue(note.getParagraph(0).getText(), note.getParagraph(0).getText().startsWith("%sh.conf"));

      ExecuteResult result = session.submit("sleep 10\npwd");
      assertFalse("Status is: " + result.getStatus().toString(), result.getStatus().isCompleted());
      result = session.waitUntilFinished(result.getStatementId());
      assertEquals(result.toString(), Status.FINISHED, result.getStatus());
      assertEquals(1, result.getResults().size());
      assertEquals("TEXT", result.getResults().get(0).getType());

      result = session.submit("invalid_command");
      result = session.waitUntilFinished(result.getStatementId());
      assertEquals(Status.ERROR, result.getStatus());
      assertEquals(2, result.getResults().size());
      assertEquals("TEXT", result.getResults().get(0).getType());
      assertTrue(result.getResults().get(0).getData(), result.getResults().get(0).getData().contains("command not found"));
      assertEquals("TEXT", result.getResults().get(1).getType());
      assertTrue(result.getResults().get(1).getData(), result.getResults().get(1).getData().contains("ExitValue"));

      assertEquals(4, note.getParagraphCount());
      assertEquals("%sh invalid_command", note.getParagraph(3).getText());

    } finally {
      session.stop();
    }
  }

  @Ignore(value="PySpark seems to fail")
  @Test
  public void testZSession_Spark() throws Exception {
    Map<String, String> intpProperties = new HashMap<>();
    intpProperties.put("SPARK_HOME", sparkHome);
    intpProperties.put("spark.master", "local[*]");

    ZSession session = ZSession.builder()
            .setClientConfig(clientConfig)
            .setInterpreter("spark")
            .setIntpProperties(intpProperties)
            .build();

    try {
      session.start();
      assertNotNull(session.getWeburl());
      assertNotNull(session.getNoteId());

      // scala
      ExecuteResult result = session.execute("sc.version");
      assertEquals(result.toString(), Status.FINISHED, result.getStatus());
      assertEquals(1, result.getResults().size());
      assertEquals("TEXT", result.getResults().get(0).getType());
      assertTrue(result.getResults().get(0).getData(), result.getResults().get(0).getData().contains("3.4.2"));
      assertEquals(0, result.getJobUrls().size());

      // pyspark
      result = session.execute("pyspark", "df = spark.createDataFrame([(1,'a'),(2,'b')])\ndf.registerTempTable('df')\ndf.show()");
      assertEquals(Status.FINISHED, result.getStatus());
      assertEquals(1, result.getResults().size());
      assertEquals("TEXT", result.getResults().get(0).getType());
      assertEquals(
              "+---+---+\n" +
              "| _1| _2|\n" +
              "+---+---+\n" +
              "|  1|  a|\n" +
              "|  2|  b|\n" +
              "+---+---+", result.getResults().get(0).getData().trim());
      assertTrue(result.getJobUrls().size() > 0);

      // spark sql
      result = session.execute("sql", "select * from df");
      assertEquals(Status.FINISHED, result.getStatus());
      assertEquals(1, result.getResults().size());
      assertEquals("TABLE", result.getResults().get(0).getType());
      assertTrue(result.getResults().get(0).getData(), result.getResults().get(0).getData().contains("1\ta\n2\tb\n"));
      assertTrue(result.getJobUrls().size() > 0);

      // spark invalid sql
      result = session.execute("sql", "select * from unknown_table");
      assertEquals(Status.ERROR, result.getStatus());
      assertEquals(1, result.getResults().size());
      assertEquals("TEXT", result.getResults().get(0).getType());
      assertTrue(result.getResults().get(0).getData(), result.getResults().get(0).getData().contains("Table or view not found"));
      assertEquals(0, result.getJobUrls().size());

    } finally {
      session.stop();
    }
  }

  @Ignore(value="Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Cannot assign requested address (connect failed)")
  @Test
  public void testZSession_Spark_Submit() throws Exception {
    Map<String, String> intpProperties = new HashMap<>();
    intpProperties.put("SPARK_HOME", sparkHome);
    intpProperties.put("spark.master", "local[*]");

    ZSession session = ZSession.builder()
            .setClientConfig(clientConfig)
            .setInterpreter("spark")
            .setIntpProperties(intpProperties)
            .build();

    try {
      session.start();
      assertNotNull(session.getWeburl());
      assertNotNull(session.getNoteId());

      // scala
      ExecuteResult result = session.submit("sc.version");
      result = session.waitUntilFinished(result.getStatementId());
      assertEquals(result.toString(), Status.FINISHED, result.getStatus());
      assertEquals(1, result.getResults().size());
      assertEquals("TEXT", result.getResults().get(0).getType());
      assertTrue(result.getResults().get(0).getData(), result.getResults().get(0).getData().contains("3.4.2"));
      assertEquals(0, result.getJobUrls().size());

      // pyspark
      result = session.submit("pyspark", "df = spark.createDataFrame([(1,'a'),(2,'b')])\ndf.registerTempTable('df')\ndf.show()");
      result = session.waitUntilFinished(result.getStatementId());
      assertEquals(result.toString(), Status.FINISHED, result.getStatus());
      assertEquals(1, result.getResults().size());
      assertEquals("TEXT", result.getResults().get(0).getType());
      assertEquals(
              "+---+---+\n" +
              "| _1| _2|\n" +
              "+---+---+\n" +
              "|  1|  a|\n" +
              "|  2|  b|\n" +
              "+---+---+", result.getResults().get(0).getData().trim());
      assertTrue(result.getJobUrls().size() > 0);

      // spark sql
      result = session.submit("sql", "select * from df");
      result = session.waitUntilFinished(result.getStatementId());
      assertEquals(Status.FINISHED, result.getStatus());
      assertEquals(1, result.getResults().size());
      assertEquals("TABLE", result.getResults().get(0).getType());
      assertTrue(result.getResults().get(0).getData(), result.getResults().get(0).getData().contains("1\ta\n2\tb\n"));
      assertTrue(result.getJobUrls().size() > 0);

      // spark invalid sql
      result = session.submit("sql", "select * from unknown_table");
      result = session.waitUntilFinished(result.getStatementId());
      assertEquals(Status.ERROR, result.getStatus());
      assertEquals(1, result.getResults().size());
      assertEquals("TEXT", result.getResults().get(0).getType());
      assertTrue(result.getResults().get(0).getData(), result.getResults().get(0).getData().contains("Table or view not found"));
      assertEquals(0, result.getJobUrls().size());

      // cancel
      result = session.submit("sc.range(1,100).map(e=>{Thread.sleep(1000);e}).collect()");
      assertFalse("Status is: " + result.getStatus().toString(), result.getStatus().isCompleted());
      result = session.waitUntilRunning(result.getStatementId());
      session.cancel(result.getStatementId());
      assertEquals(result.toString(), Status.RUNNING, result.getStatus());
      result = session.waitUntilFinished(result.getStatementId());
      assertEquals(result.toString(), Status.ABORT, result.getStatus());

    } finally {
      session.stop();
    }
  }

  @Ignore(value="Connect to localhost:8080 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Cannot assign requested address (connect failed)")
  @Test
  public void testZSession_Python() throws Exception {
    Map<String, String> intpProperties = new HashMap<>();
    intpProperties.put("zeppelin.python.gatewayserver_address", "127.0.0.1");

    ZSession session = ZSession.builder()
            .setClientConfig(clientConfig)
            .setInterpreter("python")
            .setIntpProperties(intpProperties)
            .build();

    try {
      session.start(new SimpleMessageHandler());
      assertNull(session.getWeburl());
      assertNotNull(session.getNoteId());

      // python
//      ExecuteResult result = session.execute("1+1");
//      assertEquals(result.toString(), Status.FINISHED, result.getStatus());
//      assertEquals(1, result.getResults().size());
//      assertEquals("TEXT", result.getResults().get(0).getType());
//      assertTrue(result.getResults().get(0).getData(), result.getResults().get(0).getData().contains("2"));
//
//      // python
//      result = session.execute("1/0");
//      assertEquals(result.toString(), Status.ERROR, result.getStatus());
//      assertEquals(1, result.getResults().size());
//      assertEquals("TEXT", result.getResults().get(0).getType());
//      assertTrue(result.getResults().get(0).getData(), result.getResults().get(0).getData().contains("ZeroDivisionError"));

      // for loop
      ExecuteResult result = session.execute("import time\n" +
                               "for i in range(1,10):\n" +
                               "\tprint(i)\n" +
                               "\ttime.sleep(1)");
      assertEquals(result.toString(), Status.FINISHED, result.getStatus());
      assertEquals(1, result.getResults().size());
      assertEquals("TEXT", result.getResults().get(0).getType());

      Map<String, String> localProperties = new HashMap<>();
      localProperties.put("key 1", "hello world"); // contains whitespace
      localProperties.put("key,2", "a,b"); // contains comma
      result = session.execute("1+1", localProperties);
      assertEquals(result.toString(), Status.FINISHED, result.getStatus());
      assertEquals(1, result.getResults().size());
      assertEquals("TEXT", result.getResults().get(0).getType());
    } finally {
      session.stop();
    }
  }

  @Ignore(value="This is very sketchy test")
  @Test
  public void testZSessionCleanup() throws Exception {
    Map<String, String> intpProperties = new HashMap<>();
    intpProperties.put("zeppelin.python.gatewayserver_address", "127.0.0.1");

    ZSession session = ZSession.builder()
            .setClientConfig(clientConfig)
            .setInterpreter("python")
            .setIntpProperties(intpProperties)
            .build();

    try {
      session.start(new SimpleMessageHandler());
      assertNull(session.getWeburl());
      assertNotNull(session.getNoteId());

      assertTrue(notebook.getAllNotes().size() > 0);

      Thread.sleep(30 * 1000);
      assertEquals(0, notebook.getAllNotes().size());

      try {
        session.execute("1/0");
        fail("Should fail to execute code after session is stopped");
      } catch (Exception ignored) {
      }
    } finally {
      try {
        session.stop();
        fail("Should fail to stop session after it is stopped");
      } catch (Exception e) {
        assertTrue(e.getMessage().contains("No such session"));
      }
    }
  }

  @Ignore(value="This was disabled test")
  @Test
  public void testZSession_Jdbc() throws Exception {

    Map<String, String> intpProperties = new HashMap<>();
    intpProperties.put("default.driver", "com.mysql.jdbc.Driver");
    intpProperties.put("default.url", "jdbc:mysql://localhost:3306/");
    intpProperties.put("default.user", "root");

    ZSession session = ZSession.builder()
            .setClientConfig(clientConfig)
            .setInterpreter("jdbc")
            .setIntpProperties(intpProperties)
            .build();

    try {
      session.start();
      assertEquals("", session.getWeburl());
      assertNotNull(session.getNoteId());

      // show databases
      ExecuteResult result = session.execute("show databases");
      assertEquals(result.toString(), Status.FINISHED, result.getStatus());
      assertEquals(1, result.getResults().size());
      assertEquals("TABLE", result.getResults().get(0).getType());
      assertTrue(result.getResults().get(0).getData(), result.getResults().get(0).getData().contains("Database"));

      // select statement
      result = session.execute("SELECT 1 as c1, 2 as c2");
      assertEquals(result.toString(), Status.FINISHED, result.getStatus());
      assertEquals(1, result.getResults().size());
      assertEquals("TABLE", result.getResults().get(0).getType());
      assertEquals("c1\tc2\n1\t2\n", result.getResults().get(0).getData());

    } finally {
      session.stop();
    }
  }

  @Ignore(value="This was ignored test")
  @Test
  public void testZSession_Jdbc_Submit() throws Exception {

    Map<String, String> intpProperties = new HashMap<>();
    intpProperties.put("default.driver", "com.mysql.jdbc.Driver");
    intpProperties.put("default.url", "jdbc:mysql://localhost:3306/");
    intpProperties.put("default.user", "root");

    ZSession session = ZSession.builder()
            .setClientConfig(clientConfig)
            .setInterpreter("jdbc")
            .setIntpProperties(intpProperties)
            .build();

    try {
      session.start();
      assertEquals("", session.getWeburl());
      assertNotNull(session.getNoteId());

      // show databases
      ExecuteResult result = session.submit("show databases");
      result = session.waitUntilFinished(result.getStatementId());
      assertEquals(result.toString(), Status.FINISHED, result.getStatus());
      assertEquals(1, result.getResults().size());
      assertEquals("TABLE", result.getResults().get(0).getType());
      assertTrue(result.getResults().get(0).getData(), result.getResults().get(0).getData().contains("Database"));

      // select statement
      result = session.submit("SELECT 1 as c1, 2 as c2");
      result = session.waitUntilFinished(result.getStatementId());
      assertEquals(result.toString(), Status.FINISHED, result.getStatus());
      assertEquals(1, result.getResults().size());
      assertEquals("TABLE", result.getResults().get(0).getType());
      assertEquals("c1\tc2\n1\t2\n", result.getResults().get(0).getData());

    } finally {
      session.stop();
    }
  }

  public static String getInitStreamScript(int sleep_interval) throws IOException {
    return IOUtils.toString(ZSessionIntegrationTest.class.getResource("/init_stream.scala"))
            .replace("{{sleep_interval}}", sleep_interval + "");
  }
}
