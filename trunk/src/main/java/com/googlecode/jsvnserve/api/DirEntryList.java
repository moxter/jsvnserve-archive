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

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.googlecode.jsvnserve.util.Timestamp;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class DirEntryList
{
    private final Map<String,DirEntry> entries = new TreeMap<String,DirEntry>();

    public DirEntryList()
    {
    }

    /**
     *
     * @return all directory entries of current directory
     */
    public Collection<DirEntry> getEntries()
    {
        return this.entries.values();
    }

    /**
     *
     * @param _name     name of the directory
     * @param _revision last committed revision
     * @param _date     last committed date
     * @param _author   last committed author
     */
    public void addDirectory(final String _name,
                             final Long _revision,
                             final Timestamp _date,
                             final String _author)
    {
        this.entries.put(_name, DirEntry.createDirectory(_name,
                                                         _revision,
                                                         _date,
                                                         _author));
    }

    /**
     *
     * @param _name     name of the file
     * @param _revision last committed revision
     * @param _date     last committed date
     * @param _author   last committed author
     * @param _fileSize last committed file size
     */
    public void addFile(final String _name,
                        final Long _revision,
                        final Timestamp _date,
                        final String _author,
                        final int _fileSize)
    {
        this.entries.put(_name, DirEntry.createFile(_name,
                                                    _revision,
                                                    _date,
                                                    _author,
                                                    _fileSize,
                                                    null));
    }
}
