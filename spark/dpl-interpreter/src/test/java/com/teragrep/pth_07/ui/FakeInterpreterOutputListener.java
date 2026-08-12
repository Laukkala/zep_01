package com.teragrep.pth_07.ui;

import com.teragrep.zep_01.interpreter.InterpreterOutput;
import com.teragrep.zep_01.interpreter.InterpreterOutputListener;
import com.teragrep.zep_01.interpreter.InterpreterResultMessage;
import com.teragrep.zep_01.interpreter.InterpreterResultMessageOutput;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class FakeInterpreterOutputListener implements InterpreterOutputListener {
        private List<InterpreterResultMessage> outputList = new ArrayList<>();

        @Override
        public void onUpdateAll(final InterpreterOutput out) {
        }

        @Override
        public void onAppend(final int index, final InterpreterResultMessageOutput out, final byte[] line) {
        }

        @Override
        public void onUpdate(final int index, final InterpreterResultMessageOutput out) {
            try{
                outputList.add(out.toInterpreterResultMessage());
            }
            catch (final IOException e){
                Assertions.fail("IOException occurred while listening to output messages!");
            }
        }

        public List<InterpreterResultMessage> outputs(){
            return outputList;
        }
}
