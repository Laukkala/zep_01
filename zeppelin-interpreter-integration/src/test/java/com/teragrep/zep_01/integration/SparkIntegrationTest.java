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
import org.apache.hadoop.yarn.api.protocolrecords.GetApplicationsRequest;
import org.apache.hadoop.yarn.api.protocolrecords.GetApplicationsResponse;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import com.teragrep.zep_01.interpreter.ExecutionContext;
import com.teragrep.zep_01.interpreter.Interpreter;
import com.teragrep.zep_01.interpreter.InterpreterContext;
import com.teragrep.zep_01.interpreter.InterpreterException;
import com.teragrep.zep_01.interpreter.InterpreterFactory;
import com.teragrep.zep_01.interpreter.InterpreterNotFoundException;
import com.teragrep.zep_01.interpreter.InterpreterOption;
import com.teragrep.zep_01.interpreter.InterpreterResult;
import com.teragrep.zep_01.interpreter.InterpreterSetting;
import com.teragrep.zep_01.interpreter.InterpreterSettingManager;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class SparkIntegrationTest {
  private static Logger LOGGER = LoggerFactory.getLogger(SparkIntegrationTest.class);

  private static MiniHadoopCluster hadoopCluster;
  private static MiniZeppelin zeppelin;
  private static InterpreterFactory interpreterFactory;
  protected static InterpreterSettingManager interpreterSettingManager;

  private String sparkVersion;
  private String sparkHome;

  public SparkIntegrationTest(String sparkVersion, String hadoopVersion) {
    LOGGER.debug("Testing Spark Version: " + sparkVersion);
    LOGGER.debug("Testing Hadoop Version: " + hadoopVersion);
    this.sparkVersion = sparkVersion;
    // this.sparkHome = DownloadUtils.downloadSpark(sparkVersion, hadoopVersion);
  }

  @BeforeClass
  public static void setUp() throws IOException {
    hadoopCluster = new MiniHadoopCluster();
    hadoopCluster.start();

    zeppelin = new MiniZeppelin();
    zeppelin.start(SparkIntegrationTest.class);
    interpreterFactory = zeppelin.getInterpreterFactory();
    interpreterSettingManager = zeppelin.getInterpreterSettingManager();
  }

  @AfterClass
  public static void tearDown() throws IOException {
    if (zeppelin != null) {
      zeppelin.stop();
    }
    if (hadoopCluster != null) {
      hadoopCluster.stop();
    }
  }

  protected void setUpSparkInterpreterSetting(InterpreterSetting interpreterSetting) {
    // sub class can customize spark interpreter setting.
  }

  private void testInterpreterBasics() throws IOException, InterpreterException, XmlPullParserException {
    // add jars & packages for testing
    InterpreterSetting sparkInterpreterSetting = interpreterSettingManager.getInterpreterSettingByName("spark");
    sparkInterpreterSetting.setProperty("spark.jars.packages", "com.maxmind.geoip2:geoip2:2.5.0");
    sparkInterpreterSetting.setProperty("SPARK_PRINT_LAUNCH_COMMAND", "true");
    sparkInterpreterSetting.setProperty("zeppelin.python.gatewayserver_address", "127.0.0.1");

    MavenXpp3Reader reader = new MavenXpp3Reader();
    Model model = reader.read(new FileReader("pom.xml"));
    sparkInterpreterSetting.setProperty("spark.jars", new File("target/zeppelin-interpreter-integration-" + model.getVersion() + ".jar").getAbsolutePath());

    // test SparkInterpreter
    Interpreter sparkInterpreter = interpreterFactory.getInterpreter("spark.spark", new ExecutionContext("user1", "note1", "test"));

    InterpreterContext context = new InterpreterContext.Builder().setNoteId("note1").setParagraphId("paragraph_1").build();
    InterpreterResult interpreterResult = sparkInterpreter.interpret("sc.version", context);
    assertEquals(interpreterResult.toString(), InterpreterResult.Code.SUCCESS, interpreterResult.code());
    String detectedSparkVersion = interpreterResult.message().get(0).getData();
    assertTrue(detectedSparkVersion + " doesn't contain " + this.sparkVersion, detectedSparkVersion.contains(this.sparkVersion));
    interpreterResult = sparkInterpreter.interpret("sc.range(1,10).sum()", context);
    assertEquals(interpreterResult.toString(), InterpreterResult.Code.SUCCESS, interpreterResult.code());
    assertTrue(interpreterResult.toString(), interpreterResult.message().get(0).getData().contains("45"));

    interpreterResult = sparkInterpreter.interpret("sc.getConf.get(\"spark.user.name\")", context);
    assertEquals(interpreterResult.toString(), InterpreterResult.Code.SUCCESS, interpreterResult.code());
    assertTrue(interpreterResult.toString(), interpreterResult.message().get(0).getData().contains("user1"));

    // test jars & packages can be loaded correctly
    interpreterResult = sparkInterpreter.interpret("import com.teragrep.zep_01.interpreter.integration.DummyClass\n" +
            "import com.maxmind.geoip2._", context);
    assertEquals(interpreterResult.toString(), InterpreterResult.Code.SUCCESS, interpreterResult.code());

    // test PySparkInterpreter
    Interpreter pySparkInterpreter = interpreterFactory.getInterpreter("spark.pyspark", new ExecutionContext("user1", "note1", "test"));
    interpreterResult = pySparkInterpreter.interpret("sqlContext.createDataFrame([(1,'a'),(2,'b')], ['id','name']).registerTempTable('test')", context);
    assertEquals(interpreterResult.toString(), InterpreterResult.Code.SUCCESS, interpreterResult.code());

    // test SparkSQLInterpreter
    Interpreter sqlInterpreter = interpreterFactory.getInterpreter("spark.sql", new ExecutionContext("user1", "note1", "test"));
    interpreterResult = sqlInterpreter.interpret("select count(1) as c from test", context);
    assertEquals(interpreterResult.toString(), InterpreterResult.Code.SUCCESS, interpreterResult.code());
    assertEquals(interpreterResult.toString(), InterpreterResult.Type.TABLE, interpreterResult.message().get(0).getType());
    assertEquals(interpreterResult.toString(), "c\n2\n", interpreterResult.message().get(0).getData());
  }

  @Test
  public void testLocalMode() throws IOException, YarnException, InterpreterException, XmlPullParserException {
    InterpreterSetting sparkInterpreterSetting = interpreterSettingManager.getInterpreterSettingByName("spark");
    sparkInterpreterSetting.setProperty("spark.master", "local[*]");
    sparkInterpreterSetting.setProperty("SPARK_HOME", sparkHome);
    sparkInterpreterSetting.setProperty("ZEPPELIN_CONF_DIR", zeppelin.getZeppelinConfDir().getAbsolutePath());
    sparkInterpreterSetting.setProperty("zeppelin.spark.useHiveContext", "false");
    sparkInterpreterSetting.setProperty("zeppelin.spark.scala.color", "false");
    sparkInterpreterSetting.setProperty("zeppelin.spark.deprecatedMsg.show", "false");
    sparkInterpreterSetting.setProperty("spark.user.name", "#{user}");

    try {
      setUpSparkInterpreterSetting(sparkInterpreterSetting);
      testInterpreterBasics();

      // no yarn application launched
      GetApplicationsRequest request = GetApplicationsRequest.newInstance(EnumSet.of(YarnApplicationState.RUNNING));
      GetApplicationsResponse response = hadoopCluster.getYarnCluster().getResourceManager().getClientRMService().getApplications(request);
      assertEquals(0, response.getApplicationList().size());
    } finally {
      interpreterSettingManager.close();
    }
  }

  @Test
  public void testYarnClientMode() throws IOException, YarnException, InterruptedException, InterpreterException, XmlPullParserException {
    InterpreterSetting sparkInterpreterSetting = interpreterSettingManager.getInterpreterSettingByName("spark");
    sparkInterpreterSetting.setProperty("spark.master", "yarn-client");
    sparkInterpreterSetting.setProperty("HADOOP_CONF_DIR", hadoopCluster.getConfigPath());
    sparkInterpreterSetting.setProperty("SPARK_HOME", sparkHome);
    sparkInterpreterSetting.setProperty("ZEPPELIN_CONF_DIR", zeppelin.getZeppelinConfDir().getAbsolutePath());
    sparkInterpreterSetting.setProperty("zeppelin.spark.useHiveContext", "false");
    sparkInterpreterSetting.setProperty("PYSPARK_PYTHON", "/usr/bin/python");
    sparkInterpreterSetting.setProperty("spark.driver.memory", "512m");
    sparkInterpreterSetting.setProperty("zeppelin.spark.scala.color", "false");
    sparkInterpreterSetting.setProperty("zeppelin.spark.deprecatedMsg.show", "false");
    sparkInterpreterSetting.setProperty("spark.user.name", "#{user}");
    sparkInterpreterSetting.setProperty("zeppelin.spark.run.asLoginUser", "false");

    try {
      setUpSparkInterpreterSetting(sparkInterpreterSetting);
      testInterpreterBasics();

      // 1 yarn application launched
      GetApplicationsRequest request = GetApplicationsRequest.newInstance(EnumSet.of(YarnApplicationState.RUNNING));
      GetApplicationsResponse response = hadoopCluster.getYarnCluster().getResourceManager().getClientRMService().getApplications(request);
      assertEquals(1, response.getApplicationList().size());

    } finally {
      interpreterSettingManager.close();
      waitForYarnAppCompleted(30 * 1000);
    }
  }

  private void waitForYarnAppCompleted(int timeout) throws YarnException {
    long start = System.currentTimeMillis();
    boolean yarnAppCompleted = false;
    while ((System.currentTimeMillis() - start) < timeout ) {
      GetApplicationsRequest request = GetApplicationsRequest.newInstance(EnumSet.of(YarnApplicationState.RUNNING));
      GetApplicationsResponse response = hadoopCluster.getYarnCluster().getResourceManager().getClientRMService().getApplications(request);
      if (response.getApplicationList().isEmpty()) {
        yarnAppCompleted = true;
        break;
      }
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        fail("Failure happened: " + e.getMessage());
      }
    }
    assertTrue("Yarn app is not completed in " + timeout + " milliseconds.", yarnAppCompleted);
  }

  @Test
  public void testYarnClusterMode() throws IOException, YarnException, InterruptedException, InterpreterException, XmlPullParserException {
    InterpreterSetting sparkInterpreterSetting = interpreterSettingManager.getInterpreterSettingByName("spark");
    sparkInterpreterSetting.setProperty("spark.master", "yarn-cluster");
    sparkInterpreterSetting.setProperty("HADOOP_CONF_DIR", hadoopCluster.getConfigPath());
    sparkInterpreterSetting.setProperty("SPARK_HOME", sparkHome);
    sparkInterpreterSetting.setProperty("ZEPPELIN_CONF_DIR", zeppelin.getZeppelinConfDir().getAbsolutePath());
    sparkInterpreterSetting.setProperty("zeppelin.spark.useHiveContext", "false");
    sparkInterpreterSetting.setProperty("PYSPARK_PYTHON", "/usr/bin/python");
    sparkInterpreterSetting.setProperty("spark.driver.memory", "512m");
    sparkInterpreterSetting.setProperty("zeppelin.spark.scala.color", "false");
    sparkInterpreterSetting.setProperty("zeppelin.spark.deprecatedMsg.show", "false");
    sparkInterpreterSetting.setProperty("spark.user.name", "#{user}");
    sparkInterpreterSetting.setProperty("zeppelin.spark.run.asLoginUser", "false");
    // parameters with whitespace
    sparkInterpreterSetting.setProperty("spark.app.name", "hello spark");

    try {
      setUpSparkInterpreterSetting(sparkInterpreterSetting);
      testInterpreterBasics();

      // 1 yarn application launched
      GetApplicationsRequest request = GetApplicationsRequest.newInstance(EnumSet.of(YarnApplicationState.RUNNING));
      GetApplicationsResponse response = hadoopCluster.getYarnCluster().getResourceManager().getClientRMService().getApplications(request);
      assertEquals(1, response.getApplicationList().size());
      assertEquals("hello spark", response.getApplicationList().get(0).getName());

    } finally {
      interpreterSettingManager.close();
      waitForYarnAppCompleted(30 * 1000);
    }
  }

  @Test
  public void testSparkSubmit() throws InterpreterException {
    try {
      InterpreterSetting sparkSubmitInterpreterSetting = interpreterSettingManager.getInterpreterSettingByName("spark-submit");
      sparkSubmitInterpreterSetting.setProperty("SPARK_HOME", sparkHome);
      // test SparkSubmitInterpreter
      InterpreterContext context = new InterpreterContext.Builder().setNoteId("note1").setParagraphId("paragraph_1").build();
      Interpreter sparkSubmitInterpreter = interpreterFactory.getInterpreter("spark-submit", new ExecutionContext("user1", "note1", "test"));
      InterpreterResult interpreterResult = sparkSubmitInterpreter.interpret("--class org.apache.spark.examples.SparkPi " + sparkHome + "/examples/jars/spark-examples*.jar ", context);

      assertEquals(interpreterResult.toString(), InterpreterResult.Code.SUCCESS, interpreterResult.code());
    } finally {
      interpreterSettingManager.close();
    }
  }

  @Test
  public void testScopedMode() throws InterpreterException {
    InterpreterSetting sparkInterpreterSetting = interpreterSettingManager.getInterpreterSettingByName("spark");
    try {
      sparkInterpreterSetting.setProperty("spark.master", "local[*]");
      sparkInterpreterSetting.setProperty("spark.submit.deployMode", "client");
      sparkInterpreterSetting.setProperty("SPARK_HOME", sparkHome);
      sparkInterpreterSetting.setProperty("ZEPPELIN_CONF_DIR", zeppelin.getZeppelinConfDir().getAbsolutePath());
      sparkInterpreterSetting.setProperty("zeppelin.spark.useHiveContext", "false");
      sparkInterpreterSetting.setProperty("zeppelin.spark.scala.color", "false");
      sparkInterpreterSetting.setProperty("zeppelin.spark.deprecatedMsg.show", "false");
      sparkInterpreterSetting.getOption().setPerNote(InterpreterOption.SCOPED);


      Interpreter sparkInterpreter1 = interpreterFactory.getInterpreter("spark.spark", new ExecutionContext("user1", "note1", "test"));

      InterpreterContext context = new InterpreterContext.Builder().setNoteId("note1").setParagraphId("paragraph_1").build();
      InterpreterResult interpreterResult = sparkInterpreter1.interpret("sc.range(1,10).map(e=>e+1).sum()", context);
      assertEquals(interpreterResult.toString(), InterpreterResult.Code.SUCCESS, interpreterResult.code());
      assertTrue(interpreterResult.toString(), interpreterResult.message().get(0).getData().contains("54"));

      Interpreter sparkInterpreter2 = interpreterFactory.getInterpreter("spark.spark", new ExecutionContext("user1", "note2", "test"));
      assertNotEquals(sparkInterpreter1, sparkInterpreter2);

      context = new InterpreterContext.Builder().setNoteId("note2").setParagraphId("paragraph_1").build();
      interpreterResult = sparkInterpreter2.interpret("sc.range(1,10).map(e=>e+1).sum()", context);
      assertEquals(interpreterResult.toString(), InterpreterResult.Code.SUCCESS, interpreterResult.code());
      assertTrue(interpreterResult.toString(), interpreterResult.message().get(0).getData().contains("54"));
    } finally {
      interpreterSettingManager.close();

      if (sparkInterpreterSetting != null) {
        // reset InterpreterOption so that it won't affect other tests.
        sparkInterpreterSetting.getOption().setPerNote(InterpreterOption.SHARED);
      }
    }
  }
}
