package com.teragrep.zep_01.notebook;

import java.io.IOException;
import java.util.Objects;

public final class NoteNodeStub implements NoteNode {
    private final boolean isStub;

    public NoteNodeStub(){
        this(true);
    }

    private NoteNodeStub(boolean isStub){
        this.isStub = isStub;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoteNodeStub that = (NoteNodeStub) o;
        return isStub == that.isStub;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isStub);
    }
}
