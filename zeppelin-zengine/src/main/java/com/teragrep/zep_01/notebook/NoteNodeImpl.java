package com.teragrep.zep_01.notebook;

import com.teragrep.zep_01.conf.ZeppelinConfiguration;
import com.teragrep.zep_01.notebook.repo.NotebookRepo;
import com.teragrep.zep_01.user.AuthenticationInfo;

import java.io.IOException;

/**
 * One node in the file system tree structure which represent the note.
 * This class has 2 usage scenarios:
 * 1. metadata of note (only noteId and note name is loaded via reading the file name)
 * 2. the note object (note content is loaded from NotebookRepo)
 * <p>
 * It will load note from NotebookRepo lazily until method getNote is called.
 */
public class NoteNodeImpl implements NoteNode {

    private Folder parent;
    private Note note;
    private NotebookRepo notebookRepo;

    public NoteNodeImpl(Note note, Folder parent, NotebookRepo notebookRepo) {
        this.note = note;
        this.parent = parent;
        this.notebookRepo = notebookRepo;
    }

    @Override
    public synchronized Note getNote() throws IOException {
        return getNote(false);
    }

    /**
     * This method will load note from NotebookRepo. If you just want to get noteId, noteName or
     * notePath, you can call method getNoteId, getNoteName & getNotePath
     *
     * @return
     * @throws IOException
     */

    @Override
    public synchronized Note getNote(boolean reload) throws IOException {
        if (!note.isLoaded() || reload) {
            note = notebookRepo.get(note.getId(), note.getPath(), AuthenticationInfo.ANONYMOUS);
            if (parent.toString().equals("/")) {
                note.setPath("/" + note.getName());
            } else {
                note.setPath(parent.toString() + "/" + note.getName());
            }
            note.setCronSupported(ZeppelinConfiguration.create());
            note.setLoaded(true);
        }
        return note;
    }

    @Override
    public String getNoteId() {
        return this.note.getId();
    }

    @Override
    public String getNoteName() {
        return this.note.getName();
    }

    @Override
    public String getNotePath() {
        if (parent.getPath().equals("/")) {
            return parent.getPath() + note.getName();
        } else {
            return parent.getPath() + "/" + note.getName();
        }
    }

    /**
     * This method will just return the note object without checking whether it is loaded
     * from NotebookRepo.
     *
     * @return
     */

    @Override
    public Note getRawNote() {
        return this.note;
    }

    @Override
    public Folder getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return getNotePath();
    }

    @Override
    public void setParent(Folder parent) {
        this.parent = parent;
    }

    @Override
    public void setNotePath(String notePath) {
        this.note.setPath(notePath);
    }

    /**
     * This is called when the ancestor folder is moved.
     */
    @Override
    public void updateNotePath() {
        this.note.setPath(getNotePath());
    }

    @Override
    public boolean isStub() {
        return false;
    }
}
