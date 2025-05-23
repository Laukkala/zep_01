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

package com.teragrep.zep_01.common;

import com.google.gson.Gson;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Copied from zeppelin-zengine (TODO, zjffdu). Should resume the same piece of code instead of copying.
 * Zeppelin websocket message template class.
 */
public class Message implements JsonSerializable {
  /**
   * Representation of event type.
   */
  public enum OP {
    GET_HOME_NOTE,    // [c-s] load note for home screen

    GET_NOTE,         // [c-s] client load note
                      // @param id note id

    RELOAD_NOTE,      // [c-s] client load note
                      // @param id note id

    NOTE,             // [s-c] note info
                      // @param note serialized Note object

    PARAGRAPH,        // [s-c] paragraph info
                      // @param paragraph serialized paragraph object

    PROGRESS,         // [s-c] progress update
                      // @param id paragraph id
                      // @param progress percentage progress

    NEW_NOTE,         // [c-s] create new notebook
    DEL_NOTE,         // [c-s] delete notebook
                      // @param id note id
    REMOVE_FOLDER,
    MOVE_NOTE_TO_TRASH,
    MOVE_FOLDER_TO_TRASH,
    RESTORE_FOLDER,
    RESTORE_NOTE,
    RESTORE_ALL,
    EMPTY_TRASH,
    CLONE_NOTE,       // [c-s] clone new notebook
                      // @param id id of note to clone
                      // @param name name for the cloned note
    IMPORT_NOTE,      // [c-s] import notebook
                      // @param object notebook

    CONVERT_NOTE_NBFORMAT,     // [c-s] converting note to nbformat
    CONVERTED_NOTE_NBFORMAT,     // [s-c] converting note to nbformat

    NOTE_UPDATE,

    NOTE_RENAME,

    UPDATE_PERSONALIZED_MODE, // [c-s] update personalized mode (boolean)
                              // @param note id and boolean personalized mode value

    FOLDER_RENAME,

    RUN_PARAGRAPH,    // [c-s] run paragraph
                      // @param id paragraph id
                      // @param paragraph paragraph content.ie. script
                      // @param config paragraph config
                      // @param params paragraph params

    COMMIT_PARAGRAPH, // [c-s] commit paragraph
                      // @param id paragraph id
                      // @param title paragraph title
                      // @param paragraph paragraph content.ie. script
                      // @param config paragraph config
                      // @param params paragraph params

    CANCEL_PARAGRAPH, // [c-s] cancel paragraph run
                      // @param id paragraph id

    MOVE_PARAGRAPH,   // [c-s] move paragraph order
                      // @param id paragraph id
                      // @param index index the paragraph want to go

    INSERT_PARAGRAPH, // [c-s] create new paragraph below current paragraph
                      // @param target index

    COPY_PARAGRAPH,   // [c-s] create new para below current para as a copy of current para
                      // @param target index
                      // @param title paragraph title
                      // @param paragraph paragraph content.ie. script
                      // @param config paragraph config
                      // @param params paragraph params

    EDITOR_SETTING,   // [c-s] ask paragraph editor setting
                      // @param paragraph text keyword written in paragraph
                      // ex) spark.spark or spark

    COMPLETION,       // [c-s] ask completion candidates
                      // @param id
                      // @param buf current code
                      // @param cursor cursor position in code

    COMPLETION_LIST,  // [s-c] send back completion candidates list
                      // @param id
                      // @param completions list of string

    LIST_NOTES,                   // [c-s] ask list of note
    RELOAD_NOTES_FROM_REPO,       // [c-s] reload notes from repo

    NOTES_INFO,                   // [s-c] list of note infos
                                  // @param notes serialized List<NoteInfo> object

    PARAGRAPH_REMOVE,
    PARAGRAPH_CLEAR_OUTPUT,       // [c-s] clear output of paragraph
    PARAGRAPH_CLEAR_ALL_OUTPUT,   // [c-s] clear output of all paragraphs
    PARAGRAPH_APPEND_OUTPUT,      // [s-c] append output
    PARAGRAPH_UPDATE_OUTPUT,      // [s-c] update (replace) output
    PING,
    PONG,
    AUTH_INFO,

    ANGULAR_OBJECT_UPDATE,        // [s-c] add/update angular object
    ANGULAR_OBJECT_REMOVE,        // [s-c] add angular object del

    ANGULAR_OBJECT_UPDATED,       // [c-s] angular object value updated,

    ANGULAR_OBJECT_CLIENT_BIND,   // [c-s] angular object updated from AngularJS z object

    ANGULAR_OBJECT_CLIENT_UNBIND, // [c-s] angular object unbind from AngularJS z object

    LIST_CONFIGURATIONS,          // [c-s] ask all key/value pairs of configurations
    CONFIGURATIONS_INFO,          // [s-c] all key/value pairs of configurations
                                  // @param settings serialized Map<String, String> object

    CHECKPOINT_NOTE,              // [c-s] checkpoint note to storage repository
                                  // @param noteId
                                  // @param checkpointName

