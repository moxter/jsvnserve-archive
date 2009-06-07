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
import com.googlecode.jsvnserve.element.WordElement.Word;
import com.googlecode.jsvnserve.util.Server2Client;

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
     * Path of the file on the server. Used to get the input stream which is
     * sent to the client.
     */
    private final String serverPath;

    /**
     *
     * @param _token            file token
     * @param _path             file path (used from the client) which is
     *                          updated (and so the current path...)
     * @param _serverPath       path of the file on the server
     * @param _currentRevision  current file revision which is opened (and
     *                          updated)
     * @param _currentMD5       current MD5 hash of the file which is opened
     *                          (and updated); only if a MD5 exists a file
     *                          will be updated (and not replaced)
     */
    DeltaFileOpen(final String _token,
                  final String _path,
                  final String _serverPath,
                  final long _currentRevision)
    {
        super(_token, _path, _path, _currentRevision);
        this.serverPath = _serverPath;
    }

    /**
     *
     * @param _targetRevision   target revision for which the the SVN editor
     *                          commands must be written (not used)
     * @param _streams          SVN in- and output stream
     * @param _parentToken      token of the parent directory
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    @Override
    @Server2Client
    protected void writeOpen(final long _targetRevision,
                             final SVNSessionStreams _streams,
                             final String _parentToken)
            throws UnsupportedEncodingException, IOException
    {
        this.writeOpen(
                _streams,
                _parentToken,
                Word.OPEN_FILE,
                _streams.getSession().getRepository().getFile(_targetRevision, this.serverPath));
    }

    /**
     * The close tag is already written in
     * {@link #writeOpen(long, SVNSessionStreams, String)} and therefore
     * nothing is done in this method. The close tag could not be written here,
     * because the close tag must also include the MD5 checksum which is
     * calculated file the file stream is written.
     *
     * @param _streams          SVN in- and output stream
     */
    @Override
    @Server2Client
    protected void writeClose(final SVNSessionStreams _streams)
    {
    }
}
