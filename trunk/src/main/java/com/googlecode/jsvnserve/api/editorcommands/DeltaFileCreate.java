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

import com.googlecode.jsvnserve.SVNSessionStreams;
import com.googlecode.jsvnserve.SVNSessionStreams.DeltaOutputStream;
import com.googlecode.jsvnserve.element.ListElement;
import com.googlecode.jsvnserve.element.WordElement.Word;

/**
 *
 * @see EditorCommandSet#createFile(String, long, Date)
 * @author jSVNServe Team
 * @version $Id$
 */
public class DeltaFileCreate
        extends AbstractDeltaFile
{
    /**
     * Serial version UID of this delta class.
     */
    private static final long serialVersionUID = -8348087046520885577L;

    DeltaFileCreate(final String _token,
                    final String _path)
    {
        super(_token, _path, null, null);
    }

    /**
     *
     * @param _targetRevision   target revision for which the the SVN editor
     *                          commands must be written; used for the revision
     *                          of the file (to get the input stream from the
     *                          repository)
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
        _streams.writeItemList(
                new ListElement(Word.ADD_FILE,
                                new ListElement(this.getPath(),
                                                _parentToken,
                                                this.getToken(),
                                                new ListElement())));
        this.writeAllProperties(_streams, Word.CHANGE_FILE_PROP);

        _streams.writeItemList(new ListElement(Word.APPLY_TEXTDELTA, new ListElement(this.getToken(), new ListElement())));

        final String md5 = _streams.writeFileDelta(
                null,
                _streams.getSession().getRepository().getFile(_targetRevision, this.getPath()),
                new DeltaOutputStream() {
                    @Override
                    public void write(final byte _bytes[],
                                      final int _offset,
                                      final int _len)
                    throws IOException
                    {
                        _streams.traceWrite("( textdelta-chunk ( 2:{} ( {}:... ) ) ) ", DeltaFileCreate.this.getToken(), _len);
                        _streams.writeWithoutFlush("( textdelta-chunk ( ");
                        _streams.writeWithoutFlush(String.valueOf(DeltaFileCreate.this.getToken().length()));
                        _streams.writeWithoutFlush(':');
                        _streams.writeWithoutFlush(DeltaFileCreate.this.getToken());
                        _streams.writeWithoutFlush(' ');
                        _streams.writeWithoutFlush(String.valueOf(_len));
                        _streams.writeWithoutFlush(':');
                        _streams.writeWithoutFlush(_bytes, _offset, _len);
                        _streams.writeWithoutFlush(" ) ) ");
                    }
                },
                true);

        _streams.writeItemList(
                new ListElement(Word.TEXTDELTA_END, new ListElement(this.getToken())),
                new ListElement(Word.CLOSE_FILE, new ListElement(this.getToken(), new ListElement(md5))));
    }

    @Override
    protected void writeClose(final SVNSessionStreams _streams)
    {
    }
}