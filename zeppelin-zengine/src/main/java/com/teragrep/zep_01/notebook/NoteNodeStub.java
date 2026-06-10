package com.teragrep.zep_01.notebook;

import java.io.IOException;

public final class NoteNodeStub implements NoteNode {

    @Override
    public Note getNote() throws IOException {
        throw new UnsupportedOperationException("NoteNode is a stub!");
    }

    @Override
    public Note getNote(final boolean reload) throws IOException {
        throw new UnsupportedOperationException("NoteNode is a stub!");
    }

    @Override
    public String getNoteId() {
        throw new UnsupportedOperationException("NoteNode is a stub!");
    }

    @Override
    public String getNoteName() {
        throw new UnsupportedOperationException("NoteNode is a stub!");
    }

    @Override
    public String getNotePath() {
        throw new UnsupportedOperationException("NoteNode is a stub!");
    }

    @Override
    public Note getRawNote() {
        throw new UnsupportedOperationException("NoteNode is a stub!");
    }

    @Override
    public Folder getParent() {
        throw new UnsupportedOperationException("NoteNode is a stub!");
    }

    @Override
    public void setParent(final Folder parent) {
        throw new UnsupportedOperationException("NoteNode is a stub!");
    }

    @Override
    public void setNotePath(final String notePath) {
        throw new UnsupportedOperationException("NoteNode is a stub!");
    }

    @Override
    public void updateNotePath() {
        throw new UnsupportedOperationException("NoteNode is a stub!");
    }

    @Override
    public boolean isStub() {
        return true;
    }
}
