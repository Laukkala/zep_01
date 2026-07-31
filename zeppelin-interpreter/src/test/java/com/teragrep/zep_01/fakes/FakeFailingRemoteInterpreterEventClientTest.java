package com.teragrep.zep_01.fakes;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class FakeFailingRemoteInterpreterEventClientTest {

    @Test
    public void testUnregisterInterpreter(){
        final RuntimeException exception = new RuntimeException();
        final FakeFailingRemoteInterpreterEventClient client = new FakeFailingRemoteInterpreterEventClient(exception);
        Assertions.assertThrows(RuntimeException.class,()->{client.unRegisterInterpreterProcess();});
    }

    @Test
    public void testException(){
        final RuntimeException exception = new RuntimeException();
        final FakeFailingRemoteInterpreterEventClient client = new FakeFailingRemoteInterpreterEventClient(exception);
        Assertions.assertEquals(exception,client.exception());
    }

    @Test
    public void testContract() {
        EqualsVerifier.forClass(FakeFailingRemoteInterpreterEventClient.class).verify();
    }
}