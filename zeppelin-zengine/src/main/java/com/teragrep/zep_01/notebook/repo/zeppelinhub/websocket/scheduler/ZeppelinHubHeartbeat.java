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
package com.teragrep.zep_01.notebook.repo.zeppelinhub.websocket.scheduler;

import com.teragrep.zep_01.notebook.repo.zeppelinhub.model.UserTokenContainer;
import com.teragrep.zep_01.notebook.repo.zeppelinhub.websocket.ZeppelinhubClient;
import com.teragrep.zep_01.notebook.repo.zeppelinhub.websocket.utils.ZeppelinhubUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Routine that send PING event to zeppelinhub.
 *
 */
public class ZeppelinHubHeartbeat implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(ZeppelinHubHeartbeat.class);
  private ZeppelinhubClient client;
  
  public static ZeppelinHubHeartbeat newInstance(ZeppelinhubClient client) {
    return new ZeppelinHubHeartbeat(client);
  }
  
  private ZeppelinHubHeartbeat(ZeppelinhubClient client) {
    this.client = client;
  }
  
  @Override
  public void run() {
    LOGGER.debug("Sending PING to zeppelinhub token");
    for (String token: UserTokenContainer.getInstance().getAllTokens()) {
      client.send(ZeppelinhubUtils.pingMessage(token), token);
    }
  }  
}