    LIST_REVISION_HISTORY,        // [c-s] list revision history of the notebook
                                  // @param noteId
    NOTE_REVISION,                // [c-s] get certain revision of note
                                  // @param noteId
                                  // @param revisionId
    SET_NOTE_REVISION,            // [c-s] set current notebook head to this revision
                                  // @param noteId
                                  // @param revisionId
    NOTE_REVISION_FOR_COMPARE,    // [c-s] get certain revision of note for compare
                                  // @param noteId
                                  // @param revisionId
                                  // @param position
    APP_APPEND_OUTPUT,            // [s-c] append output
    APP_UPDATE_OUTPUT,            // [s-c] update (replace) output
    APP_LOAD,                     // [s-c] on app load
    APP_STATUS_CHANGE,            // [s-c] on app status change

    LIST_NOTE_JOBS,               // [c-s] get note job management information
    LIST_UPDATE_NOTE_JOBS,        // [c-s] get job management information for until unixtime
    UNSUBSCRIBE_UPDATE_NOTE_JOBS, // [c-s] unsubscribe job information for job management
    // @param unixTime
    GET_INTERPRETER_BINDINGS,    // [c-s] get interpreter bindings
    SAVE_INTERPRETER_BINDINGS,    // [c-s] save interpreter bindings
    INTERPRETER_BINDINGS,         // [s-c] interpreter bindings

    GET_INTERPRETER_SETTINGS,     // [c-s] get interpreter settings
    INTERPRETER_SETTINGS,         // [s-c] interpreter settings
    ERROR_INFO,                   // [s-c] error information to be sent
    SESSION_LOGOUT,               // [s-c] error information to be sent
    WATCHER,                      // [s-c] Change websocket to watcher mode.
    PARAGRAPH_ADDED,              // [s-c] paragraph is added
    PARAGRAPH_REMOVED,            // [s-c] paragraph deleted
    PARAGRAPH_MOVED,              // [s-c] paragraph moved
    NOTE_UPDATED,                 // [s-c] paragraph updated(name, config)
    RUN_ALL_PARAGRAPHS,           // [c-s] run all paragraphs
    PARAGRAPH_EXECUTED_BY_SPELL,  // [c-s] paragraph was executed by spell
    RUN_PARAGRAPH_USING_SPELL,    // [s-c] run paragraph using spell
    PARAS_INFO,                   // [s-c] paragraph runtime infos
    SAVE_NOTE_FORMS,              // save note forms
    REMOVE_NOTE_FORMS,            // remove note forms
    INTERPRETER_INSTALL_STARTED,  // [s-c] start to download an interpreter
    INTERPRETER_INSTALL_RESULT,   // [s-c] Status of an interpreter installation
    COLLABORATIVE_MODE_STATUS,    // [s-c] collaborative mode status
    PATCH_PARAGRAPH,              // [c-s][s-c] patch editor text
    NOTE_RUNNING_STATUS,        // [s-c] sequential run status will be change
    NOTICE,                        // [s-c] Notice
    SERVER_SHUTDOWN             // Server shutdown says bye
  }

  // these messages will be ignored during the sequential run of the note
  private static final Set<OP> disabledForRunningNoteMessages = Collections
      .unmodifiableSet(new HashSet<>(Arrays.asList(
          OP.COMMIT_PARAGRAPH,
          OP.RUN_PARAGRAPH,
          OP.RUN_PARAGRAPH_USING_SPELL,
          OP.RUN_ALL_PARAGRAPHS,
          OP.PARAGRAPH_CLEAR_OUTPUT,
          OP.PARAGRAPH_CLEAR_ALL_OUTPUT,
          OP.INSERT_PARAGRAPH,
          OP.MOVE_PARAGRAPH,
          OP.COPY_PARAGRAPH,
          OP.PARAGRAPH_REMOVE,
          OP.MOVE_NOTE_TO_TRASH,
          OP.DEL_NOTE,
          OP.PATCH_PARAGRAPH)));

  private static final Gson GSON = new Gson();
  public static final Message EMPTY = new Message(null);

  public OP op;
  public Map<String, Object> data = new HashMap<>();
  public String ticket = "anonymous";
  public String principal = "anonymous";
  public String roles = "";

  // Unique id generated from client side. to identify message.
  // When message from server is response to the client request
  // includes the msgId in response message, client can pair request and response message.
  // When server send message that is not response to the client request, set null;
  public String msgId = MSG_ID_NOT_DEFINED;
  public static String MSG_ID_NOT_DEFINED = null;

  public Message(OP op) {
    this.op = op;
  }

  public Message withMsgId(String msgId) {
    this.msgId = msgId;
    return this;
  }

  public Message put(String k, Object v) {
    data.put(k, v);
    return this;
  }

  public Object get(String k) {
    return data.get(k);
  }

  public static boolean isDisabledForRunningNotes(OP eventType) {
    return disabledForRunningNoteMessages.contains(eventType);
  }

  public <T> T getType(String key) {
    return (T) data.get(key);
  }

  public <T> T getType(String key, Logger log) {
    try {
      return getType(key);
    } catch (ClassCastException e) {
      log.error("Failed to get {} from message (Invalid type). ", key , e);
      return null;
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Message{");
    sb.append("data=").append(data);
    sb.append(", op=").append(op);
    sb.append('}');
    return sb.toString();
  }

  @Override
  public String toJson() {
    return GSON.toJson(this);
  }

  public static Message fromJson(String json) {
    return GSON.fromJson(json, Message.class);
  }
}
