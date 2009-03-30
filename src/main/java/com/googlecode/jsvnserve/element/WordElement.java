/*
 * Copyright 2009 The jSVNServe Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package com.googlecode.jsvnserve.element;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class WordElement
            extends AbstractElement<WordElement.Word>
{
    public enum Word
    {
        // standard
        STATUS_SUCCESS("success"),
        STATUS_FAILURE("failure"),
        UNKNOWN("unknown"),

        // braces
        BRACE_OPEN("("),
        BRACE_CLOSE(")"),

        // Capabilities
        /**
         * Every released version of Subversion since 1.0 announces the
         * edit-pipeline capability; starting in Subversion 1.5, both client
         * and server &quot;require&qoutM the other side to announce
         * edit-pipeline.
         */
        EDIT_PIPELINE("edit-pipeline"),

        /**
         * If both the client and server support svndiff version 1, this will
         * be used as the on-the-wire format for svndiff instead of svndiff
         * version 0.
         *
         * @see <a href="http://svn.collab.net/repos/svn/trunk/notes/svndiff">SVN Diff spezification</a>
         */
        SVNDIFF1("svndiff1"),

        /**
         * If the remote end announces support for this capability, it will
         * accept the absent-dir and absent-file editor commands.
         */
        ABSENT_ENTRIES("absent-entries"),

        /**
         * If the server presents this capability, it supports the rev-props
         * parameter of the commit command.
         */
        COMMIT_REVPRODS("commit-revprops"),

        /**
         * If the server presents this capability, it supports the
         * get-mergeinfo command.
         */
        MERGEINFO("mergeinfo"),

        /**
         * If the server presents this capability, it understands requested
         * operational depth and per-path ambient depth.
         */
        DEPTH("depth"),
        LOG_REVPROPS("log-revprops"),

        // node kinds
        NODE_KIND_NONE("none"),
        NODE_KIND_FILE("file"),
        NODE_KIND_DIR("dir"),

        // log kinds
        LOG_KIND_ADDED("A"),
        LOG_KIND_DELETED("D"),
        LOG_KIND_MODIFIED("M"),
        LOG_KIND_READ("R"),

        // dirent-fields
        LOG_DIRENT_KIND("kind"),
        LOG_DIRENT_SIZE("size"),
        LOG_DIRENT_HAS_PROPS("has-props"),
        LOG_DIRENT_CREATED_REV("created-rev"),
        LOG_DIRENT_TIME("time"),
        LOG_DIRENT_LAST_AUTHOR("last-author"),

        // boolean values
        BOOLEAN_TRUE("true"),
        BOOLEAN_FALSE("false"),

        // depth
        DEPTH_EXCLUDE("exclude"),
        DEPTH_EMPTY("empty"),
        DEPTH_FILES("files"),
        DEPTH_IMMEDIATES("immediates"),
        DEPTH_INFINITY("infinity"),

        // Main Command Set
        REPARENT("reparent"),
        GET_LATEST_REV("get-latest-rev"),
        GET_DATED_REV("get-dated-rev"),
        CHANGE_REV_PROP("change-rev-prop"),
        REV_PROPLIST("rev-proplist"),
        REV_PROP("rev-prop"),
        COMMIT("commit"),
        GET_FILE("get-file"),
        GET_DIR("get-dir"),
        CHECK_PATH("check-path"),
        STAT("stat"),
        GET_MERGEINFO("get-mergeinfo"),
        UPDATE("update"),
        SWITCH("switch"),
        STATUS("status"),
        DIFF("diff"),
        LOG("log"),
        GET_LOCATIONS("get-locations"),
        GET_LOCATION_SEGMENTS("get-location-segments"),
        GET_FILE_REVS("get-file-revs"),
        LOCK("lock"),
        LOCK_MANY("lock-many"),
        UNLOCK("unlock"),
        UNLOCK_MANY("unlock-many"),
        GET_LOCK("get-lock"),
        GET_LOCKS("get-locks"),
        REPLAY("replay"),
        REPLAY_RANGE("replay-range"),
        GET_DELETED_REV("get-deleted-rev"),

        // Editor Command Set
        TARGET_REV("target-rev"),
        OPEN_ROOT("open-root"),
        DELETE_ENTRY("delete-entry"),
        ADD_DIR("add-dir"),
        OPEN_DIR("open-dir"),
        CHANGE_DIR_PROP("change-dir-prop"),
        CLOSE_DIR("close-dir"),
        ABSENT_DIR("absent-dir"),
        ADD_FILE("add-file"),
        OPEN_FILE("open-file"),
        APPLY_TEXTDELTA("apply-textdelta"),
        TEXTDELTA_CHUNK("textdelta-chunk"),
        TEXTDELTA_END("textdelta-end"),
        CHANGE_FILE_PROP("change-file-prop"),
        CLOSE_FILE("close-file"),
        ABSENT_FILE("absent-file"),
        CLOSE_EDIT("close-edit"),
        ABORT_EDIT("abort-edit"),
        FINISH_REPLAY("finish-replay"),

        // Report Command Set
        SET_PATH("set-path"),
        DELETE_PATH("delete-path"),
        LINK_PATH("link-path"),
        FINISH_REPORT("finish-report"),
        ABORT_REPORT("abort-report");

        public final String value;


        private Word(final String _value)
        {
            this.value = _value;
            WordElement.MAP.put(this.value, this);
        }

        public static Word wordByValue(final String _value)
        {
            return WordElement.MAP.get(_value);
        }
    }

    private static final Map<String,Word> MAP = new HashMap<String,Word>();

    final String origValue;


    public WordElement(final CharSequence _value)
    {
        super(WordElement.Word.wordByValue(_value.toString()));
        this.origValue = _value.toString();
    }

    public WordElement(final WordElement.Word _value)
    {
        super(_value);
        this.origValue = _value.value;
    }

    @Override
    public void write(final OutputStream _out)
            throws UnsupportedEncodingException, IOException
    {
        _out.write(this.origValue.getBytes("UTF8"));
        _out.write(' ');
    }

    @Override
    public WordElement.Word getWord()
    {
        return this.getValue();
    }

    /**
     * If a string of a word must be returned, the original value defined in
     * {@link #origValue} is returned.
     *
     * @return original string value
     * @see #origValue
     */
    @Override
    public String getString()
    {
        return this.origValue;
    }

    @Override
    public String toString()
    {
        return "WORD '" + this.getValue() + "' (original '" + this.origValue + "')";
    }
}