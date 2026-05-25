package com.teragrep.zep_01.fakes;

import com.teragrep.zep_01.interpreter.remote.RemoteInterpreterEventClient;

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
}