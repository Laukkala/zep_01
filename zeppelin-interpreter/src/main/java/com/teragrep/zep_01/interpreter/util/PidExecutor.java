package com.teragrep.zep_01.interpreter.util;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class PidExecutor extends DefaultExecutor {
    private final AtomicReference<ProcessId> pid;

    public PidExecutor(){
        super();
        pid = new AtomicReference<>(new ProcessIdStub());
    }
    @Override
    protected Process launch(final CommandLine commandLine, final Map<String, String> environment, final File workingDirectory) throws IOException {
        final Process process = super.launch(commandLine, environment, workingDirectory);
        // Since Java 8 does not support Process.pid() method, we need to use reflection instead.
        try {
            final Field field = process.getClass().getDeclaredField("pid");
            // Another try-catch block to make sure field.setAccessible isn't left to 'true' state if field.getLong() throws an exception.
            try{
                field.setAccessible(true);
                final long processId = field.getLong(process);
                pid.set(new ProcessIdImpl(processId));
            }
            finally {
                field.setAccessible(false);
            }

        } catch (final NoSuchFieldException | IllegalAccessException | SecurityException e) {
            throw new RuntimeException("Failed to read PID of process!", e);
        }
        return process;
    }

    public ProcessId processId(){
        return pid.get();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PidExecutor that = (PidExecutor) o;
        return Objects.equals(pid, that.pid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pid);
    }
}
