/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.teragrep.zep_01.notebook;

import com.teragrep.zep_01.notebook.exception.NoteNotFoundException;
import org.apache.commons.lang3.StringUtils;
import com.teragrep.zep_01.conf.ZeppelinConfiguration;
import com.teragrep.zep_01.notebook.repo.NotebookRepo;
import com.teragrep.zep_01.user.AuthenticationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manager class for note. It handle all the note related operations, such as get, create,
 * delete & move note.
 *
 * It load 2 kinds of metadata into memory:
 * 1. Mapping from noteId to note name
 * 2. The tree structure of notebook folder
 *
 * Note will be loaded lazily. Initially only noteId nad note name is loaded,
 * other note content is loaded until getNote is called.
 *
 * TODO(zjffdu) implement the lifecycle manager of Note
 * (release memory if note is not used for some period)
 */
@Singleton
public class NoteManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(NoteManager.class);
  public static final String TRASH_FOLDER = "~Trash";
  private Folder root;
  private Folder trash;

  private NotebookRepo notebookRepo;
  // noteId -> notePath
  private Map<String, String> notesInfo;

  @Inject
  public NoteManager(NotebookRepo notebookRepo) throws IOException {
    this.notebookRepo = notebookRepo;
    this.root = new Folder("/", notebookRepo);
    this.trash = this.root.getOrCreateFolder(TRASH_FOLDER);
    init();
  }

  // build the tree structure of notes
  private void init() throws IOException {
    this.notesInfo = notebookRepo.list(AuthenticationInfo.ANONYMOUS).values().stream()
        .collect(Collectors.toMap(NoteInfo::getId, NoteInfo::getPath));
    this.notesInfo.entrySet().stream()
        .forEach(entry ->
        {
          try {
            addOrUpdateNoteNode(new Note(new NoteInfo(entry.getKey(), entry.getValue())));
          } catch (IOException e) {
            LOGGER.warn(e.getMessage());
          }
        });
  }

  public Map<String, String> getNotesInfo() {
    return notesInfo;
  }

  /**
   * Return java stream instead of List to save memory, otherwise OOM will happen
   * when there's large amount of notes.
   * @return
   */
  public Stream<Note> getNotesStream() {
    return notesInfo.keySet().stream()
            .map(noteId -> {
              try {
                return getNoteNode(noteId).getNote();
              } catch (Exception e) {
                LOGGER.warn("Fail to load note: {}", noteId, e);
                return null;
              }
            })
            .filter(Objects::nonNull);
  }

  /**
   *
   * @throws IOException
   */
  public void reloadNotes() throws IOException {
    this.root = new Folder("/", notebookRepo);
    this.trash = this.root.getOrCreateFolder(TRASH_FOLDER);
    init();
  }

  private void addOrUpdateNoteNode(Note note) throws IOException {
    String notePath = note.getPath();

    String[] tokens = notePath.split("/");
    Folder curFolder = root;
    for (int i = 0; i < tokens.length - 1; ++i) {
      if (!StringUtils.isBlank(tokens[i])) {
        curFolder = curFolder.getOrCreateFolder(tokens[i]);
      }
    }

    curFolder.addNote(note.getId(), note);
    this.notesInfo.put(note.getId(), note.getPath());
  }

  /**
   * Check whether there exist note under this noteId.
   *
   * @param noteId
   * @return
   */
  public boolean containsNote(String noteId) {
    try {
      getNoteNode(noteId);
      return true;
    } catch (NoteNotFoundException e) {
      return false;
    }
  }

  public boolean trashContainsNote(String noteId) {
    final NoteNode noteNode = getTrashFolder().getNote(noteId);
    return !noteNode.isStub();
  }

  /**
   * Check whether there exist such folder.
   *
   * @param folderPath
   * @return
   */
  public boolean containsFolder(String folderPath) {
    try {
      getFolder(folderPath);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  /**
   * Save note to NoteManager, it won't check duplicates, this is used when updating note.
   * Only save note in 2 cases:
   *  1. Note is new created, isSaved is false
   *  2. Note is in loaded state. Unload state means its content is empty.
   *
   * @param note
   * @param subject
   * @throws IOException
   */
  public void saveNote(Note note, AuthenticationInfo subject) throws IOException {
    if (note.isRemoved()) {
      LOGGER.warn("Try to save note: {} when it is removed", note.getId());
    } else if (note.isLoaded() || !note.isSaved()) {
      addOrUpdateNoteNode(note);
      this.notebookRepo.save(note, subject);
      note.setSaved(true);
    } else {
      LOGGER.warn("Try to save note: {} when it is unloaded", note.getId());
    }
  }

  public void addNote(Note note, AuthenticationInfo subject) throws IOException {
    addOrUpdateNoteNode(note);
    note.setLoaded(true);
  }

  /**
   * Add or update Note
   *
   * @param note
   * @throws IOException
   */
  public void saveNote(Note note) throws IOException {
    saveNote(note, AuthenticationInfo.ANONYMOUS);
  }

  /**
   * Remove note from NotebookRepo and NoteManager
   *
   * @param noteId
   * @param subject
   * @throws IOException
   */
  public void removeNote(String noteId, AuthenticationInfo subject) throws IOException {
    String notePath = this.notesInfo.remove(noteId);
    Folder folder = getOrCreateFolder(getFolderName(notePath));
    folder.removeNote(noteId);
    this.notebookRepo.remove(noteId, notePath, subject);
  }

  public void moveNote(String noteId,
                       String newNotePath,
                       AuthenticationInfo subject) throws IOException {
    String notePath = this.notesInfo.get(noteId);
    if (noteId == null) {
      throw new IOException("No metadata found for this note: " + noteId);
    }

    // move the old NoteNode from notePath to newNotePath
    NoteNode noteNode = getNoteNode(noteId);
    if(noteNode.isStub()){
      throw new NoteNotFoundException("Note with ID "+noteId+ " not found!");
    }
    noteNode.getParent().removeNote(noteId);
    noteNode.setNotePath(newNotePath);
    String newParent = getFolderName(newNotePath);
    Folder newFolder = getOrCreateFolder(newParent);
    newFolder.addNoteNode(noteNode);

    // update noteInfo mapping
    this.notesInfo.put(noteId, newNotePath);

    // update notebookrepo
    this.notebookRepo.move(noteId, notePath, newNotePath, subject);

    // save note if note name is changed, because we need to update the note field in note json.
    String oldNoteName = getNoteName(notePath);
    String newNoteName = getNoteName(newNotePath);
    if (!StringUtils.equalsIgnoreCase(oldNoteName, newNoteName)) {
      this.notebookRepo.save(noteNode.getRawNote(), subject);
    }
  }

  public void moveFolder(String folderPath,
                         String newFolderPath,
                         AuthenticationInfo subject) throws IOException {

    // update notebookrepo
    this.notebookRepo.move(folderPath, newFolderPath, subject);

    // update filesystem tree
    Folder folder = getFolder(folderPath);
    folder.getParent().removeFolder(folder.getName(), subject);
    Folder newFolder = getOrCreateFolder(newFolderPath);
    newFolder.getParent().addFolder(newFolder.getName(), folder);

    // update notesInfo
    for (Note note : folder.getRawNotesRecursively()) {
      notesInfo.put(note.getId(), note.getPath());
    }
  }

  /**
   * Remove the folder from the tree and returns the affected NoteInfo under this folder.
   *
   * @param folderPath
   * @param subject
   * @return
   * @throws IOException
   */
  public List<Note> removeFolder(String folderPath, AuthenticationInfo subject) throws IOException {

    // update notebookrepo
    this.notebookRepo.remove(folderPath, subject);

    // update filesystem tree
    Folder folder = getFolder(folderPath);
    List<Note> notes = folder.getParent().removeFolder(folder.getName(), subject);

    // update notesInfo
    for (Note note : notes) {
      this.notesInfo.remove(note.getId());
    }

    return notes;
  }

  /**
   * Get note from NotebookRepo.
   *
   * @param noteId
   * @return return null if not found on NotebookRepo.
   * @throws IOException
   */
  public Note getNote(String noteId, boolean reload) throws IOException {
    String notePath = this.notesInfo.get(noteId);
    if (notePath == null) {
      return null;
    }
    NoteNode noteNode = getNoteNode(noteId);
    if(noteNode.isStub()){
      throw new NoteNotFoundException("No such note "+noteId+" !");
    }
    return noteNode.getNote(reload);
  }

  /**
   * Get note from NotebookRepo.
   *
   * @param noteId
   * @return returns the searched for Note.
   * @throws IOException
   */
  public Note getNote(String noteId) throws IOException {
    String notePath = this.notesInfo.get(noteId);
    if (notePath == null) {
      return null;
    }
    NoteNode noteNode = getNoteNode(noteId);
    if(noteNode.isStub()){
      throw new NoteNotFoundException("No such note "+noteId+" !");
    }
    return noteNode.getNote();
  }

  /**
   *
   * @param folderName  Absolute path of folder name
   * @return
   */
  public Folder getOrCreateFolder(String folderName) {
    String[] tokens = folderName.split("/");
    Folder curFolder = root;
    for (int i = 0; i < tokens.length; ++i) {
      if (!StringUtils.isBlank(tokens[i])) {
        curFolder = curFolder.getOrCreateFolder(tokens[i]);
      }
    }
    return curFolder;
  }

  private NoteNode getNoteNode(String noteId) throws NoteNotFoundException {
    Folder curFolder = root;
    NoteNode noteNode = curFolder.getNote(noteId);
    if(noteNode.isStub()){
      throw new NoteNotFoundException("Note with id "+noteId+" not found!");
    }
    return noteNode;
  }

  private Folder getFolder(String folderPath) throws IOException {
    String[] tokens = folderPath.split("/");
    Folder curFolder = root;
    for (int i = 0; i < tokens.length; ++i) {
      if (!StringUtils.isBlank(tokens[i])) {
        curFolder = curFolder.getFolder(tokens[i]);
        if (curFolder == null) {
          throw new IOException("Can not find folder: " + folderPath);
        }
      }
    }
    return curFolder;
  }

  public Folder getTrashFolder() {
    return this.trash;
  }

  private String getFolderName(String notePath) {
    int pos = notePath.lastIndexOf('/');
    return notePath.substring(0, pos);
  }

  private String getNoteName(String notePath) {
    int pos = notePath.lastIndexOf('/');
    return notePath.substring(pos + 1);
  }


}
