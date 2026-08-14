package com.teragrep.zep_01.interpreter.util;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.apache.commons.exec.CommandLine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public final class PidExecutorTest {

    @Test
    public void processIdTest() {
        final PidExecutor pidExecutor = new PidExecutor();
        final CommandLine cmdLine = CommandLine.parse("echo hello test");
        final Map<String, String> environment = new HashMap<>();

        Assertions.assertDoesNotThrow(()->pidExecutor.execute(cmdLine, environment));
        Assertions.assertFalse(pidExecutor.processId().isStub());
    }

    @Test
    public void launchTest() {
        final PidExecutor pidExecutor = new PidExecutor();
        final CommandLine cmdLine = CommandLine.parse("sleep 1");

        Thread thread = new Thread(()->Assertions.assertDoesNotThrow(()->pidExecutor.execute(cmdLine)));
        thread.start();
        while (pidExecutor.processId().isStub()){
            // Wait until processId exists, as Thread needs some time to initialize
        }
        final ProcessId pid = pidExecutor.processId();
        Assertions.assertFalse(pid.isStub());
        final File processFile = new File("/proc/" + pid.asLong());
        Assertions.assertTrue(processFile.exists());
        // Wait for process to terminate
        Assertions.assertDoesNotThrow(()->thread.join());
        Assertions.assertFalse(processFile.exists());
    }

    @Test
    public void testContract(){
        EqualsVerifier.forClass(PidExecutor.class)
                // Ignore fields from superclass as they are protected and superclass' equals() method is not compliant with EqualsVerifier
                .withIgnoredFields("streamHandler")
                .withIgnoredFields("workingDirectory")
                .withIgnoredFields("watchdog")
                .withIgnoredFields("exitValues")
                .withIgnoredFields("launcher")
                .withIgnoredFields("executorThread")
                .withIgnoredFields("exceptionCaught")
                .withIgnoredFields("processDestroyer")
                .verify();
    }
}