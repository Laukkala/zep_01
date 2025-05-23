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

package com.teragrep.zep_01.interpreter.recovery;

import com.teragrep.zep_01.conf.ZeppelinConfiguration;
import com.teragrep.zep_01.interpreter.launcher.InterpreterClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * Interface for storing interpreter process recovery metadata.
 * Just store mapping between interpreterGroupId to interpreter process host:ip
 *
 */
public abstract class RecoveryStorage {

  protected ZeppelinConfiguration zConf;
  protected Map<String, InterpreterClient> restoredClients;

  // TODO(zjffdu) The constructor is inconsistent between base class and its implementation.
  //  The implementation actually use InterpreterSettingManager, the interface should also use it.
  public RecoveryStorage(ZeppelinConfiguration zConf) {
    this.zConf = zConf;
  }

  /**
   * Update RecoveryStorage when new InterpreterClient is started
   *
   * @param client
   * @throws IOException
   */
  public abstract void onInterpreterClientStart(InterpreterClient client) throws IOException;

  /**
   * Update RecoveryStorage when InterpreterClient is stopped
   *
   * @param client
   * @throws IOException
   */
  public abstract void onInterpreterClientStop(InterpreterClient client) throws IOException;

  /**
   *
   * It is only called when Zeppelin Server is started.
   *
   * @return Map between interpreterGroupId to InterpreterClient
   * @throws IOException
   */
  public abstract Map<String, InterpreterClient> restore() throws IOException;


  /**
   * It is called after constructor
   *
   * @throws IOException
   */
  public void init() throws IOException {
    Map<String, InterpreterClient> restoredClientsInStorage= restore();
    this.restoredClients = new HashMap<String, InterpreterClient>();
    for (Map.Entry<String, InterpreterClient> entry : restoredClientsInStorage.entrySet()) {
      if (entry.getValue().recover()) {
        this.restoredClients.put(entry.getKey(), entry.getValue());
      } else {
        onInterpreterClientStop(entry.getValue());
      }
    }
  }

  /**
   * Get InterpreterClient that is associated with this interpreterGroupId, return null when there's
   * no such InterpreterClient.
   *
   * @param interpreterGroupId
   * @return InterpreterClient
   */
  public InterpreterClient getInterpreterClient(String interpreterGroupId) {
    if (restoredClients.containsKey(interpreterGroupId)) {
      return restoredClients.get(interpreterGroupId);
    } else {
      return null;
    }
  }

  public void removeInterpreterClient(String interpreterGroupId) {
    this.restoredClients.remove(interpreterGroupId);
  }
}
