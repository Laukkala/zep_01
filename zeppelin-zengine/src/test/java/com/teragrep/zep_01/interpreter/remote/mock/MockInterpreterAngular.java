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

package com.teragrep.zep_01.interpreter.remote.mock;

import com.teragrep.zep_01.display.AngularObjectRegistry;
import com.teragrep.zep_01.display.AngularObjectWatcher;
import com.teragrep.zep_01.interpreter.Interpreter;
import com.teragrep.zep_01.interpreter.InterpreterContext;
import com.teragrep.zep_01.interpreter.InterpreterResult;
import com.teragrep.zep_01.interpreter.InterpreterResult.Code;
import com.teragrep.zep_01.interpreter.thrift.InterpreterCompletion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.fail;

public class MockInterpreterAngular extends Interpreter {

  private static final Logger LOGGER = LoggerFactory.getLogger(MockInterpreterAngular.class);

  AtomicInteger numWatch = new AtomicInteger(0);

  public MockInterpreterAngular(Properties property) {
    super(property);
  }

  @Override
  public void open() {
  }

  @Override
  public void close() {

  }

  @Override
  public InterpreterResult interpret(String st, InterpreterContext context) {
    String[] stmt = st.split(" ");
    String cmd = stmt[0];
    String name = null;
    if (stmt.length >= 2) {
      name = stmt[1];
    }
    String value = null;
    if (stmt.length == 3) {
      value = stmt[2];
    }

    AngularObjectRegistry registry = context.getAngularObjectRegistry();

    if (cmd.equals("add")) {
      registry.add(name, value, context.getNoteId(), null);
      registry.get(name, context.getNoteId(), null).addWatcher(new AngularObjectWatcher
              (null) {

        @Override
        public void watch(Object oldObject, Object newObject,
            InterpreterContext context) {
          numWatch.incrementAndGet();
        }

      });
    } else if (cmd.equalsIgnoreCase("update")) {
      registry.get(name, context.getNoteId(), null).set(value);
    } else if (cmd.equals("remove")) {
      registry.remove(name, context.getNoteId(), null);
    }

    try {
      Thread.sleep(500); // wait for watcher executed
    } catch (InterruptedException e) {
      fail("Failure: " + e.getMessage());
    }

    String msg = registry.getAll(context.getNoteId(), null).size() + " " + Integer.toString(numWatch
            .get());
    return new InterpreterResult(Code.SUCCESS, msg);
  }

  @Override
  public void cancel(InterpreterContext context) {
  }

  @Override
  public FormType getFormType() {
    return FormType.NATIVE;
  }

  @Override
  public int getProgress(InterpreterContext context) {
    return 0;
  }

  @Override
  public List<InterpreterCompletion> completion(String buf, int cursor,
      InterpreterContext interpreterContext) {
    return null;
  }
}
