package com.teragrep.zep_01.fakes;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.*;

public class FakeRemoteInterpreterEventClientTest {

    @Test
    public void testUnregisterInterpreter(){
        final FakeRemoteInterpreterEventClient client = new FakeRemoteInterpreterEventClient();
        Assertions.assertEquals(false, client.unregistered());
        client.unRegisterInterpreterProcess();
        Assertions.assertEquals(true, client.unregistered());
    }

    @Test
    public void testContract() {
        EqualsVerifier.forClass(FakeRemoteInterpreterEventClient.class).verify();
    }
}