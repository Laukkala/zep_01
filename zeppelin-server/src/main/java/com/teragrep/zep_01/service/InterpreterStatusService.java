package com.teragrep.zep_01.service;
import com.teragrep.zep_01.common.Message;
import com.teragrep.zep_01.interpreter.Interpreter;
import com.teragrep.zep_01.interpreter.ManagedInterpreterGroup;
import com.teragrep.zep_01.interpreter.status.InterpreterStatus;
import com.teragrep.zep_01.notebook.Notebook;
import com.teragrep.zep_01.socket.ConnectionManager;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class InterpreterStatusService implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(InterpreterStatusService.class);
    private final Notebook notebook;
    private final ConnectionManager connectionManager;
    private final Thread statusThread;

    @Inject
    public InterpreterStatusService(
            Notebook notebook,
            ConnectionManager connectionManager){
        this.notebook = notebook;
        this.connectionManager = connectionManager;
        this. statusThread = new Thread(this);
    }

    public void init(){
        statusThread.start();
    }

    private Message generateStatusMessage(){
        try{
            final JsonObjectBuilder interpreterGroupJson = Json.createObjectBuilder();
            final List<ManagedInterpreterGroup> interpreterGroups = notebook.getInterpreterSettingManager().getAllInterpreterGroup();
            for (ManagedInterpreterGroup interpreterGroup: interpreterGroups) {
                final JsonObjectBuilder sessionJson = Json.createObjectBuilder();
                for (Map.Entry<String, List<Interpreter>> session : interpreterGroup.sessions().entrySet()) {
                    final JsonObjectBuilder interpreterJson = Json.createObjectBuilder();
                    for (Interpreter interpreter : session.getValue()){
                        final InterpreterStatus status = interpreter.status();
                        if(!status.isStub()){
                            interpreterJson.add(interpreter.getClassName(),status.asJson());
                        }
                    }
                    sessionJson.add(session.getKey(),interpreterJson.build());
                }
                interpreterGroupJson.add(interpreterGroup.getId(),sessionJson);
            }
            return new Message(Message.OP.INTERPRETER_STATUS)
                    .put("status",interpreterGroupJson);
        } catch (Exception e){
            LOGGER.error("Failed to broadcast interpreter status information!",e);
            return new Message(Message.OP.INTERPRETER_STATUS)
                    .put("status","Failed to broadcast interpreter status information! Check technical logs for details.");
        }
    }

    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(2000);
                Message message = generateStatusMessage();
                connectionManager.broadcast(message);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
