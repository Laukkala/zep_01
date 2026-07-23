package com.teragrep.zep_01.fakes;

import com.teragrep.zep_01.interpreter.remote.RemoteInterpreterEventClient;

import java.util.Objects;

public final class FakeRemoteInterpreterEventClient extends RemoteInterpreterEventClient {
    private boolean unregistered = false;
    public FakeRemoteInterpreterEventClient(final String host, final int port, final int connectionPoolSize){
        super(host,port,connectionPoolSize);
    }

    @Override
    public void unRegisterInterpreterProcess() {
        unregistered = true;
    }
    public boolean unregistered(){
        return unregistered;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FakeRemoteInterpreterEventClient that = (FakeRemoteInterpreterEventClient) o;
        return unregistered == that.unregistered;
    }

    @Override
    public int hashCode() {
        return Objects.hash(unregistered);
    }
}