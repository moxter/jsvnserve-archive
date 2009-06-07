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
import java.io.UnsupportedEncodingException;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.delta.SVNDeltaReader;
import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;
import org.tmatesoft.svn.core.io.ISVNDeltaConsumer;
import org.tmatesoft.svn.core.io.diff.SVNDeltaProcessor;
import org.tmatesoft.svn.core.io.diff.SVNDiffWindow;

import com.googlecode.jsvnserve.SVNSessionStreams;
import com.googlecode.jsvnserve.SVNSessionStreams.DeltaOutputStream;
import com.googlecode.jsvnserve.api.OtherServerException;
import com.googlecode.jsvnserve.element.ListElement;
import com.googlecode.jsvnserve.element.WordElement.Word;
import com.googlecode.jsvnserve.util.Client2Server;
import com.googlecode.jsvnserve.util.Server2Client;

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
     * Serial version UID of this delta class.
     */
    private static final long serialVersionUID = 4228000831626825431L;

    /**
     * SVN delta reader.
     */
    @Client2Server
    private SVNDeltaReader deltaReader;

    @Client2Server
    private SVNDeltaProcessor deltaProcessor;

    @Client2Server
    private ISVNDeltaConsumer deltaConsumer;

    /**
     * Temporary file of the file to checkin.
     */
    @Client2Server
    private File file;

    /**
     * Output stream used for processing text delta's from the SVN client for
     * {@link #file}.
     */
    @Client2Server
    private OutputStream output;

    /**
     * The input stream is opened from the SVN server for {@link #file}.
     *
     * @see #getInputStream()
     */
    @Client2Server
    private InputStream in;

    /**
     * MD5 checksum of the {@link #file}.
     */
    @Client2Server
    private String checkSumMD5;

    /**
     * Stores the MD5 checksum on which this file delta is based on. The
     * information is required if a file already exists (e.g. if file is copied
     * or file is updated).
     *
     * @see #setBaseCheckSumMD5(String)
     * @see #baseRevision
     * @see #basePath
     */
    @Client2Server
    @Server2Client
    private String baseCheckSumMD5;

    /**
     * Stores the revision on which this file delta is based on. The
     * information is required if a file already exists (e.g. if file is copied
     * or file is updated).
     *
     * @see #baseCheckSumMD5
     * @see #basePath
     * @see #getBaseRevision()
     */
    @Client2Server
    @Server2Client
    private final Long baseRevision;

    /**
     * Stores the file path on which this file delta is based on. The
     * information is required if a file already exists (e.g. if file is copied
     * or file is updated).
     *
     * @see #baseCheckSumMD5
     * @see #baseRevision
     * @see #getBasePath()
     */
    @Client2Server
    @Server2Client
    private final String basePath;

    /**
     * Input stream on the base file content.
     *
     * @see #applyTextDelta(String)
     */
    @Client2Server
    @Server2Client
    private InputStream baseIn;

    /**
     * Default constructor.
     *
     * @param _token            token of the file
     * @param _path             path of the file
     * @param _copiedPath       copied from file with given path
     * @param _copiedRevision   copied from file with given revision
     */
    AbstractDeltaFile(final String _token,
                      final String _path,
                      final String _copiedPath,
                      final Long _copiedRevision)
    {
        super(_token, _path,
              _copiedPath, _copiedRevision);
        this.basePath = _copiedPath;
        this.baseRevision = _copiedRevision;
    }

    /**
     *
     * @param _streams          SVN in- and output stream
     * @param _baseCheckSumMD5  base MD5 checksum on which the text delta is created
     * @throws IOException
     * @TODO baseMD5 checksum is not used....
     */
    @Client2Server
    protected void applyTextDelta(final SVNSessionStreams _streams,
                                  final String _baseCheckSumMD5)
            throws IOException
    {
        this.file = File.createTempFile("DeltaFileCreate", ".tmp");
        this.output = new FileOutputStream(this.file);
        this.deltaProcessor = new SVNDeltaProcessor();
        this.deltaReader = new SVNDeltaReader();
        this.baseCheckSumMD5 = _baseCheckSumMD5;
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

        // exists a base path? if yes open an input stream
        if ((this.basePath != null) && (this.baseRevision != null))  {
            this.baseIn = _streams.getSession().getRepository().getFile(this.getBaseRevision(), this.getBasePath());
        }

        this.deltaProcessor.applyTextDelta((this.baseIn == null) ? SVNFileUtil.DUMMY_IN : this.baseIn,
                                           this.output,
                                           true);

    }

    @Client2Server
    protected void textDeltaChunk(final byte[] _buffer)
    {
        try {
            this.deltaReader.nextWindow(_buffer, 0, _buffer.length, null, this.deltaConsumer);
        } catch (final SVNException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Text delta end event that the delta calculation is finished. The
     * {@link #checkSumMD5} is stored and the {@link #output} stream and
     * {@link #baseIn} stream (if opened) are closed and reset to
     * <code>null</code>.
     *
     * @throws IOException if the in- and output streams could not be closed
     */
    @Client2Server
    protected void textDeltaEnd()
            throws IOException
    {
        this.checkSumMD5 = this.deltaProcessor.textDeltaEnd();
        this.deltaReader = null;
        this.deltaProcessor = null;
        this.output.close();
        this.output = null;
        if (this.baseIn != null)  {
            this.baseIn.close();
            this.baseIn = null;
        }
     }

    @Client2Server
    protected void closeFile(final String _md5)
    {
        System.out.println("have md5: " + this.checkSumMD5 + "    from SVN client: " + _md5);
if (!this.checkSumMD5.equals(_md5))  {
    throw new Error("md5 checksum not equal");
}
    }

    /**
     *
     * @return MD5 checksum of the file
     * @see #checkSumMD5
     */
    @Client2Server
    public String getChecksum()
    {
        return this.checkSumMD5;
    }

    /**
     * Returns <i> if the content of the file was changed.
     *
     * @return <i>true</i> if content of file was changed; otherwise
     *         <i>false</i>
     */
    @Client2Server
    public boolean isContentChanged()
    {
        return this.file != null;
    }

    /**
     * Returns the size of the file related to this delta.
     *
     * @return size (length) of the file in byte
     */
    @Client2Server
    public long getFileSize()
    {
      return this.file.length();
    }

    /**
     *
     * @return input stream of the complete file
     * @see #in
     */
    @Client2Server
    public InputStream getInputStream() throws OtherServerException
    {
        if (this.in != null)  {
// TODO: i18n
throw new OtherServerException("File was already opened!");
        }
        try {
            this.in = new FileInputStream(this.file);
        } catch (final FileNotFoundException ex) {
// TODO: i18n
throw new OtherServerException("temporary file '" + this.file + "' not found");
        }
        return this.in;
    }

    @Override
    @Client2Server
    protected void close()
    {
        if (this.in != null)  {
            try {
                this.in.close();
            } catch (final IOException e) {
            }
        }
        if (this.file != null)  {
            this.file.delete();
        }
    }

    /**
     * Sets the MD5 checksum of the file on which this file delta is based on.
     *
     * @param _baseCheckSumMD5  new MD5 checksum on which this file delta is
     *                          based on
     * @see #baseCheckSumMD5
     */
    @Client2Server
    @Server2Client
    public void setBaseCheckSumMD5(final String _baseCheckSumMD5)
    {
        this.baseCheckSumMD5 = _baseCheckSumMD5;
    }

    /**
     * Returns the revision of the file on which this file delta is based on.
     * If the file delta is not based on an existing file, <code>null</code> is
     * returned.
     *
     * @return base revision on which this file delta is based on
     * @see #baseRevision
     */
    public Long getBaseRevision()
    {
        return this.baseRevision;
    }

    /**
     * Returns the path of the file on which this file delta is based on.
     * If the file delta is not based on an existing file, <code>null</code> is
     * returned.
     *
     * @return base file path on which this file delta is based on
     * @see #basePath
     */
    public String getBasePath()
    {
        return this.basePath;
    }

    /**
     * Writes the given file for given event <code>_type</code> to the
     * <code>_streams</code> including all required properties. The content
     * of the file is calculated depending on the delta between
     * base path and the <code>_targetIn</code>. The base path is only used if
     * {@link #basePath}, {@link #baseRevision} and {@link #baseCheckSumMD5}
     * are defined.
     *
     * @param _streams          SVN in- and output stream
     * @param _parentToken      token of the parent directory (where the file
     *                          is located)
     * @param _type             type of the file to write
     *                          ({@link Word#ADD_FILE} or
     *                          {@link Word#OPEN_FILE})
     * @param _targetIn         input stream of the target file
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    @Server2Client
    protected void writeOpen(final SVNSessionStreams _streams,
                             final String _parentToken,
                             final Word _type,
                             final InputStream _targetIn)
            throws UnsupportedEncodingException, IOException
    {
        if ((this.basePath != null) && (this.baseRevision != null) && (this.baseCheckSumMD5 != null))  {
            this.baseIn = _streams.getSession().getRepository().getFile(this.getBaseRevision(), this.getBasePath());
        }

        _streams.writeItemList(
                new ListElement(_type,
                                new ListElement(this.getPath(),
                                                _parentToken,
                                                this.getToken(),
                                                (this.baseIn != null)
                                                        ? new ListElement(this.baseRevision)
                                                        : new ListElement())));
        this.writeAllProperties(_streams, Word.CHANGE_FILE_PROP);

        _streams.writeItemList(new ListElement(Word.APPLY_TEXTDELTA,
                                               new ListElement(this.getToken(),
                                                               (this.baseIn != null)
                                                                       ? new ListElement(this.baseCheckSumMD5)
                                                                       : new ListElement())));

        final String md5 = _streams.writeFileDelta(
                this.baseIn,
                _targetIn,
                new DeltaOutputStream() {
                    @Override
                    public void write(final byte _bytes[],
                                      final int _offset,
                                      final int _len)
                    throws IOException
                    {
                        _streams.traceWrite("( textdelta-chunk ( 2:{} ( {}:... ) ) ) ", AbstractDeltaFile.this.getToken(), _len);
                        _streams.writeWithoutFlush("( textdelta-chunk ( ");
                        _streams.writeWithoutFlush(String.valueOf(AbstractDeltaFile.this.getToken().length()));
                        _streams.writeWithoutFlush(':');
                        _streams.writeWithoutFlush(AbstractDeltaFile.this.getToken());
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

        if (this.baseIn != null)  {
            this.baseIn.close();
            this.baseIn = null;
        }
    }
}
