package com.teragrep.zep_01.fakes;

import com.teragrep.zep_01.interpreter.remote.RemoteInterpreterEventClient;

public final class FakeFailingRemoteInterpreterEventClient extends RemoteInterpreterEventClient {
    private final RuntimeException expectedException;
    private RuntimeException thrownException;
    public FakeFailingRemoteInterpreterEventClient(final String host, final int port, final int connectionPoolSize, RuntimeException expectedException){
        super(host,port,connectionPoolSize);
        this.expectedException =expectedException;
    }

    @Override
    public void unRegisterInterpreterProcess() {
        thrownException = expectedException;
        throw thrownException;
    }
    public RuntimeException exception(){
        return thrownException;
    }
}