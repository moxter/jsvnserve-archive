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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.googlecode.jsvnserve.element.WordElement.Word;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public final class DirEntry
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
     * Revision where this directory entry is committed.
     */
    private final Long revision;

    /**
     * Committed modified date time of this directory entry.
     */
    private final Date date;

    /**
     * Author of this directory entry.
     */
    private final String author;

    /**
     * Size of a file content.
     */
    private final int fileSize;

    /**
     * MD5 hash of a file content.
     */
    private final String fileMD5;

    /**
     * Holds the properties of a directory entry.
     */
    final Map<String,String> properties = new HashMap<String,String>();

    private DirEntry(final String _name,
                     final Word _kind,
                     final Long _revision,
                     final Date _date,
                     final String _author,
                     final int _fileSize,
                     final String _fileMD5)
    {
        this.name = _name;
        this.kind = _kind;
        this.revision = _revision;
        this.date = _date;
        this.author = _author;
        this.fileSize = _fileSize;
        this.fileMD5 = _fileMD5;
    }

    public static DirEntry createDirectory(final String _name,
                                           final Long _revision,
                                           final Date _modified,
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
                                      final Date _modified,
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

    /**
     * Adds a new property to this directory instance.
     *
     * @param _key      key name of the property
     * @param _value    value of the property
     * @return this directory entry instance
     */
    public DirEntry addProperty(final String _key,
                                final String _value)
    {
        this.properties.put(_key, _value);
        return this;
    }

    public String getName()
    {
        return this.name;
    }

    public Word getKind()
    {
        return this.kind;
    }

    public long getRevision()
    {
        return this.revision;
    }

    public Date getDate()
    {
        return this.date;
    }

    public String getAuthor()
    {
        return this.author;
    }

    public int getFileSize()
    {
        return this.fileSize;
    }

    public String getFileMD5()
    {
        return this.fileMD5;
    }

    public Map<String, String> getProperties()
    {
        return this.properties;
    }
}