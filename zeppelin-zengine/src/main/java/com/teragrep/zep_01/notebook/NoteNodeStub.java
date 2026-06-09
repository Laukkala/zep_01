package com.teragrep.zep_01.notebook;

import java.io.IOException;

public class NoteNodeStub implements NoteNode {

    @Override
    public Note getNote() throws IOException {
        throw new RuntimeException("NoteNode is a stub!");
    }

    @Override
    public Note getNote(boolean reload) throws IOException {
        throw new RuntimeException("NoteNode is a stub!");
    }

    @Override
    public String getNoteId() {
        throw new RuntimeException("NoteNode is a stub!");
    }

    @Override
    public String getNoteName() {
        throw new RuntimeException("NoteNode is a stub!");
    }

    @Override
    public String getNotePath() {
        throw new RuntimeException("NoteNode is a stub!");
    }

    @Override
    public Note getRawNote() {
        throw new RuntimeException("NoteNode is a stub!");
    }

    @Override
    public Folder getParent() {
        throw new RuntimeException("NoteNode is a stub!");
    }

    @Override
    public void setParent(Folder parent) {
        throw new RuntimeException("NoteNode is a stub!");
    }

    @Override
    public void setNotePath(String notePath) {
        throw new RuntimeException("NoteNode is a stub!");
    }

    @Override
    public void updateNotePath() {
        throw new RuntimeException("NoteNode is a stub!");
    }

    @Override
    public boolean isStub() {
        return true;
    }
}
