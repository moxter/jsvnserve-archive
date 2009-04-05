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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.io.ISVNDeltaConsumer;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
import org.tmatesoft.svn.core.io.diff.SVNDiffWindow;

import com.googlecode.jsvnserve.SVNSessionStreams;
import com.googlecode.jsvnserve.element.ListElement;
import com.googlecode.jsvnserve.element.WordElement.Word;

/**
 *
 * @see EditorCommandSet#createFile(String, long, Date)
 * @author jSVNServe Team
 * @version $Id$
 */
class DeltaFileCreate
        extends AbstractDelta
{
    DeltaFileCreate(final String _token,
                    final String _path,
                    final String _lastAuthor,
                    final Long _committedRevision,
                    final Date _committedDate)
    {
        super(_token, _path, null, null, _lastAuthor, _committedRevision, _committedDate);
    }

    @Override
    protected void writeOpen(final SVNSessionStreams _streams,
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

        SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
        String md5 = "";
        try {
            md5 = deltaGenerator.sendDelta(this.getToken(),
                    _streams.getSession().getRepository().getFile(this.getCommittedRevision(), this.getPath()),
                    new ISVNDeltaConsumer() {
                        boolean writeHeader = true;

                        public void applyTextDelta(String s, String s1)
                        {
                        }

                        public OutputStream textDeltaChunk(final String s,
                                final SVNDiffWindow svndiffwindow) throws SVNException
                        {

                            OutputStream out = new OutputStream() {
                                @Override
                                public void write(byte b[], int off, int len) throws IOException {
        _streams.traceWrite("( textdelta-chunk ( 2:{} ( {}:... ) ) ) ", DeltaFileCreate.this.getToken(), len);
        _streams.writeWithoutFlush("( textdelta-chunk ( ");
        _streams.writeWithoutFlush(String.valueOf(DeltaFileCreate.this.getToken().length()));
        _streams.writeWithoutFlush(':');
        _streams.writeWithoutFlush(DeltaFileCreate.this.getToken());
        _streams.writeWithoutFlush(' ');
        _streams.writeWithoutFlush(String.valueOf(len));
        _streams.writeWithoutFlush(':');
        _streams.writeWithoutFlush(b, off, len);
        _streams.writeWithoutFlush(" ) ) ");
                                }

                                @Override
                                public void write(byte[] b) throws IOException {
                                    write(b, 0, b.length);
                                }

                                @Override
                                public void write(int b) throws IOException {
                                    write(new byte[]{(byte) (b & 0xFF)});
                                }
                            };

                            try {
                                svndiffwindow.writeTo(out, this.writeHeader, true);
                            } catch (final IOException e) {
                                throw new SVNException(SVNErrorMessage.UNKNOWN_ERROR_MESSAGE, e);
                            }
                            this.writeHeader = false;
                            return null;
                        }

                        public void textDeltaEnd(String s) throws SVNException
                        {
                        }
            },
                    true);
        } catch (SVNException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        _streams.writeItemList(
                new ListElement(Word.TEXTDELTA_END, new ListElement(this.getToken())),
                new ListElement(Word.CLOSE_FILE, new ListElement(this.getToken(), new ListElement(md5))));
    }

    @Override
    protected void writeClose(final SVNSessionStreams _streams)
    {
    }
}