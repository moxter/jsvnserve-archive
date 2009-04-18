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
import java.util.Map;

import com.googlecode.jsvnserve.SVNSessionStreams;
import com.googlecode.jsvnserve.api.properties.Properties;
import com.googlecode.jsvnserve.element.ListElement;
import com.googlecode.jsvnserve.element.WordElement.Word;
import com.googlecode.jsvnserve.util.Timestamp;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public abstract class AbstractDelta
        extends Properties
{
    /**
     * Serial version UID of this delta class.
     */
    private static final long serialVersionUID = -3022875288811307279L;

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

    AbstractDelta(final String _token,
                  final String _path,
                  final String _copiedPath,
                  final Long _copiedRevision)
    {
        this.token = _token;
        this.path = _path;
        this.copiedPath = _copiedPath;
        this.copiedRevision = _copiedRevision;
    }

    /**
     * Method is used to close an opened handler.
     */
    protected void close()
    {
    }

    /**
     *
     * @param _targetRevision   target revision for which the the SVN editor
     *                          commands must be written
     * @param _streams          session stream with the output streams
     * @param _parentToken      token of the parent directory
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    protected abstract void writeOpen(final long _targetRevision,
                                      final SVNSessionStreams _streams,
                                      final String _parentToken)
            throws UnsupportedEncodingException, IOException;

    protected abstract void writeClose(final SVNSessionStreams _streams)
            throws UnsupportedEncodingException, IOException;

    protected void writeAllProperties(final SVNSessionStreams _streams,
                                      final Word _word)
            throws UnsupportedEncodingException, IOException
    {
        this.writeProperty(_streams,
                           _word,
                           PropertyKey.ENTRY_REPOSITORY_UUID.getSVNKey(),
                           _streams.getSession().getRepository().getUUID().toString());
        for (final Map.Entry<String,String> propEntry : this.entrySet())  {
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

    public void setLastAuthor(final String _author)
    {
        this.put(PropertyKey.ENTRY_DIR_ENTRY_AUTHOR, _author);
    }

    public String getLastAuthor()
    {
        return this.get(PropertyKey.ENTRY_DIR_ENTRY_AUTHOR);
    }

    public void setCommittedRevision(final Long _revision)
    {
        this.put(PropertyKey.ENTRY_DIR_ENTRY_REVISION,
                 (_revision != null) ? String.valueOf(_revision) : null);
    }

    public Long getCommittedRevision()
    {
        final String revStr = this.get(PropertyKey.ENTRY_DIR_ENTRY_REVISION);
        return (revStr == null) ? null : Long.valueOf(revStr);
    }

    public void setCommittedDate(final Timestamp _date)
    {
        this.put(PropertyKey.ENTRY_DIR_ENTRY_DATE,
                 (_date != null) ? _date.toString() : null);
    }

    public Timestamp getCommittedDate()
    {
        final String dateStr = this.get(PropertyKey.ENTRY_DIR_ENTRY_DATE);
        return (dateStr == null)
               ? null
               : Timestamp.valueOf(dateStr);
    }
}