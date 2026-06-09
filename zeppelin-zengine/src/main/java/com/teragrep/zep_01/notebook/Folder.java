package com.teragrep.zep_01.notebook;

import com.teragrep.zep_01.notebook.repo.NotebookRepo;
import com.teragrep.zep_01.user.AuthenticationInfo;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represent one folder that could contains sub folders and note files.
 */
public class Folder {

    private String name;
    private Folder parent;
    private NotebookRepo notebookRepo;

    // noteId -> NoteNode
    private Map<String, NoteNode> notes = new HashMap<>();
    // folderName -> Folder
    private Map<String, Folder> subFolders = new HashMap<>();

    public Folder(String name, NotebookRepo notebookRepo) {
        this.name = name;
        this.notebookRepo = notebookRepo;
    }

    public Folder(String name, Folder parent, NotebookRepo notebookRepo) {
        this(name, notebookRepo);
        this.parent = parent;
    }

    public synchronized Folder getOrCreateFolder(String folderName) {
        if (StringUtils.isBlank(folderName)) {
            return this;
        }
        if (!subFolders.containsKey(folderName)) {
            subFolders.put(folderName, new Folder(folderName, this, notebookRepo));
        }
        return subFolders.get(folderName);
    }

    public Folder getParent() {
        return parent;
    }

    public void setParent(Folder parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Folder getFolder(String folderName) {
        return subFolders.get(folderName);
    }

    public Map<String, Folder> getFolders() {
        return subFolders;
    }

    public NoteNode getNote(String noteId) {
        // Search for the note within this Folder
        NoteNode rv = new NoteNodeStub();
        if (this.notes.containsKey(noteId)) {
            rv = notes.get(noteId);
        } else {
            for (Folder subfolder : subFolders.values()) {
                // If a match is found in one of the subfolders, return it.
                NoteNode noteNode = subfolder.getNote(noteId);
                if (!noteNode.isStub()) {
                    rv = noteNode;
                    break;
                }
            }
        }
        return rv;
    }

    public void addNote(String noteId, Note note) {
        notes.put(noteId, new NoteNodeImpl(note, this, notebookRepo));
    }

    /**
     * Attach another folder under this folder, this is used when moving folder.
     * The path of notes under this folder also need to be updated.
     */
    public void addFolder(String folderName, Folder folder) throws IOException {
        subFolders.put(folderName, folder);
        folder.setParent(this);
        folder.setName(folderName);
        for (NoteNode noteNode : folder.getNoteNodeRecursively()) {
            noteNode.updateNotePath();
        }
    }


    /**
     * Attach note under this folder, this is used when moving note
     *
     * @param noteNode
     */
    public void addNoteNode(NoteNode noteNode) {
        this.notes.put(noteNode.getNoteId(), noteNode);
        noteNode.setParent(this);
    }

    public void removeNote(String noteId) {
        this.notes.remove(noteId);
    }

    public List<Note> removeFolder(String folderName,
                                   AuthenticationInfo subject) throws IOException {
        Folder folder = this.subFolders.remove(folderName);
        return folder.getRawNotesRecursively();
    }

    public List<Note> getRawNotesRecursively() {
        List<Note> notesInfo = new ArrayList<>();
        for (NoteNode noteNode : this.notes.values()) {
            notesInfo.add(noteNode.getRawNote());
        }
        for (Folder folder : subFolders.values()) {
            notesInfo.addAll(folder.getRawNotesRecursively());
        }
        return notesInfo;
    }

    public List<NoteNode> getNoteNodeRecursively() {
        List<NoteNode> noteNodeRecursively = new ArrayList<>();
        noteNodeRecursively.addAll(this.notes.values());
        for (Folder folder : subFolders.values()) {
            noteNodeRecursively.addAll(folder.getNoteNodeRecursively());
        }
        return noteNodeRecursively;
    }

    public Map<String, NoteNode> getNotes() {
        return notes;
    }

    public String getPath() {
        // root
        if (name.equals("/")) {
            return name;
        }
        // folder under root
        if (parent.name.equals("/")) {
            return "/" + name;
        }
        // other cases
        return parent.toString() + "/" + name;
    }

    @Override
    public String toString() {
        return getPath();
    }
}
