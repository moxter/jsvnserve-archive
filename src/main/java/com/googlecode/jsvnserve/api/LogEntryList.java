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

package com.googlecode.jsvnserve.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.googlecode.jsvnserve.element.WordElement.Word;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class LogEntryList
{
    /**
     * Holds all log entries depending on the revision number.
     */
    private final Map<Long,LogEntry> logEntries = new TreeMap<Long, LogEntry>();

    public Collection<LogEntry> getLogEntries()
    {
        return this.logEntries.values();
    }

    public LogEntry addLogEntry(final long _revision,
                                final String _author,
                                final Date _modified,
                                final String _comment)
    {
        final LogEntry ret = new LogEntry(_revision,
                                          _author,
                                          _modified,
                                          _comment);
        this.logEntries.put(_revision, ret);
        return ret;
    }

    public class LogEntry
    {
        private final List<ChangedPath> changedPaths = new ArrayList<ChangedPath>();

        private final long revision;

        private final String author;

        private final Date modified;

        private final String comment;

        public LogEntry(final long _revision,
                        final String _author,
                        final Date _modified,
                        final String _comment)
        {
            this.revision = _revision;
            this.author = _author;
            this.modified = _modified;
            this.comment = _comment;
        }

        public LogEntry addCopied(final String _name,
                                  final String _copiedFromPath,
                                  final long _copiedFromRevision)
        {
            this.changedPaths.add(new ChangedPath(_name,
                                                  Word.LOG_KIND_ADDED,
                                                  _copiedFromPath,
                                                  _copiedFromRevision));
            return this;
        }

        public LogEntry addDeleted(final String _name)
        {
            this.changedPaths.add(new ChangedPath(_name,
                                                  Word.LOG_KIND_DELETED,
                                                  null,
                                                  0));
            return this;
        }

        public LogEntry addLogCreated(final String _name)
        {
            this.changedPaths.add(new ChangedPath(_name,
                                                  Word.LOG_KIND_ADDED,
                                                  null,
                                                  0));
            return this;
        }

        public LogEntry addModified(final String _name)
        {
            this.changedPaths.add(new ChangedPath(_name,
                                                  Word.LOG_KIND_MODIFIED,
                                                  null,
                                                  0));
            return this;
        }

        public LogEntry addRead(final String _name)
        {
            this.changedPaths.add(new ChangedPath(_name,
                                                  Word.LOG_KIND_READ,
                                                  null,
                                                  0));
            return this;
        }

        public LogEntry addRenamed(final String _name, final String _oldName)
        {
            this.addCopied(_name, _oldName, this.revision);
            this.addDeleted(_oldName);
            return this;
        }

        public long getRevision()
        {
            return this.revision;
        }

        public String getAuthor()
        {
            return this.author;
        }

        public Date getModified()
        {
            return this.modified;
        }

        public String getComment()
        {
            return this.comment;
        }

        public List<ChangedPath> getChangedPaths()
        {
            return changedPaths;
        }
    }

    public class ChangedPath
    {
        private final String path;
        private final String copiedFromPath;
        private final long copiedFromRevision;
        private final Word kind;

        private ChangedPath(final String _path,
                            final Word _kind,
                            final String _copiedFromPath,
                            final long _copiedFromRevision)
        {
            this.path = _path;
            this.kind = _kind;
            this.copiedFromPath = _copiedFromPath;
            this.copiedFromRevision = _copiedFromRevision;
        }

        public String getCopiedFromPath()
        {
            return this.copiedFromPath;
        }
        public long getCopiedFromRevision()
        {
            return this.copiedFromRevision;
        }
        public Word getKind()
        {
            return this.kind;
        }
        public String getPath()
        {
            return this.path;
        }

    }
}
