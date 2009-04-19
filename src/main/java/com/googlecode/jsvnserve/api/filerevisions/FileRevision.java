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

package com.googlecode.jsvnserve.api.filerevisions;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.googlecode.jsvnserve.SVNSessionStreams;
import com.googlecode.jsvnserve.SVNSessionStreams.DeltaOutputStream;
import com.googlecode.jsvnserve.api.properties.Properties;
import com.googlecode.jsvnserve.api.properties.RevisionPropertyValues;
import com.googlecode.jsvnserve.element.ListElement;
import com.googlecode.jsvnserve.element.WordElement.Word;

/**
 * The class represents information about a specific file revision depending on
 * another file revision.
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class FileRevision
{
    /**
     * Path of the file.
     *
     * @see #getPath()
     */
    private final String path;

    /**
     * Revision of the file.
     *
     * @see #getRevision()
     */
    private final long revision;

    /**
     * Stores information whether the content of the file was modified with
     * this file revision.
     *
     * @see #isContentModified()
     */
    private final boolean contentModified;

    /**
     * Stores information whether this file revision is the result of a merge.
     *
     * @see #isResultOfMerge()
     */
    private final boolean resultOfMerge;

    /**
     * Properties of the revision (including author, log message, etc.).
     *
     * @see #getRevisionProperties()
     */
    private final RevisionPropertyValues revisionProperties = new RevisionPropertyValues();

    /**
     * Delta properties depending on the previous file revision.
     *
     * @see #getFileDeltaProperties()
     */
    private final Properties fileDeltaProperties = new Properties();

    /**
     * Default constructor.
     *
     * @param _revision         revision of the file
     * @param _path             path of the file
     * @param _contentModified  must be <i>true</i> if the file content itself
     *                          was modified
     * @param _resultOfMerge    must be <i>true</i> if the file with
     *                          <code>_revision</code> is the result of a merge
     * @see #revision
     * @see #path
     * @see #contentModified
     * @see #resultOfMerge
     */
    public FileRevision(final long _revision,
                        final String _path,
                        final boolean _contentModified,
                        final boolean _resultOfMerge)
    {
        this.revision = _revision;
        this.path = _path;
        this.contentModified = _contentModified;
        this.resultOfMerge = _resultOfMerge;
    }

    /**
     *
     * @param _streams      session streams with output stream
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    protected void writeInfo(final SVNSessionStreams _streams)
            throws UnsupportedEncodingException, IOException
    {
        // revision properties
        final ListElement revList = new ListElement();
        for (final Map.Entry<String,String> rev : this.revisionProperties.entrySet())  {
            revList.add(new ListElement(rev.getKey(), rev.getValue()));
        }
        // file properties (only delta)
        final ListElement fileList = new ListElement();
        for (final Map.Entry<String,String> rev : this.fileDeltaProperties.entrySet())  {
            fileList.add(new ListElement(rev.getKey(), new ListElement(rev.getValue())));
        }

        _streams.writeItemList(new ListElement(
                this.path,
                this.revision,
                revList,
                fileList,
                this.resultOfMerge ? Word.BOOLEAN_TRUE : Word.BOOLEAN_FALSE));
    }

    /**
     *
     * @param _streams
     * @param _previousRev
     */
    protected void writeFile(final SVNSessionStreams _streams,
                             final FileRevision _previousRev)
    {
        _streams.writeFileDelta(
                (_previousRev == null)
                        ? null
                        : _streams.getSession().getRepository().getFile(_previousRev.revision, _previousRev.path),
                _streams.getSession().getRepository().getFile(this.revision, this.path),
                new DeltaOutputStream() {
                    @Override
                    public void write(final byte _bytes[],
                                      final int _offset,
                                      final int _len)
                    throws IOException
                    {
                        _streams.traceWrite("{}:... ", _len);
                        _streams.writeWithoutFlush(String.valueOf(_len));
                        _streams.writeWithoutFlush(':');
                        _streams.writeWithoutFlush(_bytes, _offset, _len);
                        _streams.writeWithoutFlush(' ');
                    }
                },
                false);
    }

    /**
     * @return path of the file
     * @see #path
     */
    public String getPath()
    {
        return this.path;
    }

    /**
     * @return revision of the file
     * @see #revision
     */
    public long getRevision()
    {
        return this.revision;
    }

    /**
     * Returns whether the content of this file revision was modified.
     *
     * @return <i>true</i> if the file content was modified with this file
     *         revision; otherwise <i>false</i>
     * @see #contentModified
     */
    public boolean isContentModified()
    {
        return this.contentModified;
    }

    /**
     * Returns whether this file revision is the result of a merge.
     *
     * @return <i>true</i> if this file revision is the result of a merge;
     *         otherwise <i>false</i>
     * @see #resultOfMerge
     */
    public boolean isResultOfMerge()
    {
        return resultOfMerge;
    }

    /**
     * @return revision properties
     * @see #revisionProperties
     */
    public RevisionPropertyValues getRevisionProperties()
    {
        return this.revisionProperties;
    }

    /**
     * @return file delta properties
     * @see #fileDeltaProperties
     */
    public Properties getFileDeltaProperties()
    {
        return this.fileDeltaProperties;
    }

    /**
     * String representation which includes all instance variables of this
     * class.
     *
     * @return string representation of this file revision
     * @see #revision
     * @see #path
     * @see #contentModified
     * @see #resultOfMerge
     * @see #revisionProperties
     * @see #fileDeltaProperties
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                .append("revision", this.revision)
                .append("path", this.path)
                .append("content is modified", this.contentModified)
                .append("result of merge", this.resultOfMerge)
                .append("revision properties", this.revisionProperties)
                .append("file delta properties", this.fileDeltaProperties)
                .toString();
    }
}
