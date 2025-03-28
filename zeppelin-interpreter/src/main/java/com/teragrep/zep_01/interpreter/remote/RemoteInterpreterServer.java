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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;
import com.teragrep.zep_01.conf.ZeppelinConfiguration;
import com.teragrep.zep_01.display.AngularObject;
import com.teragrep.zep_01.display.AngularObjectRegistry;
import com.teragrep.zep_01.display.GUI;
import com.teragrep.zep_01.interpreter.Constants;
import com.teragrep.zep_01.interpreter.Interpreter;
import com.teragrep.zep_01.interpreter.InterpreterContext;
import com.teragrep.zep_01.interpreter.InterpreterException;
import com.teragrep.zep_01.interpreter.InterpreterGroup;
import com.teragrep.zep_01.interpreter.InterpreterHookListener;
import com.teragrep.zep_01.interpreter.InterpreterHookRegistry;
import com.teragrep.zep_01.interpreter.InterpreterHookRegistry.HookType;
import com.teragrep.zep_01.interpreter.InterpreterOutput;
import com.teragrep.zep_01.interpreter.InterpreterOutputListener;
import com.teragrep.zep_01.interpreter.InterpreterResult;
import com.teragrep.zep_01.interpreter.InterpreterResult.Code;
import com.teragrep.zep_01.interpreter.InterpreterResultMessage;
import com.teragrep.zep_01.interpreter.InterpreterResultMessageOutput;
import com.teragrep.zep_01.interpreter.LazyOpenInterpreter;
import com.teragrep.zep_01.interpreter.LifecycleManager;
import com.teragrep.zep_01.interpreter.thrift.InterpreterCompletion;
import com.teragrep.zep_01.interpreter.thrift.InterpreterRPCException;
import com.teragrep.zep_01.interpreter.thrift.RegisterInfo;
import com.teragrep.zep_01.interpreter.thrift.RemoteApplicationResult;
import com.teragrep.zep_01.interpreter.thrift.RemoteInterpreterContext;
import com.teragrep.zep_01.interpreter.thrift.RemoteInterpreterResult;
import com.teragrep.zep_01.interpreter.thrift.RemoteInterpreterResultMessage;
import com.teragrep.zep_01.interpreter.thrift.RemoteInterpreterService;
import com.teragrep.zep_01.resource.DistributedResourcePool;
import com.teragrep.zep_01.resource.Resource;
import com.teragrep.zep_01.resource.ResourcePool;
import com.teragrep.zep_01.resource.ResourceSet;
import com.teragrep.zep_01.scheduler.ExecutorFactory;
import com.teragrep.zep_01.scheduler.Job;
import com.teragrep.zep_01.scheduler.Job.Status;
import com.teragrep.zep_01.scheduler.JobListener;
import com.teragrep.zep_01.scheduler.Scheduler;
import com.teragrep.zep_01.scheduler.SchedulerFactory;
import com.teragrep.zep_01.user.AuthenticationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Entry point for Interpreter process.
 * Accepting thrift connections from ZeppelinServer.
 */
