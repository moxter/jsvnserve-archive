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

package com.googlecode.jsvnserve.api.editorcommands;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import com.googlecode.jsvnserve.SVNSessionStreams;
import com.googlecode.jsvnserve.api.properties.Properties.PropertyKey;
import com.googlecode.jsvnserve.element.ListElement;
import com.googlecode.jsvnserve.element.WordElement.Word;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public abstract class AbstractDelta
{
    /**
     * Current token of the delta.
     *
     * @see #getToken()
     */
    private final String token;

    /**
     * Path of the delta.
     *
     * @see #getPath()
     */
    private final String path;

    /**
     * Name of the original copied path. If <code>null</code> the new
     * directory / file was not copied. The copied path is absolute to the
     * repository path (and starts always with '/').
     */
    private final String copiedPath;

    /**
     * Revision of the copied path. If <code>null</code> the new directory /
     * file was not copied.
     */
    private final Long copiedRevision;

    /**
     * Last author.
     */
    private final String lastAuthor;

    /**
     * Committed revision.
     *
     * @see #getCommittedRevision()
     */
    private final Long committedRevision;

    /**
     * Committed date.
     *
     * @see #getCommittedDate()
     */
    private final Date committedDate;

    private final Map<String,String> properties = new TreeMap<String,String>();

    AbstractDelta(final String _token,
                  final String _path,
                  final String _copiedPath,
                  final Long _copiedRevision,
                  final String _lastAuthor,
                  final Long _committedRevision,
                  final Date _committedDate)
    {
        this.token = _token;
        this.path = _path;
        this.copiedPath = _copiedPath;
        this.copiedRevision = _copiedRevision;
        this.lastAuthor = _lastAuthor;
        this.committedRevision = _committedRevision;
        this.committedDate = _committedDate;
    }

    /**
     * Method is used to close an opened handler.
     */
    protected void close()
    {
    }

    protected abstract void writeOpen(final SVNSessionStreams _streams,
                                      final String _parentToken)
            throws UnsupportedEncodingException, IOException;

    protected abstract void writeClose(final SVNSessionStreams _streams)
            throws UnsupportedEncodingException, IOException;

    protected void writeAllProperties(final SVNSessionStreams _streams,
                                      final Word _word)
            throws UnsupportedEncodingException, IOException
    {
        if (this.getCommittedRevision() != null)  {
            this.writeProperty(_streams,
                               _word,
                               PropertyKey.ENTRY_DIR_ENTRY_REVISION.getSVNKey(),
                               String.valueOf(this.getCommittedRevision()));
        }
        if (this.getCommittedDate() != null)  {
            this.writeProperty(_streams,
                               _word,
                               PropertyKey.ENTRY_DIR_ENTRY_DATE.getSVNKey(),
                               this.getCommittedDate());
        }
        this.writeProperty(_streams,
                           _word,
                           PropertyKey.ENTRY_REPOSITORY_UUID.getSVNKey(),
                           _streams.getSession().getRepository().getUUID().toString());
        if (this.getLastAuthor() != null)  {
            this.writeProperty(_streams,
                               _word,
                               PropertyKey.ENTRY_DIR_ENTRY_AUTHOR.getSVNKey(),
                               this.getLastAuthor());
        }
        for (final Map.Entry<String,String> propEntry : this.getProperties().entrySet())  {
            this.writeProperty(_streams, _word, propEntry.getKey(), propEntry.getValue());
        }
    }

    protected void writeProperty(final SVNSessionStreams _streams,
                                 final Word _word,
                                 final String _key,
                                 final Object _value)
            throws UnsupportedEncodingException, IOException
    {
        _streams.writeItemList(new ListElement(_word,
                                               new ListElement(this.getToken(), _key, new ListElement(_value))));

    }

    /**
     * Adds a new property.
     *
     * @param _key      key of the property
     * @param _value    value of the property
     */
    public void addProperty(final String _key, final String _value)
    {
        this.properties.put(_key, _value);
    }

    public Map<String, String> getProperties()
    {
        return this.properties;
    }

    public String getToken()
    {
        return this.token;
    }

    public String getPath()
    {
        return this.path;
    }

    public String getCopiedPath()
    {
        return this.copiedPath;
    }

    public Long getCopiedRevision()
    {
        return this.copiedRevision;
    }

    public String getLastAuthor()
    {
        return this.lastAuthor;
    }

    public Long getCommittedRevision()
    {
        return this.committedRevision;
    }

    public Date getCommittedDate()
    {
        return this.committedDate;
    }
}