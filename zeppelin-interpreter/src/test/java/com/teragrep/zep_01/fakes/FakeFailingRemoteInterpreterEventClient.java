package com.teragrep.zep_01.fakes;

import com.teragrep.zep_01.interpreter.remote.RemoteInterpreterEventClient;

import java.util.Objects;

public final class FakeFailingRemoteInterpreterEventClient extends RemoteInterpreterEventClient {
    private final RuntimeException exception;
    public FakeFailingRemoteInterpreterEventClient(final String host, final int port, final int connectionPoolSize, final RuntimeException exception){
        super(host,port,connectionPoolSize);
        this.exception = exception;
    }

    @Override
    public void unRegisterInterpreterProcess() {
        throw exception;
    }
    public RuntimeException exception(){
        return exception;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FakeFailingRemoteInterpreterEventClient that = (FakeFailingRemoteInterpreterEventClient) o;
        return Objects.equals(exception, that.exception);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exception);
    }
}