public class RemoteInterpreterServer extends Thread
    implements RemoteInterpreterService.Iface {

  private static final Logger LOGGER = LoggerFactory.getLogger(RemoteInterpreterServer.class);

  public static final int DEFAULT_SHUTDOWN_TIMEOUT = 2000;

  private String interpreterGroupId;
  private InterpreterGroup interpreterGroup;
  private AngularObjectRegistry angularObjectRegistry;
  private InterpreterHookRegistry hookRegistry;
  private DistributedResourcePool resourcePool;
  private Gson gson = new Gson();
  private String launcherEnv = System.getenv("ZEPPELIN_INTERPRETER_LAUNCHER");

  private String intpEventServerHost;
  private int intpEventServerPort;
  private String host;
  private int port;
  private TThreadPoolServer server;
  RemoteInterpreterEventClient intpEventClient;
  private LifecycleManager lifecycleManager;


  // Hold information for manual progress update
  private ConcurrentMap<String, Integer> progressMap = new ConcurrentHashMap<>();

  // keep track of the running jobs for job recovery.
  private ConcurrentMap<String, InterpretJob> runningJobs = new ConcurrentHashMap<>();
  // cache result threshold, result cache is for purpose of recover paragraph even after
  // paragraph is finished
  private int resultCacheInSeconds;
  private ScheduledExecutorService resultCleanService = Executors.newSingleThreadScheduledExecutor();

  private boolean isTest;
  // Whether calling System.exit to force shutdown interpreter process.
  // In Flink K8s application mode, RemoteInterpreterServer#main is called via reflection by flink framework.
  // We should not call System.exit in this scenario when RemoteInterpreterServer is stopped,
  // Otherwise flink will think flink job is exited abnormally and will try to restart this
  // pod (RemoteInterpreterServer)
  private boolean isForceShutdown = true;

  private ZeppelinConfiguration zConf;

  private static Thread shutdownThread;

  public RemoteInterpreterServer(String intpEventServerHost,
                                 int intpEventServerPort,
                                 String interpreterGroupId,
                                 String portRange) throws Exception {
    this(intpEventServerHost, intpEventServerPort, portRange, interpreterGroupId, false);
  }

  public RemoteInterpreterServer(String intpEventServerHost,
                                 int intpEventServerPort,
                                 String portRange,
                                 String interpreterGroupId,
                                 boolean isTest) throws Exception {
    super("RemoteInterpreterServer-Thread");
    if (null != intpEventServerHost) {
      this.intpEventServerHost = intpEventServerHost;
      this.intpEventServerPort = intpEventServerPort;
      this.port = RemoteInterpreterUtils.findAvailablePort(portRange);
      this.host = RemoteInterpreterUtils.findAvailableHostAddress();
    } else {
      // DevInterpreter
      this.port = intpEventServerPort;
    }
    this.isTest = isTest;
    this.interpreterGroupId = interpreterGroupId;
  }

  @Override
  public void run() {
    RemoteInterpreterService.Processor<RemoteInterpreterServer> processor =
      new RemoteInterpreterService.Processor<>(this);
    try (TServerSocket tSocket = new TServerSocket(port)){
      server = new TThreadPoolServer(
      new TThreadPoolServer.Args(tSocket)
        .stopTimeoutVal(DEFAULT_SHUTDOWN_TIMEOUT)
        .stopTimeoutUnit(TimeUnit.MILLISECONDS)
        .processor(processor));

      if (null != intpEventServerHost && !isTest) {
        Thread registerThread = new Thread(new RegisterRunnable());
        registerThread.setName("RegisterThread");
        registerThread.start();
      }
      LOGGER.info("Launching ThriftServer at {}:{}", this.host, this.port);
      server.serve();
    } catch (TTransportException e) {
      LOGGER.error("Failure in TTransport", e);
    }
    LOGGER.info("RemoteInterpreterServer-Thread finished");
  }

  @Override
  public void init(Map<String, String> properties) throws InterpreterRPCException, TException {
    this.zConf = ZeppelinConfiguration.create();
    for (Map.Entry<String, String> entry : properties.entrySet()) {
      this.zConf.setProperty(entry.getKey(), entry.getValue());
    }

    try {
      lifecycleManager = createLifecycleManager();
      lifecycleManager.onInterpreterProcessStarted(interpreterGroupId);
    } catch (Exception e) {
      throw new InterpreterRPCException("Fail to create LifecycleManager, cause: " + e.toString());
    }

    if (!isTest) {
      int connectionPoolSize =
              this.zConf.getInt(ZeppelinConfiguration.ConfVars.ZEPPELIN_INTERPRETER_CONNECTION_POOL_SIZE);
      LOGGER.info("Creating RemoteInterpreterEventClient with connection pool size: {}",
              connectionPoolSize);
      intpEventClient = new RemoteInterpreterEventClient(intpEventServerHost, intpEventServerPort,
              connectionPoolSize);
    }
  }

  @Override
  public void shutdown() throws InterpreterRPCException, TException {
    // unRegisterInterpreterProcess should be a sync operation (outside of shutdown thread),
    // otherwise it would cause data mismatch between zeppelin server & interpreter process.
    // e.g. zeppelin server start a new interpreter process, while previous interpreter process
    // uniregister it with the same interpreter group id: flink-shared-process.
    if (intpEventClient != null) {
      try {
        LOGGER.info("Unregister interpreter process");
        intpEventClient.unRegisterInterpreterProcess();
      } catch (Exception e) {
        LOGGER.error("Fail to unregister remote interpreter process", e);
      }
    }
    if (shutdownThread != null) {
      // no need to call shutdownhook twice
      if (Runtime.getRuntime().removeShutdownHook(shutdownThread)) {
        LOGGER.debug("ShutdownHook removed, because of a regular shutdown");
      } else {
        LOGGER.warn("The ShutdownHook could not be removed");
      }
    }

    Thread shutDownThread = new ShutdownThread(ShutdownThread.CAUSE_SHUTDOWN_CALL);
    shutDownThread.start();
  }

  public ZeppelinConfiguration getConf() {
    return this.zConf;
  }

  public LifecycleManager getLifecycleManager() {
    return this.lifecycleManager;
  }

  public int getPort() {
    return port;
  }

  public boolean isRunning() {
    if (server == null) {
      return false;
    } else {
      return server.isServing();
    }
  }

  private LifecycleManager createLifecycleManager() throws Exception {
    String lifecycleManagerClass = zConf.getLifecycleManagerClass();
    Class<?> clazz = Class.forName(lifecycleManagerClass);
    LOGGER.info("Creating interpreter lifecycle manager: {}", lifecycleManagerClass);
    return (LifecycleManager) clazz.getConstructor(ZeppelinConfiguration.class, RemoteInterpreterServer.class)
            .newInstance(zConf, this);
  }

  public static void main(String[] args) throws Exception {
    String zeppelinServerHost = null;
    int port = Constants.ZEPPELIN_INTERPRETER_DEFAUlT_PORT;
    String portRange = ":";
    String interpreterGroupId = null;
    if (args.length > 0) {
      zeppelinServerHost = args[0];
      port = Integer.parseInt(args[1]);
      interpreterGroupId = args[2];
      if (args.length > 3) {
        portRange = args[3];
      }
    }
    RemoteInterpreterServer remoteInterpreterServer =
        new RemoteInterpreterServer(zeppelinServerHost, port, interpreterGroupId, portRange);
    remoteInterpreterServer.start();

    /*
     * Registration of a ShutdownHook in case of an unpredictable system call
     * Examples: STRG+C, SIGTERM via kill
     */
    shutdownThread = remoteInterpreterServer.new ShutdownThread(ShutdownThread.CAUSE_SHUTDOWN_HOOK);
    Runtime.getRuntime().addShutdownHook(shutdownThread);

    remoteInterpreterServer.join();
    LOGGER.info("RemoteInterpreterServer thread is finished");

    /* TODO(pdallig): Remove System.exit(0) if the thrift server can be shut down successfully.
     * https://github.com/apache/thrift/commit/9cb1c794cd39cfb276771f8e52f0306eb8d462fd
     * should be part of the next release and solve the problem.
     * We may have other threads that are not terminated successfully.
     */
    if (remoteInterpreterServer.isForceShutdown) {
      LOGGER.info("Force shutting down");
      System.exit(0);
    }
  }

  @Override
  public void createInterpreter(String interpreterGroupId, String sessionId, String
      className, Map<String, String> properties, String userName) throws InterpreterRPCException, TException {
    try {
      if (interpreterGroup == null) {
        interpreterGroup = new InterpreterGroup(interpreterGroupId);
        angularObjectRegistry = new AngularObjectRegistry(interpreterGroup.getId(), intpEventClient);
        hookRegistry = new InterpreterHookRegistry();
        resourcePool = new DistributedResourcePool(interpreterGroup.getId(), intpEventClient);
        interpreterGroup.setInterpreterHookRegistry(hookRegistry);
        interpreterGroup.setAngularObjectRegistry(angularObjectRegistry);
        interpreterGroup.setResourcePool(resourcePool);
        intpEventClient.setIntpGroupId(interpreterGroupId);

        String localRepoPath = properties.get("zeppelin.interpreter.localRepo");
        if (properties.containsKey("zeppelin.interpreter.output.limit")) {
          InterpreterOutput.LIMIT = Integer.parseInt(
                  properties.get("zeppelin.interpreter.output.limit"));
        }

        resultCacheInSeconds =
                Integer.parseInt(properties.getOrDefault("zeppelin.interpreter.result.cache", "0"));
      }

      Class<Interpreter> replClass = (Class<Interpreter>) Object.class.forName(className);
      Properties p = new Properties();
      p.putAll(properties);
      setSystemProperty(p);

      Constructor<Interpreter> constructor =
              replClass.getConstructor(new Class[]{Properties.class});
      Interpreter interpreter = constructor.newInstance(p);
      interpreter.setClassloaderUrls(new URL[]{});

      interpreter.setInterpreterGroup(interpreterGroup);
      interpreter.setUserName(userName);

      interpreterGroup.addInterpreterToSession(new LazyOpenInterpreter(interpreter), sessionId);

      this.isForceShutdown = Boolean.parseBoolean(properties.getOrDefault("zeppelin.interpreter.forceShutdown", "true"));
      LOGGER.info("Instantiate interpreter {}, isForceShutdown: {}", className, isForceShutdown);
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      throw new InterpreterRPCException("Fail to create interpreter, cause: " + e.toString());
    }
  }

  protected InterpreterGroup getInterpreterGroup() {
    return interpreterGroup;
  }

  protected ResourcePool getResourcePool() {
    return resourcePool;
  }

  protected RemoteInterpreterEventClient getIntpEventClient() {
    return intpEventClient;
  }

  private void setSystemProperty(Properties properties) {
    for (Object key : properties.keySet()) {
      if (!RemoteInterpreterUtils.isEnvString((String) key)) {
        String value = properties.getProperty((String) key);
        if (!StringUtils.isBlank(value)) {
          System.setProperty((String) key, properties.getProperty((String) key));
        }
      }
    }
  }

  protected Interpreter getInterpreter(String sessionId, String className)
          throws InterpreterRPCException, TException {
    if (interpreterGroup == null) {
      throw new InterpreterRPCException("Interpreter instance " + className + " not created");
    }
    synchronized (interpreterGroup) {
      List<Interpreter> interpreters = interpreterGroup.get(sessionId);
      if (interpreters == null) {
        throw new InterpreterRPCException("Interpreter " + className + " not initialized");
      }
      for (Interpreter inp : interpreters) {
        if (inp.getClassName().equals(className)) {
          return inp;
        }
      }
    }
    throw new InterpreterRPCException("Interpreter instance " + className + " not found");
  }

  @Override
  public void open(String sessionId, String className) throws InterpreterRPCException, TException {
    LOGGER.info("Open Interpreter {} for session {}", className, sessionId);
    Interpreter intp = getInterpreter(sessionId, className);
    try {
      intp.open();
    } catch (InterpreterException e) {
      throw new InterpreterRPCException(e.toString());
    }
  }

  @Override
  public void close(String sessionId, String className) throws InterpreterRPCException, TException {
    // close interpreters
    if (interpreterGroup != null) {
      synchronized (interpreterGroup) {
        List<Interpreter> interpreters = interpreterGroup.get(sessionId);
        if (interpreters != null) {
          Iterator<Interpreter> it = interpreters.iterator();
          while (it.hasNext()) {
            Interpreter inp = it.next();
            if (inp.getClassName().equals(className)) {
              try {
                inp.close();
              } catch (InterpreterException e) {
                LOGGER.warn("Fail to close interpreter", e);
              }
              it.remove();
              break;
            }
          }
        }
      }
    }
  }

  @Override
  public void reconnect(String host, int port) throws InterpreterRPCException, TException {
    try {
      LOGGER.info("Reconnect to this interpreter process from {}:{}", host, port);
      this.intpEventServerHost = host;
      this.intpEventServerPort = port;
      intpEventClient = new RemoteInterpreterEventClient(intpEventServerHost, intpEventServerPort,
              this.zConf.getInt(ZeppelinConfiguration.ConfVars.ZEPPELIN_INTERPRETER_CONNECTION_POOL_SIZE));
      intpEventClient.setIntpGroupId(interpreterGroupId);

      this.angularObjectRegistry = new AngularObjectRegistry(interpreterGroup.getId(), intpEventClient);
      this.resourcePool = new DistributedResourcePool(interpreterGroup.getId(), intpEventClient);

      // reset all the available InterpreterContext's components that use intpEventClient.
      for (InterpreterContext context : InterpreterContext.getAllContexts().values()) {
        context.setIntpEventClient(intpEventClient);
        context.setAngularObjectRegistry(angularObjectRegistry);
        context.setResourcePool(resourcePool);
      }
    } catch (Exception e) {
      throw new InterpreterRPCException(e.toString());
    }
  }

  @Override
  public RemoteInterpreterResult interpret(String sessionId,
                                           String className,
                                           String st,
                                           RemoteInterpreterContext interpreterContext)
          throws InterpreterRPCException, TException {
    try {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("st:\n{}", st);
      }
      lifecycleManager.onInterpreterUse(interpreterGroupId);

      Interpreter intp = getInterpreter(sessionId, className);
      InterpreterContext context = convert(interpreterContext);
      context.setInterpreterClassName(intp.getClassName());

      InterpretJob interpretJob = null;
      boolean isRecover = Boolean.parseBoolean(
              context.getLocalProperties().getOrDefault("isRecover", "false"));
      if (isRecover) {
        LOGGER.info("Recovering paragraph: {} of note: {}",
                context.getParagraphId(), context.getNoteId());
        interpretJob = runningJobs.get(context.getParagraphId());
        if (interpretJob == null) {
          InterpreterResult result = new InterpreterResult(Code.ERROR, "Job is finished, unable to recover it");
          return convert(result,
                  context.getConfig(),
                  context.getGui(),
                  context.getNoteGui());
        }
      } else {
        Scheduler scheduler = intp.getScheduler();
        InterpretJobListener jobListener = new InterpretJobListener();
        interpretJob = new InterpretJob(
                context.getParagraphId(),
                "RemoteInterpretJob_" + System.currentTimeMillis(),
                jobListener,
                intp,
                st,
                context);
        runningJobs.put(context.getParagraphId(), interpretJob);
        scheduler.submit(interpretJob);
      }

      while (!interpretJob.isTerminated()) {
        JobListener jobListener = interpretJob.getListener();
        synchronized (jobListener) {
          try {
            jobListener.wait(1000);
          } catch (InterruptedException e) {
            LOGGER.info("Exception in RemoteInterpreterServer while interpret, jobListener.wait", e);
          }
        }
      }

      progressMap.remove(context.getParagraphId());
      resultCleanService.schedule(() -> {
        runningJobs.remove(context.getParagraphId());
      }, resultCacheInSeconds, TimeUnit.SECONDS);

      InterpreterResult result = interpretJob.getReturn();
      // in case of job abort in PENDING status, result can be null
      if (result == null) {
        result = new InterpreterResult(Code.KEEP_PREVIOUS_RESULT);
      }
      return convert(result,
              context.getConfig(),
              context.getGui(),
              context.getNoteGui());
    } catch (Exception e) {
      LOGGER.error("Internal error when interpret code", e);
      throw new InterpreterRPCException(e.toString());
    }
  }

  class RegisterRunnable implements Runnable {

    @Override
    public void run() {
      LOGGER.info("Start registration");
      // wait till the server is serving
      while (!Thread.currentThread().isInterrupted() && server != null && !server.isServing()) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          LOGGER.info("InterruptedException received", e);
          Thread.currentThread().interrupt();
        }
      }
      if (!Thread.currentThread().isInterrupted()) {
        RegisterInfo registerInfo = new RegisterInfo(host, port, interpreterGroupId);
        try {
          intpEventClient = new RemoteInterpreterEventClient(intpEventServerHost, intpEventServerPort, 10);
          LOGGER.info("Registering interpreter process");
          intpEventClient.registerInterpreterProcess(registerInfo);
          LOGGER.info("Registered interpreter process");
        } catch (Exception e) {
          LOGGER.error("Error while registering interpreter: {}, cause: {}", registerInfo, e);
          try {
            shutdown();
          } catch (Exception e1) {
            LOGGER.warn("Exception occurs while shutting down", e1);
          }
        }
      }

      if (launcherEnv != null && "yarn".endsWith(launcherEnv)) {
        try {
          YarnUtils.register(host, port);
          ScheduledExecutorService yarnHeartbeat = ExecutorFactory.singleton()
            .createOrGetScheduled("RM-Heartbeat", 1);
          yarnHeartbeat.scheduleAtFixedRate(YarnUtils::heartbeat, 0, 1, TimeUnit.MINUTES);
        } catch (Exception e) {
          LOGGER.error("Fail to register yarn app", e);
        }
      }
      LOGGER.info("Registration finished");
    }
  }

  class ShutdownThread extends Thread {
    private final String cause;

    public static final String CAUSE_SHUTDOWN_HOOK = "ShutdownHook";
    public static final String CAUSE_SHUTDOWN_CALL = "ShutdownCall";

    public ShutdownThread(String cause) {
      super("ShutdownThread");
      this.cause = cause;
    }

    @Override
    public void run() {
      LOGGER.info("Shutting down...");
      LOGGER.info("Shutdown initialized by {}", cause);

      if (interpreterGroup != null) {
        synchronized (interpreterGroup) {
          for (List<Interpreter> session : interpreterGroup.values()) {
            for (Interpreter interpreter : session) {
              try {
                interpreter.close();
              } catch (InterpreterException e) {
                LOGGER.warn("Fail to close interpreter", e);
              }
            }
          }
        }
      }
      if (!isTest) {
        SchedulerFactory.singleton().destroy();
        ExecutorFactory.singleton().shutdownAll();
      }

      if ("yarn".equals(launcherEnv)) {
        try {
          YarnUtils.unregister(true, "");
        } catch (Exception e) {
          LOGGER.error("Fail to unregister yarn app", e);
        }
      }
      // Try to unregister the interpreter process in case the interpreter process exit unpredictable via ShutdownHook
      if (intpEventClient != null && CAUSE_SHUTDOWN_HOOK.equals(cause)) {
        try {
          LOGGER.info("Unregister interpreter process");
          intpEventClient.unRegisterInterpreterProcess();
        } catch (Exception e) {
          LOGGER.error("Fail to unregister remote interpreter process", e);
        }
      }

      server.stop();

      // server.stop() does not always finish server.serve() loop
      // sometimes server.serve() is hanging even after server.stop() call.
      // this case, need to force kill the process

      long startTime = System.currentTimeMillis();
      while (System.currentTimeMillis() - startTime < (DEFAULT_SHUTDOWN_TIMEOUT + 100) &&
              server.isServing()) {
        try {
          Thread.sleep(300);
        } catch (InterruptedException e) {
          LOGGER.info("Exception in RemoteInterpreterServer while shutdown, Thread.sleep", e);
          Thread.currentThread().interrupt();
        }
      }

      if (server.isServing() && isForceShutdown) {
        LOGGER.info("Force shutting down");
        System.exit(1);
      }

      LOGGER.info("Shutting down");
    }
  }

  class InterpretJobListener implements JobListener {

    @Override
    public void onProgressUpdate(Job job, int progress) {
    }

    @Override
    public void onStatusChange(Job job, Status before, Status after) {
      synchronized (this) {
        notifyAll();
      }
    }
  }

  public static class InterpretJob extends Job<InterpreterResult> {

    private Interpreter interpreter;
    private String script;
    private InterpreterContext context;
    private Map<String, Object> infos;
    private InterpreterResult results;

    public InterpretJob(
        String jobId,
        String jobName,
        JobListener listener,
        Interpreter interpreter,
        String script,
        InterpreterContext context) {
      super(jobId, jobName, listener);
      this.interpreter = interpreter;
      this.script = script;
      this.context = context;
    }

    @Override
    public InterpreterResult getReturn() {
      return results;
    }

    @Override
    public int progress() {
      return 0;
    }

    @Override
    public Map<String, Object> info() {
      if (infos == null) {
        infos = new HashMap<>();
      }
      return infos;
    }

    private void processInterpreterHooks(final String noteId) {
      InterpreterHookListener hookListener = new InterpreterHookListener() {
        @Override
        public void onPreExecute(String script) {
          String cmdDev = interpreter.getHook(noteId, HookType.PRE_EXEC_DEV.getName());
          String cmdUser = interpreter.getHook(noteId, HookType.PRE_EXEC.getName());

          // User defined hook should be executed before dev hook
          List<String> cmds = Arrays.asList(cmdDev, cmdUser);
          for (String cmd : cmds) {
            if (cmd != null) {
              script = cmd + '\n' + script;
            }
          }

          InterpretJob.this.script = script;
        }

        @Override
        public void onPostExecute(String script) {
          String cmdDev = interpreter.getHook(noteId, HookType.POST_EXEC_DEV.getName());
          String cmdUser = interpreter.getHook(noteId, HookType.POST_EXEC.getName());

          // User defined hook should be executed after dev hook
          List<String> cmds = Arrays.asList(cmdUser, cmdDev);
          for (String cmd : cmds) {
            if (cmd != null) {
              script += '\n' + cmd;
            }
          }

          InterpretJob.this.script = script;
        }
      };
      hookListener.onPreExecute(script);
      hookListener.onPostExecute(script);
    }

    @Override
    public InterpreterResult jobRun() throws Throwable {
      ClassLoader currentThreadContextClassloader = Thread.currentThread().getContextClassLoader();
      try {
        InterpreterContext.set(context);
        // clear the result of last run in frontend before running this paragraph.
        context.out.clear();

        InterpreterResult result = null;

        // Open the interpreter instance prior to calling interpret().
        // This is necessary because the earliest we can register a hook
        // is from within the open() method.
        LazyOpenInterpreter lazy = (LazyOpenInterpreter) interpreter;
        if (!lazy.isOpen()) {
          lazy.open();
          result = lazy.executePrecode(context);
        }

        if (result == null || result.code() == Code.SUCCESS) {
          // Add hooks to script from registry.
          // note scope first, followed by global scope.
          // Here's the code after hooking:
          //     global_pre_hook
          //     note_pre_hook
          //     script
          //     note_post_hook
          //     global_post_hook
          processInterpreterHooks(context.getNoteId());
          processInterpreterHooks(null);
          LOGGER.debug("Script after hooks: {}", script);
          result = interpreter.interpret(script, context);
        }

        // data from context.out is prepended to InterpreterResult if both defined
        context.out.flush();
        List<InterpreterResultMessage> resultMessages = context.out.toInterpreterResultMessage();

        for (InterpreterResultMessage resultMessage : result.message()) {
          // only add non-empty InterpreterResultMessage
          if (!StringUtils.isBlank(resultMessage.getData())) {
            resultMessages.add(resultMessage);
          }
        }

        List<String> stringResult = new ArrayList<>();
        for (InterpreterResultMessage msg : resultMessages) {
          if (msg.getType() == InterpreterResult.Type.IMG) {
            LOGGER.debug("InterpreterResultMessage: IMAGE_DATA");
          } else {
            LOGGER.debug("InterpreterResultMessage: {}", msg);
          }
          stringResult.add(msg.getData());
        }
        // put result into resource pool
        if (context.getLocalProperties().containsKey("saveAs")) {
          if (stringResult.size() == 1) {
            LOGGER.info("Saving result into ResourcePool as single string: " +
                    context.getLocalProperties().get("saveAs"));
            context.getResourcePool().put(
                    context.getLocalProperties().get("saveAs"), stringResult.get(0));
          } else {
            LOGGER.info("Saving result into ResourcePool as string list: " +
                    context.getLocalProperties().get("saveAs"));
            context.getResourcePool().put(
                    context.getLocalProperties().get("saveAs"), stringResult);
          }
        }
        return new InterpreterResult(result.code(), resultMessages);
      } catch (Throwable e) {
        return new InterpreterResult(Code.ERROR, ExceptionUtils.getStackTrace(e));
      } finally {
        Thread.currentThread().setContextClassLoader(currentThreadContextClassloader);
        InterpreterContext.remove();
      }
    }

    @Override
    protected boolean jobAbort() {
      return false;
    }

    @Override
    public void setResult(InterpreterResult result) {
      this.results = result;
    }
  }


  @Override
  public void cancel(String sessionId,
                     String className,
                     RemoteInterpreterContext interpreterContext)
          throws InterpreterRPCException, TException {
    LOGGER.info("cancel {} {}", className, interpreterContext.getParagraphId());
    Interpreter intp = getInterpreter(sessionId, className);
    String jobId = interpreterContext.getParagraphId();
    Job job = intp.getScheduler().getJob(jobId);

    if (job != null && job.getStatus() == Status.PENDING) {
      job.setStatus(Status.ABORT);
    } else {
      Thread thread = new Thread( ()-> {
        try {
          intp.cancel(convert(interpreterContext, null));
        } catch (InterpreterException e) {
          LOGGER.error("Fail to cancel paragraph: {}", interpreterContext.getParagraphId());
        }
      });
      thread.start();
    }
  }

  @Override
  public int getProgress(String sessionId, String className,
                         RemoteInterpreterContext interpreterContext)
          throws InterpreterRPCException, TException {
    lifecycleManager.onInterpreterUse(interpreterGroupId);

    Integer manuallyProvidedProgress = progressMap.get(interpreterContext.getParagraphId());
    if (manuallyProvidedProgress != null) {
      return manuallyProvidedProgress;
    } else {
      Interpreter intp = getInterpreter(sessionId, className);
      if (intp == null) {
        throw new InterpreterRPCException("No interpreter " + className + " existed for session " + sessionId);
      }
      try {
        return intp.getProgress(convert(interpreterContext, null));
      } catch (InterpreterException e) {
        throw new InterpreterRPCException(e.toString());
      }
    }
  }


  @Override
  public String getFormType(String sessionId, String className)
          throws InterpreterRPCException, TException{
    Interpreter intp = getInterpreter(sessionId, className);
    try {
      return intp.getFormType().toString();
    } catch (InterpreterException e) {
      throw new InterpreterRPCException(e.toString());
    }
  }

  @Override
  public List<InterpreterCompletion> completion(String sessionId,
                                                String className,
                                                String buf,
                                                int cursor,
                                                RemoteInterpreterContext remoteInterpreterContext)
          throws InterpreterRPCException, TException{
    Interpreter intp = getInterpreter(sessionId, className);
    try {
      return intp.completion(buf, cursor, convert(remoteInterpreterContext, null));
    } catch (InterpreterException e) {
      throw new InterpreterRPCException("Fail to get completion, cause: " + e.getMessage());
    }
  }

  private InterpreterContext convert(RemoteInterpreterContext ric) {
    return convert(ric, createInterpreterOutput(ric.getNoteId(), ric.getParagraphId()));
  }

  private InterpreterContext convert(RemoteInterpreterContext ric, InterpreterOutput output) {
    return InterpreterContext.builder()
        .setNoteId(ric.getNoteId())
        .setNoteName(ric.getNoteName())
        .setParagraphId(ric.getParagraphId())
        .setReplName(ric.getReplName())
        .setParagraphTitle(ric.getParagraphTitle())
        .setParagraphText(ric.getParagraphText())
        .setLocalProperties(ric.getLocalProperties())
        .setAuthenticationInfo(AuthenticationInfo.fromJson(ric.getAuthenticationInfo()))
        .setGUI(GUI.fromJson(ric.getGui()))
        .setConfig(gson.fromJson(ric.getConfig(),
                   new TypeToken<Map<String, Object>>() {}.getType()))
        .setNoteGUI(GUI.fromJson(ric.getNoteGui()))
        .setAngularObjectRegistry(interpreterGroup.getAngularObjectRegistry())
        .setResourcePool(interpreterGroup.getResourcePool())
        .setInterpreterOut(output)
        .setIntpEventClient(intpEventClient)
        .setProgressMap(progressMap)
        .build();
  }


  protected InterpreterOutput createInterpreterOutput(final String noteId, final String
      paragraphId) {
    return new InterpreterOutput(new InterpreterOutputListener() {
      @Override
      public void onUpdateAll(InterpreterOutput out) {
        try {
          intpEventClient.onInterpreterOutputUpdateAll(
              noteId, paragraphId, out.toInterpreterResultMessage());
        } catch (IOException e) {
          LOGGER.error(e.getMessage(), e);
        }
      }

      @Override
      public void onAppend(int index, InterpreterResultMessageOutput out, byte[] line) {
        String output = new String(line);
        LOGGER.debug("Output Append: {}", output);
        intpEventClient.onInterpreterOutputAppend(
            noteId, paragraphId, index, output);
      }

      @Override
      public void onUpdate(int index, InterpreterResultMessageOutput out) {
        String output;
        try {
          output = new String(out.toByteArray());
          LOGGER.debug("Output Update for index {}: {}", index, output);
          intpEventClient.onInterpreterOutputUpdate(
              noteId, paragraphId, index, out.getType(), output);
        } catch (IOException e) {
          LOGGER.error(e.getMessage(), e);
        }
      }
    });
  }

  private RemoteInterpreterResult convert(InterpreterResult result,
                                          Map<String, Object> config, GUI gui, GUI noteGui) {

    List<RemoteInterpreterResultMessage> msg = new LinkedList<>();
    for (InterpreterResultMessage m : result.message()) {
      msg.add(new RemoteInterpreterResultMessage(
          m.getType().name(),
          m.getData()));
    }

    return new RemoteInterpreterResult(
        result.code().name(),
        msg,
        gson.toJson(config),
        gui.toJson(),
        noteGui.toJson());
  }

  @Override
  public String getStatus(String sessionId, String jobId)
          throws InterpreterRPCException, TException{

    lifecycleManager.onInterpreterUse(interpreterGroupId);
    if (interpreterGroup == null) {
      return Status.UNKNOWN.name();
    }

    synchronized (interpreterGroup) {
      List<Interpreter> interpreters = interpreterGroup.get(sessionId);
      if (interpreters == null) {
        return Status.UNKNOWN.name();
      }

      for (Interpreter intp : interpreters) {
        Scheduler scheduler = intp.getScheduler();
        if (scheduler != null) {
          Job job = scheduler.getJob(jobId);
          if (job != null) {
            return job.getStatus().name();
          }
        }
      }
    }
    return Status.UNKNOWN.name();
  }

  /**
   * called when object is updated in client (web) side.
   *
   * @param name
   * @param noteId      noteId where the update issues
   * @param paragraphId paragraphId where the update issues
   * @param object
   * @throws InterpreterRPCException, TException
   */
  @Override
  public void angularObjectUpdate(String name, String noteId, String paragraphId, String object)
          throws InterpreterRPCException, TException{
    AngularObjectRegistry registry = interpreterGroup.getAngularObjectRegistry();
    // first try local objects
    AngularObject ao = registry.get(name, noteId, paragraphId);
    if (ao == null) {
      LOGGER.debug("Angular object {} not exists", name);
      return;
    }

    if (object == null) {
      ao.set(null, false);
      return;
    }

    Object oldObject = ao.get();
    Object value = null;
    if (oldObject != null) {  // first try with previous object's type
      try {
        value = gson.fromJson(object, oldObject.getClass());
        ao.set(value, false);
        return;
      } catch (Exception e) {
        // it's not a previous object's type. proceed to treat as a generic type
        LOGGER.debug(e.getMessage(), e);
      }
    }

    // Generic java object type for json.
    if (value == null) {
      try {
        value = gson.fromJson(object,
            new TypeToken<Map<String, Object>>() {
            }.getType());
      } catch (Exception e) {
        // it's not a generic json object, too. okay, proceed to threat as a string type
        LOGGER.debug(e.getMessage(), e);
      }
    }

    // try string object type at last
    if (value == null) {
      value = gson.fromJson(object, String.class);
    }

    ao.set(value, false);
  }

  /**
   * When zeppelinserver initiate angular object add.
   * Dont't need to emit event to zeppelin server
   */
  @Override
  public void angularObjectAdd(String name, String noteId, String paragraphId, String object)
          throws InterpreterRPCException, TException{
    AngularObjectRegistry registry = interpreterGroup.getAngularObjectRegistry();
    // first try local objects
    AngularObject ao = registry.get(name, noteId, paragraphId);
    if (ao != null) {
      angularObjectUpdate(name, noteId, paragraphId, object);
      return;
    }

    // Generic java object type for json.
    Object value = null;
    try {
      value = gson.fromJson(object,
          new TypeToken<Map<String, Object>>() {
          }.getType());
    } catch (Exception e) {
      // it's okay. proceed to treat object as a string
      LOGGER.debug(e.getMessage(), e);
    }

    // try string object type at last
    if (value == null) {
      value = gson.fromJson(object, String.class);
    }

    registry.add(name, value, noteId, paragraphId, false);
  }

  @Override
  public void angularObjectRemove(String name, String noteId, String paragraphId)
          throws InterpreterRPCException, TException{
    AngularObjectRegistry registry = interpreterGroup.getAngularObjectRegistry();
    registry.remove(name, noteId, paragraphId, false);
  }

  @Override
  public List<String> resourcePoolGetAll() throws InterpreterRPCException, TException{
    LOGGER.debug("Request resourcePoolGetAll from ZeppelinServer");
    List<String> result = new LinkedList<>();

    if (resourcePool == null) {
      return result;
    }

    ResourceSet resourceSet = resourcePool.getAll(false);
    for (Resource r : resourceSet) {
      result.add(r.toJson());
    }
    return result;
  }

  @Override
  public boolean resourceRemove(String noteId, String paragraphId, String resourceName)
          throws InterpreterRPCException, TException{
    Resource resource = resourcePool.remove(noteId, paragraphId, resourceName);
    return resource != null;
  }

  @Override
  public ByteBuffer resourceGet(String noteId, String paragraphId, String resourceName)
          throws InterpreterRPCException, TException {
    LOGGER.debug("Request resourceGet {} from ZeppelinServer", resourceName);
    Resource resource = resourcePool.get(noteId, paragraphId, resourceName, false);

    if (resource == null || resource.get() == null || !resource.isSerializable()) {
      return ByteBuffer.allocate(0);
    } else {
      try {
        return Resource.serializeObject(resource.get());
      } catch (IOException e) {
        LOGGER.error(e.getMessage(), e);
        return ByteBuffer.allocate(0);
      }
    }
  }

  @Override
  public ByteBuffer resourceInvokeMethod(
      String noteId, String paragraphId, String resourceName, String invokeMessage) {
    InvokeResourceMethodEventMessage message =
        InvokeResourceMethodEventMessage.fromJson(invokeMessage);
    Resource resource = resourcePool.get(noteId, paragraphId, resourceName, false);
    if (resource == null || resource.get() == null) {
      return ByteBuffer.allocate(0);
    } else {
      try {
        Object o = resource.get();
        Method method = o.getClass().getMethod(
            message.methodName,
            message.getParamTypes());
        Object ret = method.invoke(o, message.params);
        if (message.shouldPutResultIntoResourcePool()) {
          // if return resource name is specified,
          // then put result into resource pool
          // and return the Resource class instead of actual return object.
          resourcePool.put(
              noteId,
              paragraphId,
              message.returnResourceName,
              ret);

          Resource returnValResource = resourcePool.get(noteId, paragraphId, message.returnResourceName);
          ByteBuffer serialized = Resource.serializeObject(returnValResource);
          if (serialized == null) {
            return ByteBuffer.allocate(0);
          } else {
            return serialized;
          }
        } else {
          // if return resource name is not specified,
          // then return serialized result
          ByteBuffer serialized = Resource.serializeObject(ret);
          if (serialized == null) {
            return ByteBuffer.allocate(0);
          } else {
            return serialized;
          }
        }
      } catch (Exception e) {
        LOGGER.error(e.getMessage(), e);
        return ByteBuffer.allocate(0);
      }
    }
  }

  @Override
  public void angularRegistryPush(String registryAsString) throws InterpreterRPCException, TException {
    try {
      Map<String, Map<String, AngularObject>> deserializedRegistry = gson
          .fromJson(registryAsString,
              new TypeToken<Map<String, Map<String, AngularObject>>>() {
              }.getType());
      interpreterGroup.getAngularObjectRegistry().setRegistry(deserializedRegistry);
    } catch (Exception e) {
      LOGGER.info("Exception in RemoteInterpreterServer while angularRegistryPush, nolock", e);
    }
  }
}
