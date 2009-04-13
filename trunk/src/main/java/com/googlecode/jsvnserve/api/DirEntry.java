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

import java.util.HashMap;
import java.util.Map;

import com.googlecode.jsvnserve.api.properties.Properties;
import com.googlecode.jsvnserve.element.WordElement.Word;
import com.googlecode.jsvnserve.util.Timestamp;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public final class DirEntry
        extends Properties
{
    /**
     * Name of the directory entry.
     */
    private final String name;

    /**
     * Kind of the directory entry. Used to answer if the directory entry
     * is itself a directory or file.
     */
    private final Word kind;

    /**
     * Size of a file content.
     */
    private final int fileSize;

    /**
     * Holds the properties of a directory entry.
     */
    final Map<String,String> properties = new HashMap<String,String>();

    private DirEntry(final String _name,
                     final Word _kind,
                     final Long _revision,
                     final Timestamp _date,
                     final String _author,
                     final int _fileSize,
                     final String _fileMD5)
    {
        this.name = _name;
        this.kind = _kind;
        this.fileSize = _fileSize;
        if (_fileMD5 != null)  {
            this.put(PropertyKey.ENTRY_DIR_ENTRY_CHECKSUM, _fileMD5);
        }
        if (_revision != null)  {
            this.put(PropertyKey.ENTRY_DIR_ENTRY_REVISION, String.valueOf(_revision));
        }
        if (_date != null)  {
            this.put(PropertyKey.ENTRY_DIR_ENTRY_DATE, _date.toString());
        }
        if (_author != null)  {
            this.put(PropertyKey.ENTRY_DIR_ENTRY_AUTHOR, _author);
        }
    }

    public static DirEntry createDirectory(final String _name,
                                           final Long _revision,
                                           final Timestamp _modified,
                                           final String _author)
    {
        return new DirEntry(_name,
                            Word.NODE_KIND_DIR,
                            _revision,
                            _modified,
                            _author,
                            0,
                            null);
    }

    public static DirEntry createFile(final String _name,
                                      final Long _revision,
                                      final Timestamp _modified,
                                      final String _author,
                                      final int _fileSize,
                                      final String _fileMD5)
    {
        return new DirEntry(_name,
                            Word.NODE_KIND_FILE,
                            _revision,
                            _modified,
                            _author,
                            _fileSize,
                            _fileMD5);
    }

    public String getName()
    {
        return this.name;
    }

    public Word getKind()
    {
        return this.kind;
    }

    public int getFileSize()
    {
        return this.fileSize;
    }

    public Long getRevision()
    {
        final String revision = this.get(PropertyKey.ENTRY_DIR_ENTRY_REVISION);
        return (revision == null) ? null : Long.parseLong(revision);
    }

    public Timestamp getDate()
    {
        return Timestamp.valueOf(this.get(PropertyKey.ENTRY_DIR_ENTRY_DATE));
    }

    public String getAuthor()
    {
        return this.get(PropertyKey.ENTRY_DIR_ENTRY_AUTHOR);
    }

    public String getFileMD5()
    {
        return this.get(PropertyKey.ENTRY_DIR_ENTRY_CHECKSUM);
    }
}