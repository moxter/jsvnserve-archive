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
import java.util.TreeMap;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.googlecode.jsvnserve.SVNSessionStreams;
import com.googlecode.jsvnserve.api.IRepository;

/**
 * The class is used to hold a list of file revisions.
 *
 * @author jSVNServe Team
 * @version $Id$
 * @see IRepository#getFileRevs(String, long, long, boolean)
 */
public class FileRevisionsList
{
    /**
     * Serial version UID of this class.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Holds all sorted file revisions depending the revision itself.
     */
    private final Map<Long,FileRevision> revisions = new TreeMap<Long,FileRevision>();

    /**
     * Adds a new file revision instance to current revisions.
     *
     * @param _fileRevision     file revision to add
     * @see #revisions
     */
    public void add(final FileRevision _fileRevision)
    {
        this.revisions.put(_fileRevision.getRevision(), _fileRevision);
    }

    /**
     * <p>Writes the list of file revisions into the SVN session stream to the
     * SVN client.</p>
     * <p>Each interesting file revision is written. First the common
     * information like revision properties, file path etc. with
     * {@link FileRevision#writeInfo(SVNSessionStreams)} and then the delta
     * file content. For the first interesting revision the file contents will
     * be written as a text delta against an empty file. For the following
     * revisions, the delta will be against the full-text contents of the
     * previous revision (if in this file revision the content was modified).
     * </p>
     *
     * @param _streams      SVN server session streams
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @see FileRevision#writeInfo(SVNSessionStreams)
     * @see FileRevision#writeFile(SVNSessionStreams, FileRevision)
     */
    public void write(final SVNSessionStreams _streams)
            throws UnsupportedEncodingException, IOException
    {
        FileRevision prev = null;

        for (final FileRevision fileRev : this.revisions.values())  {
            fileRev.writeInfo(_streams);

            if (fileRev.isContentModified() || prev == null)  {
                fileRev.writeFile(_streams, prev);
                prev = fileRev;
            }

            _streams.write("0: ");
        }

        _streams.write("done ");
    }

    /**
     * Returns the string representation of this file revision list. This
     * includes only the list of all file revisions instances.
     *
     * @return string representation
     * @see FileRevision#toString()
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                .appendToString(this.revisions.values().toString())
                .toString();
    }
}
