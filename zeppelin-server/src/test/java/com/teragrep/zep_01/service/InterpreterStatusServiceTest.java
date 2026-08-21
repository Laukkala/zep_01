package com.teragrep.zep_01.service;

import com.teragrep.zep_01.common.Message;
import com.teragrep.zep_01.conf.ZeppelinConfiguration;
import com.teragrep.zep_01.interpreter.InterpreterFactory;
import com.teragrep.zep_01.interpreter.InterpreterSettingManager;
import com.teragrep.zep_01.notebook.AuthorizationService;
import com.teragrep.zep_01.notebook.NoteManager;
import com.teragrep.zep_01.notebook.Notebook;
import com.teragrep.zep_01.notebook.repo.NotebookRepo;
import com.teragrep.zep_01.socket.ConnectionManager;
import com.teragrep.zep_01.user.Credentials;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InterpreterStatusServiceTest {

    @Test
    public void testStatus(){
        ZeppelinConfiguration conf = ZeppelinConfiguration.create();
        FakeInterpreterSettingManager fakeInterpreterSettingManager = Assertions.assertDoesNotThrow(()->new FakeInterpreterSettingManager(conf));
        FakeNotebook fakeNotebook = Assertions.assertDoesNotThrow(()->new FakeNotebook(conf, fakeInterpreterSettingManager));
        FakeConnectionManager fakeConnectionManager = Assertions.assertDoesNotThrow(()->new FakeConnectionManager());
        InterpreterStatusService statusService = new InterpreterStatusService(fakeNotebook,fakeConnectionManager);
        statusService.init();
        Assertions.assertDoesNotThrow(()->Thread.sleep(5000));
        List<Message> messages = fakeConnectionManager.messageList();
        Assertions.assertTrue(messages.size() > 0);
    }

    //TODO: separate these Fakes
    public class FakeNotebook extends Notebook{
        public FakeNotebook(ZeppelinConfiguration conf, InterpreterSettingManager interpreterSettingManager) throws IOException {
            super(conf, null, null, null, null, interpreterSettingManager, null);
        }
    }
    public class FakeConnectionManager extends ConnectionManager {

        private final List<Message> messageList;
        public FakeConnectionManager() {
            super(null);
            this.messageList = new ArrayList<>();
        }

        @Override
        public void broadcast(Message m) {
            messageList.add(m);
        }

        public List<Message> messageList(){
            return messageList;
        }
    }

    public class FakeInterpreterSettingManager extends InterpreterSettingManager {
        public FakeInterpreterSettingManager(ZeppelinConfiguration conf) throws IOException{
            super(conf,null,null);
        }
    }
}