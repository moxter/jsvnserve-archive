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

import com.googlecode.jsvnserve.SVNSessionStreams;

/**
 * Class for the delta to open a file.
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class DeltaFileOpen
        extends AbstractDeltaFile
{
    /**
     * Serial version UID of this delta class.
     */
    private static final long serialVersionUID = 4917399931185488168L;

    /**
     * Holds the revision of the file.
     *
     * @see #getRevision()
     */
    private final long revision;

    /**
     *
     * @param _token    file token
     * @param _path     file path
     * @param _revision file revision
     */
    DeltaFileOpen(final String _token,
                  final String _path,
                  final long _revision)
    {
        super(_token, _path, null, null);
        this.revision = _revision;
    }

    /**
     * Returns the revision of the file.
     *
     * @return revision of the file
     * @see #revision
     */
    public long getRevision()
    {
        return this.revision;
    }

    /**
     *
     * @param _targetRevision   target revision for which the the SVN editor
     *                          commands must be written (not used)
     * @param _streams
     * @param _parentToken      token of the parent directory
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    @Override
    protected void writeOpen(final long _targetRevision,
                             final SVNSessionStreams _streams,
                             final String _parentToken)
            throws UnsupportedEncodingException, IOException
    {
        // TODO: only stub!
    }

    @Override
    protected void writeClose(final SVNSessionStreams _streams)
    {
        // TODO: only stub!
    }
}
