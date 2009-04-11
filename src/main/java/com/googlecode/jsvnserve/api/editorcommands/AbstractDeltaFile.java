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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.delta.SVNDeltaReader;
import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;
import org.tmatesoft.svn.core.io.ISVNDeltaConsumer;
import org.tmatesoft.svn.core.io.diff.SVNDeltaProcessor;
import org.tmatesoft.svn.core.io.diff.SVNDiffWindow;

import com.googlecode.jsvnserve.api.ServerException;

/**
 * Handles all file commands from the editor command set
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public abstract class AbstractDeltaFile
        extends AbstractDelta
{
    /**
     * SVN delta reader.
     */
    private SVNDeltaReader deltaReader;

    private SVNDeltaProcessor deltaProcessor;

    private ISVNDeltaConsumer deltaConsumer;

    /**
     * Temporary file of the file to checkin.
     */
    private File file;

    /**
     * Output stream used for processing text delta's from the SVN client for
     * {@link #file}.
     */
    private OutputStream output;

    /**
     * The input stream is opened from the SVN server for {@link #file}.
     *
     * @see #getInputStream()
     */
    private InputStream in;

    /**
     * MD5 checksum of the {@link #checksum}.
     */
    private String checksum;

    /**
     * Default constructor.
     *
     * @param _token            token of the file
     * @param _path             path of the file
     * @param _copiedPath       copied from file with given path
     * @param _copiedRevision   copied from file with given revision
     * @param _author           committed author of the file
     * @param _revision         committed revision of the file
     * @param _date             committed date of the file
     */
    AbstractDeltaFile(final String _token,
                      final String _path,
                      final String _copiedPath,
                      final Long _copiedRevision,
                      final String _author,
                      final Long _revision,
                      final Date _date)
    {
        super(_token, _path,
              _copiedPath, _copiedRevision,
              _author, _revision, _date);
    }

    protected void applyTextDelta() throws IOException
    {
        this.file = File.createTempFile("DeltaFileCreate", ".tmp");
        this.output = new FileOutputStream(this.file);
        this.deltaProcessor = new SVNDeltaProcessor();
        this.deltaReader = new SVNDeltaReader();
        // simple delta consumer to redirect all text delta chunks to processor
        this.deltaConsumer = new ISVNDeltaConsumer()  {
            public void applyTextDelta(final String _path,
                                       final String _baseChecksum)
            {
            }
            public OutputStream textDeltaChunk(final String _path,
                                               final SVNDiffWindow _svndiffwindow)
                    throws SVNException
            {
                return AbstractDeltaFile.this.deltaProcessor.textDeltaChunk(_svndiffwindow);
            }
            public void textDeltaEnd(final String _path)
            {
            }
        };

        this.deltaProcessor.applyTextDelta(SVNFileUtil.DUMMY_IN,
                                           this.output,
                                           true);

    }

    protected void textDeltaChunk(final byte[] _buffer)
    {
        try {
            this.deltaReader.nextWindow(_buffer, 0, _buffer.length, null, this.deltaConsumer);
        } catch (SVNException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void textDeltaEnd()
    {
        this.checksum = this.deltaProcessor.textDeltaEnd();
        this.deltaReader = null;
        this.deltaProcessor = null;
        this.output = null;
    }

    protected void closeFile(final String _md5)
    {
        System.out.println("have md5: " + this.checksum + "    from SVN client: " + _md5);
    }

    /**
     *
     * @return MD5 checksum of the file
     * @see #checksum
     */
    public String getChecksum()
    {
        return this.checksum;
    }

    /**
     *
     * @return input stream of the complete file
     * @see #in
     */
    public InputStream getInputStream() throws ServerException
    {
        if (this.in != null)  {
// TODO: i18n
throw new ServerException("File was already opened!");
        }
        try {
            this.in = new FileInputStream(this.file);
        } catch (final FileNotFoundException ex) {
// TODO: i18n
throw new ServerException("temporary file '" + this.file + "' not found");
        }
        return this.in;
    }

    @Override
    protected void close()
    {
        if (this.in != null)  {
            try {
                this.in.close();
            } catch (final IOException e) {
            }
        }
        this.file.delete();
    }

}
