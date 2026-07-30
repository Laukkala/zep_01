package com.teragrep.zep_01.notebook;

import com.teragrep.stb_01.Stubable;

import java.io.IOException;

public interface NoteNode extends Stubable {

    public Note getNote() throws IOException;

    public Note getNote(boolean reload) throws IOException;

    public String getNoteId();

    public String getNoteName();

    public String getNotePath();

    public Note getRawNote();

    public Folder getParent();

    public void setParent(Folder parent);

    public void setNotePath(String notePath);

    public void updateNotePath();
}
