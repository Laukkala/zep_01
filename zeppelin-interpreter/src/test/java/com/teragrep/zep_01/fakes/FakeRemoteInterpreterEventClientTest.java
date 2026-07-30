package com.teragrep.zep_01.fakes;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.*;

public final class FakeRemoteInterpreterEventClientTest {

    @Test
    public void testUnregisterInterpreter(){
        final FakeRemoteInterpreterEventClient client = new FakeRemoteInterpreterEventClient();
        Assertions.assertFalse(client.unregistered());
        client.unRegisterInterpreterProcess();
        Assertions.assertTrue(client.unregistered());
    }

    @Test
    public void testContract() {
        EqualsVerifier.forClass(FakeRemoteInterpreterEventClient.class).verify();
    }
}