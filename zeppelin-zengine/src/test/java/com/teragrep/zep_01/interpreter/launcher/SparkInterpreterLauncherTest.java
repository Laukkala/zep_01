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

package com.teragrep.zep_01.interpreter.launcher;

import org.apache.commons.io.FileUtils;
import com.teragrep.zep_01.conf.ZeppelinConfiguration;
import com.teragrep.zep_01.interpreter.InterpreterOption;
import com.teragrep.zep_01.interpreter.remote.ExecRemoteInterpreterProcess;
import com.teragrep.zep_01.util.Util;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Ignore(value="Depends on old spark 2.x version, requires sparkr that is not being supported")
public class SparkInterpreterLauncherTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(SparkInterpreterLauncher.class);

  private String sparkHome;
  private String zeppelinHome;

  @Before
  public void setUp() {
    for (final ZeppelinConfiguration.ConfVars confVar : ZeppelinConfiguration.ConfVars.values()) {
      System.clearProperty(confVar.getVarName());
    }

    // sparkHome = DownloadUtils.downloadSpark("2.4.4", "2.7");
    System.setProperty(ZeppelinConfiguration.ConfVars.ZEPPELIN_HOME.getVarName(),
            new File("..").getAbsolutePath());

    zeppelinHome = ZeppelinConfiguration.create().getZeppelinHome();
    LOGGER.debug("ZEPPELIN_HOME: " + zeppelinHome);
  }

  @Test
  public void testConnectTimeOut() throws IOException {
    ZeppelinConfiguration zConf = ZeppelinConfiguration.create();
    SparkInterpreterLauncher launcher = new SparkInterpreterLauncher(zConf, null);
    Properties properties = new Properties();
    properties.setProperty("SPARK_HOME", sparkHome);
    properties.setProperty(
        ZeppelinConfiguration.ConfVars.ZEPPELIN_INTERPRETER_CONNECT_TIMEOUT.getVarName(), "10000");
    InterpreterOption option = new InterpreterOption();
    option.setUserImpersonate(true);
    InterpreterLaunchContext context = new InterpreterLaunchContext(properties, option, null, "user1", "intpGroupId", "groupId", "groupName", "name", 0, "host");
    InterpreterClient client = launcher.launch(context);
    assertTrue(client instanceof ExecRemoteInterpreterProcess);
    ExecRemoteInterpreterProcess interpreterProcess = (ExecRemoteInterpreterProcess) client;
    assertEquals("name", interpreterProcess.getInterpreterSettingName());
    assertEquals(zeppelinHome + "/interpreter/groupName", interpreterProcess.getInterpreterDir());
    assertEquals(10000, interpreterProcess.getConnectTimeout());
    assertEquals(zConf.getInterpreterRemoteRunnerPath(), interpreterProcess.getInterpreterRunner());
    assertTrue(interpreterProcess.getEnv().size() >= 2);
    assertTrue(interpreterProcess.isUserImpersonated());
  }

  @Test
  public void testLocalMode() throws IOException {
    ZeppelinConfiguration zConf = ZeppelinConfiguration.create();
    SparkInterpreterLauncher launcher = new SparkInterpreterLauncher(zConf, null);
    Properties properties = new Properties();
    properties.setProperty("SPARK_HOME", sparkHome);
    properties.setProperty("ENV_1", "");
    properties.setProperty("property_1", "value_1");
    properties.setProperty("spark.master", "local[*]");
    properties.setProperty("spark.files", "file_1");
    properties.setProperty("spark.jars", "jar_1");

    InterpreterOption option = new InterpreterOption();
    InterpreterLaunchContext context = new InterpreterLaunchContext(properties, option, null, "user1", "intpGroupId", "groupId", "spark", "spark", 0, "host");
    InterpreterClient client = launcher.launch(context);
    assertTrue( client instanceof ExecRemoteInterpreterProcess);
    ExecRemoteInterpreterProcess interpreterProcess = (ExecRemoteInterpreterProcess) client;
    assertEquals("spark", interpreterProcess.getInterpreterSettingName());
    assertTrue(interpreterProcess.getInterpreterDir().endsWith("/interpreter/spark"));
    assertEquals(zConf.getInterpreterRemoteRunnerPath(), interpreterProcess.getInterpreterRunner());
    assertTrue(interpreterProcess.getEnv().size() >= 2);
    assertEquals(sparkHome, interpreterProcess.getEnv().get("SPARK_HOME"));
    assertFalse(interpreterProcess.getEnv().containsKey("ENV_1"));
    assertEquals("--conf|spark.files=file_1" +
                    "|--conf|spark.jars=jar_1|--conf|spark.app.name=intpGroupId|--conf|spark.master=local[*]",
            interpreterProcess.getEnv().get("ZEPPELIN_SPARK_CONF"));
  }

  @Test
  public void testYarnClientMode_1() throws IOException {
    ZeppelinConfiguration zConf = ZeppelinConfiguration.create();
    SparkInterpreterLauncher launcher = new SparkInterpreterLauncher(zConf, null);
    Properties properties = new Properties();
    properties.setProperty("SPARK_HOME", sparkHome);
    properties.setProperty("property_1", "value_1");
    properties.setProperty("spark.master", "yarn-client");
    properties.setProperty("spark.files", "file_1");
    properties.setProperty("spark.jars", "jar_1");

    InterpreterOption option = new InterpreterOption();
    InterpreterLaunchContext context = new InterpreterLaunchContext(properties, option, null, "user1", "intpGroupId", "groupId", "spark", "spark", 0, "host");
    InterpreterClient client = launcher.launch(context);
    assertTrue( client instanceof ExecRemoteInterpreterProcess);
    ExecRemoteInterpreterProcess interpreterProcess = (ExecRemoteInterpreterProcess) client;
    assertEquals("spark", interpreterProcess.getInterpreterSettingName());
    assertTrue(interpreterProcess.getInterpreterDir().endsWith("/interpreter/spark"));
    assertEquals(zConf.getInterpreterRemoteRunnerPath(), interpreterProcess.getInterpreterRunner());
    assertTrue(interpreterProcess.getEnv().size() >= 2);
    assertEquals(sparkHome, interpreterProcess.getEnv().get("SPARK_HOME"));

    String sparkJars = "jar_1";
    String sparkrZip = sparkHome + "/R/lib/sparkr.zip#sparkr";
    String sparkFiles = "file_1";
    assertEquals("--conf|spark.yarn.dist.archives=" + sparkrZip +
                    "|--conf|spark.files=" + sparkFiles + "|--conf|spark.jars=" + sparkJars +
                    "|--conf|spark.yarn.isPython=true|--conf|spark.app.name=intpGroupId|--conf|spark.master=yarn-client",
            interpreterProcess.getEnv().get("ZEPPELIN_SPARK_CONF"));
  }

  @Test
  public void testYarnClientMode_2() throws IOException {
    ZeppelinConfiguration zConf = ZeppelinConfiguration.create();
    SparkInterpreterLauncher launcher = new SparkInterpreterLauncher(zConf, null);
    Properties properties = new Properties();
    properties.setProperty("SPARK_HOME", sparkHome);
    properties.setProperty("property_1", "value_1");
    properties.setProperty("spark.master", "yarn");
    properties.setProperty("spark.submit.deployMode", "client");
    properties.setProperty("spark.files", "file_1");
    properties.setProperty("spark.jars", "jar_1");

    InterpreterOption option = new InterpreterOption();
    InterpreterLaunchContext context = new InterpreterLaunchContext(properties, option, null, "user1", "intpGroupId", "groupId", "spark", "spark", 0, "host");
    InterpreterClient client = launcher.launch(context);
    assertTrue( client instanceof ExecRemoteInterpreterProcess);
    ExecRemoteInterpreterProcess interpreterProcess = (ExecRemoteInterpreterProcess) client;
    assertEquals("spark", interpreterProcess.getInterpreterSettingName());
    assertTrue(interpreterProcess.getInterpreterDir().endsWith("/interpreter/spark"));
    assertEquals(zConf.getInterpreterRemoteRunnerPath(), interpreterProcess.getInterpreterRunner());
    assertTrue(interpreterProcess.getEnv().size() >= 2);
    assertEquals(sparkHome, interpreterProcess.getEnv().get("SPARK_HOME"));

    String sparkJars = "jar_1";
    String sparkrZip = sparkHome + "/R/lib/sparkr.zip#sparkr";
    String sparkFiles = "file_1";
    assertEquals("--conf|spark.yarn.dist.archives=" + sparkrZip +
                    "|--conf|spark.files=" + sparkFiles + "|--conf|spark.jars=" + sparkJars +
                    "|--conf|spark.submit.deployMode=client" +
                    "|--conf|spark.yarn.isPython=true|--conf|spark.app.name=intpGroupId|--conf|spark.master=yarn",
            interpreterProcess.getEnv().get("ZEPPELIN_SPARK_CONF"));
  }

  @Ignore("This test is broken: java.lang.RuntimeException: No such note: note1")
  @Test
  public void testYarnClusterMode_1() throws IOException {
    ZeppelinConfiguration zConf = ZeppelinConfiguration.create();
    SparkInterpreterLauncher launcher = new SparkInterpreterLauncher(zConf, null);
    Properties properties = new Properties();
    properties.setProperty("SPARK_HOME", sparkHome);
    properties.setProperty("property_1", "value_1");
    properties.setProperty("spark.master", "yarn-cluster");
    properties.setProperty("spark.files", "file_1");
    properties.setProperty("spark.jars", "jar_1");

    InterpreterOption option = new InterpreterOption();
    InterpreterLaunchContext context = new InterpreterLaunchContext(properties, option, null, "user1", "intpGroupId", "groupId", "spark", "spark", 0, "host");
    InterpreterClient client = launcher.launch(context);
    assertTrue( client instanceof ExecRemoteInterpreterProcess);
    ExecRemoteInterpreterProcess interpreterProcess = (ExecRemoteInterpreterProcess) client;
    assertEquals("spark", interpreterProcess.getInterpreterSettingName());
    assertTrue(interpreterProcess.getInterpreterDir().endsWith("/interpreter/spark"));
    assertEquals(zConf.getInterpreterRemoteRunnerPath(), interpreterProcess.getInterpreterRunner());
    assertTrue(interpreterProcess.getEnv().size() >= 3);
    assertEquals(sparkHome, interpreterProcess.getEnv().get("SPARK_HOME"));

    assertEquals("true", interpreterProcess.getEnv().get("ZEPPELIN_SPARK_YARN_CLUSTER"));
    String sparkJars = "jar_1," +
            zeppelinHome + "/interpreter/spark/scala-2.11/spark-scala-2.11-" + Util.getVersion() + ".jar," +
            zeppelinHome + "/interpreter/zeppelin-interpreter-shaded-" + Util.getVersion() + ".jar";
    String sparkrZip = sparkHome + "/R/lib/sparkr.zip#sparkr";
    String sparkFiles = "file_1," + zeppelinHome + "/conf/log4j_yarn_cluster.properties";
    assertEquals("--conf|spark.yarn.dist.archives=" + sparkrZip +
                    "|--conf|spark.yarn.maxAppAttempts=1" +
                    "|--conf|spark.files=" + sparkFiles +
                    "|--conf|spark.jars=" + sparkJars +
                    "|--conf|spark.yarn.isPython=true" +
                    "|--conf|spark.yarn.submit.waitAppCompletion=false" +
                    "|--conf|spark.app.name=intpGroupId" +
                    "|--conf|spark.master=yarn-cluster",
            interpreterProcess.getEnv().get("ZEPPELIN_SPARK_CONF"));
  }

  @Ignore("This test is broken: java.lang.RuntimeException: No such note: note1. This test will also most likely fail due to sparkJars being modified from dependency resolver")
  @Test
  public void testYarnClusterMode_2() throws IOException {
    ZeppelinConfiguration zConf = ZeppelinConfiguration.create();
    SparkInterpreterLauncher launcher = new SparkInterpreterLauncher(zConf, null);
    Properties properties = new Properties();
    properties.setProperty("SPARK_HOME", sparkHome);
    properties.setProperty("property_1", "value_1");
    properties.setProperty("spark.master", "yarn");
    properties.setProperty("spark.submit.deployMode", "cluster");
    properties.setProperty("spark.files", "file_1");
    properties.setProperty("spark.jars", "jar_1");

    InterpreterOption option = new InterpreterOption();
    option.setUserImpersonate(true);
    InterpreterLaunchContext context = new InterpreterLaunchContext(properties, option, null, "user1", "intpGroupId", "groupId", "spark", "spark", 0, "host");

    InterpreterClient client = launcher.launch(context);
    assertTrue(client instanceof ExecRemoteInterpreterProcess);
    ExecRemoteInterpreterProcess interpreterProcess = (ExecRemoteInterpreterProcess) client;
    assertEquals("spark", interpreterProcess.getInterpreterSettingName());
    assertTrue(interpreterProcess.getInterpreterDir().endsWith("/interpreter/spark"));
    assertEquals(zConf.getInterpreterRemoteRunnerPath(), interpreterProcess.getInterpreterRunner());
    assertTrue(interpreterProcess.getEnv().size() >= 3);
    assertEquals(sparkHome, interpreterProcess.getEnv().get("SPARK_HOME"));
    assertEquals("true", interpreterProcess.getEnv().get("ZEPPELIN_SPARK_YARN_CLUSTER"));
    String sparkJars = "jar_1," +
            zeppelinHome + "/interpreter/spark/scala-2.11/spark-scala-2.11-" + Util.getVersion() + ".jar," +
            zeppelinHome + "/interpreter/zeppelin-interpreter-shaded-" + Util.getVersion() + ".jar";
    String sparkrZip = sparkHome + "/R/lib/sparkr.zip#sparkr";
    String sparkFiles = "file_1," + zeppelinHome + "/conf/log4j_yarn_cluster.properties";
    assertEquals("--proxy-user|user1|--conf|spark.yarn.dist.archives=" + sparkrZip +
            "|--conf|spark.yarn.isPython=true|--conf|spark.app.name=intpGroupId" +
            "|--conf|spark.yarn.maxAppAttempts=1" +
            "|--conf|spark.master=yarn" +
            "|--conf|spark.files=" + sparkFiles + "|--conf|spark.jars=" + sparkJars +
            "|--conf|spark.submit.deployMode=cluster" +
            "|--conf|spark.yarn.submit.waitAppCompletion=false",
            interpreterProcess.getEnv().get("ZEPPELIN_SPARK_CONF"));
  }

  @Ignore("This test is broken: java.lang.RuntimeException: No such note: note1")
  @Test
  public void testYarnClusterMode_3() throws IOException {
    ZeppelinConfiguration zConf = ZeppelinConfiguration.create();
    SparkInterpreterLauncher launcher = new SparkInterpreterLauncher(zConf, null);
    Properties properties = new Properties();
    properties.setProperty("SPARK_HOME", sparkHome);
    properties.setProperty("property_1", "value_1");
    properties.setProperty("spark.master", "yarn");
    properties.setProperty("spark.submit.deployMode", "cluster");
    properties.setProperty("spark.files", "{}");
    properties.setProperty("spark.jars", "jar_1");

    InterpreterOption option = new InterpreterOption();
    option.setUserImpersonate(true);
    InterpreterLaunchContext context = new InterpreterLaunchContext(properties, option, null, "user1", "intpGroupId", "groupId", "spark", "spark", 0, "host");

    InterpreterClient client = launcher.launch(context);
    assertTrue(client instanceof ExecRemoteInterpreterProcess);
    ExecRemoteInterpreterProcess interpreterProcess = (ExecRemoteInterpreterProcess) client;
    assertEquals("spark", interpreterProcess.getInterpreterSettingName());
    assertTrue(interpreterProcess.getInterpreterDir().endsWith("/interpreter/spark"));
    assertEquals(zConf.getInterpreterRemoteRunnerPath(), interpreterProcess.getInterpreterRunner());
    assertTrue(interpreterProcess.getEnv().size() >= 3);
    assertEquals(sparkHome, interpreterProcess.getEnv().get("SPARK_HOME"));
    assertEquals("true", interpreterProcess.getEnv().get("ZEPPELIN_SPARK_YARN_CLUSTER"));

    String sparkJars = "jar_1," +
            zeppelinHome + "/interpreter/spark/scala-2.11/spark-scala-2.11-" + Util.getVersion() + ".jar," +
            zeppelinHome + "/interpreter/zeppelin-interpreter-shaded-" + Util.getVersion() + ".jar";
    String sparkrZip = sparkHome + "/R/lib/sparkr.zip#sparkr";
    // escape special characters
    String sparkFiles = "{}," + zeppelinHome + "/conf/log4j_yarn_cluster.properties";
    assertEquals("--proxy-user|user1" +
                    "|--conf|spark.yarn.dist.archives=" + sparkrZip +
                    "|--conf|spark.yarn.isPython=true" +
                    "|--conf|spark.app.name=intpGroupId" +
                    "|--conf|spark.yarn.maxAppAttempts=1" +
                    "|--conf|spark.master=yarn" +
                    "|--conf|spark.files=" + sparkFiles +
                    "|--conf|spark.jars=" + sparkJars +
                    "|--conf|spark.submit.deployMode=cluster" +
                    "|--conf|spark.yarn.submit.waitAppCompletion=false",
            interpreterProcess.getEnv().get("ZEPPELIN_SPARK_CONF"));
  }
}
