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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.teragrep.zep_01.util.ExecutorUtil;

/**
 * FIFOScheduler runs submitted job sequentially
 */
public class FIFOScheduler extends AbstractScheduler {

  private ExecutorService executor;

  FIFOScheduler(String name) {
    super(name);
    this.executor = Executors.newSingleThreadExecutor(
        new SchedulerThreadFactory("FIFOScheduler-" + name + "-Worker-"));
  }

  @Override
  public void runJobInScheduler(final Job job) {
    // run job in the SingleThreadExecutor since this is FIFO.
    executor.execute(() -> runJob(job));
  }

  @Override
  public void stop() {
    stop(2, TimeUnit.MINUTES);
  }

  @Override
  public void stop(int stopTimeoutVal, TimeUnit stopTimeoutUnit) {
    super.stop();
    ExecutorUtil.softShutdown(name, executor, stopTimeoutVal, stopTimeoutUnit);
  }
}
