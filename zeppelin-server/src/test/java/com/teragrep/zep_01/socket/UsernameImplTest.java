package com.teragrep.zep_01.socket;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class UsernameImplTest {

    @Test
    public void testContract(){
        EqualsVerifier.forClass(UsernameImpl.class).verify();
    }

}