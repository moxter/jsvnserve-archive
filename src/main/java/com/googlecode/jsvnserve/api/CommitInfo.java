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

import java.util.Map;

import com.googlecode.jsvnserve.api.editorcommands.EditorCommandSet;
import com.googlecode.jsvnserve.util.Timestamp;

/**
 * Holds the log information about a commit.
 *
 * @see IRepository#commit(String, Map, boolean, com.googlecode.jsvnserve.api.properties.Properties, EditorCommandSet)
 * @author jSVNServe Team
 * @version $Id$
 */
public class CommitInfo
{
    /**
     * Revision of the commit.
     */
    private final long revision;

    /**
     * Author of the commit.
     */
    private final String author;

    /**
     * Date of the commit.
     */
    private final Timestamp date;

    /**
     *
     * @param _revision revision of the commit
     * @param _author   author of the commit
     * @param _date     date of the commit
     */
    public CommitInfo(final long _revision,
                      final String _author,
                      final Timestamp _date)
    {
        this.revision = _revision;
        this.author = _author;
        this.date = _date;
    }

    /**
     *
     * @return commit revision
     * @see #revision
     */
    public long getRevision()
    {
        return this.revision;
    }

    /**
     *
     * @return author of commit
     * @see #author
     */
    public String getAuthor()
    {
        return this.author;
    }

    /**
     *
     * @return date of commit
     * @see #date
     */
    public Timestamp getDate()
    {
        return this.date;
    }
}
