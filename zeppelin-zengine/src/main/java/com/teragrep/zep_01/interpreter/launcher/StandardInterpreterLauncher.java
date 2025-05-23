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

import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.commons.lang3.StringUtils;
import com.teragrep.zep_01.conf.ZeppelinConfiguration;
import com.teragrep.zep_01.interpreter.InterpreterOption;
import com.teragrep.zep_01.interpreter.InterpreterRunner;
import com.teragrep.zep_01.interpreter.recovery.RecoveryStorage;
import com.teragrep.zep_01.interpreter.remote.ExecRemoteInterpreterProcess;
import com.teragrep.zep_01.interpreter.remote.RemoteInterpreterRunningProcess;
import com.teragrep.zep_01.interpreter.remote.RemoteInterpreterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Interpreter Launcher which use shell script to launch the interpreter process.
 */
public class StandardInterpreterLauncher extends InterpreterLauncher {

  private static final Logger LOGGER = LoggerFactory.getLogger(StandardInterpreterLauncher.class);

  public StandardInterpreterLauncher(ZeppelinConfiguration zConf, RecoveryStorage recoveryStorage) {
    super(zConf, recoveryStorage);
  }

  @Override
  public InterpreterClient launchDirectly(InterpreterLaunchContext context) throws IOException {
    LOGGER.info("Launching new interpreter process of {}", context.getInterpreterSettingGroup());
    this.properties = context.getProperties();
    InterpreterOption option = context.getOption();
    InterpreterRunner runner = context.getRunner();
    String groupName = context.getInterpreterSettingGroup();
    String name = context.getInterpreterSettingName();
    int connectTimeout = getConnectTimeout();
    int connectionPoolSize = getConnectPoolSize();

    if (option.isExistingProcess()) {
      return new RemoteInterpreterRunningProcess(
          context.getInterpreterSettingName(),
          context.getInterpreterGroupId(),
          connectTimeout,
          connectionPoolSize,
          context.getIntpEventServerHost(),
          context.getIntpEventServerPort(),
          option.getHost(),
          option.getPort(),
          false);
    } else {
      // create new remote process
      return new ExecRemoteInterpreterProcess(
          context.getIntpEventServerPort(), context.getIntpEventServerHost(), zConf.getInterpreterPortRange(),
          zConf.getInterpreterDir() + "/" + groupName,
          buildEnvFromProperties(context), connectTimeout, connectionPoolSize, name,
          context.getInterpreterGroupId(), option.isUserImpersonate(),
          runner != null ? runner.getPath() : zConf.getInterpreterRemoteRunnerPath());
    }
  }

  public Map<String, String> buildEnvFromProperties(InterpreterLaunchContext context) throws IOException {
    Map<String, String> env = EnvironmentUtils.getProcEnvironment();
    for (Map.Entry<Object,Object> entry : context.getProperties().entrySet()) {
      String key = (String) entry.getKey();
      String value = (String) entry.getValue();
      if (RemoteInterpreterUtils.isEnvString(key) && !StringUtils.isBlank(value)) {
        env.put(key, value);
      }
    }
    env.put("INTERPRETER_GROUP_ID", context.getInterpreterGroupId());
    return env;
  }
}
