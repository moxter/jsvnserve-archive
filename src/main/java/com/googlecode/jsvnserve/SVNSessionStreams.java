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

package com.googlecode.jsvnserve;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.security.sasl.Sasl;
import javax.security.sasl.SaslServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;
import org.tmatesoft.svn.core.io.ISVNDeltaConsumer;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
import org.tmatesoft.svn.core.io.diff.SVNDiffWindow;

import com.googlecode.jsvnserve.api.ServerException;
import com.googlecode.jsvnserve.api.ServerException.ErrorCode;
import com.googlecode.jsvnserve.element.AbstractElement;
import com.googlecode.jsvnserve.element.ListElement;
import com.googlecode.jsvnserve.element.WordElement.Word;
import com.googlecode.jsvnserve.sasl.SaslInputStream;
import com.googlecode.jsvnserve.sasl.SaslOutputStream;

/**
 * @author jSVNServe Team
 * @version $Id$
 */
public class SVNSessionStreams
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SVNSessionStreams.class);

    /**
     * Stores related SVN server session for which this input and output
     * streams are defined.
     *
     * @see #getSession()
     */
    private final SVNServerSession session;

    /**
     * @see #updateStreams(SaslServer)
     */
    private OutputStream out;

    /**
     * @see #updateStreams(SaslServer)
     */
    private InputStream in;

    /**
     * Dummy list of elements to read a single element.
     *
     * @see #readElement()
     */
    private final ListElement dummyList = new ListElement();

    /**
     *
     * @param _session  related SVN server session
     * @param _out      initial output stream
     * @param _in       initial input stream
     */
    protected SVNSessionStreams(final SVNServerSession _session,
                                final OutputStream _out,
                                final InputStream _in)
    {
        this.session = _session;
        this.out = _out;
        this.in = _in;
    }

    /**
     *
     * @return related SVN server session
     * @see #session
     */
    public SVNServerSession getSession()
    {
        return this.session;
    }

    /**
     * If the writeWithoutFlush methods are used, no trace is written and must
     * done manually.
     *
     * @param _text     text with format
     * @param _objects  objects for the text
     */
    public void traceWrite(final String _text,
                           final Object... _objects)
    {
        if (SVNSessionStreams.LOGGER.isTraceEnabled())  {
            SVNSessionStreams.LOGGER.trace("RES<: " + _text, _objects);
        }
    }

    /**
     * If the {@link #readElement()} method is used, no trace is written and
     * must done manually.
     *
     * @param _text     text with format
     * @param _objects  objects for the text
     */
    public void traceRead(final String _text,
                          final Object... _objects)
    {
        if (SVNSessionStreams.LOGGER.isTraceEnabled())  {
            SVNSessionStreams.LOGGER.trace("REQ>: " + _text, _objects);
        }
    }


    public void write(final String _text)
            throws UnsupportedEncodingException, IOException
    {
        SVNSessionStreams.LOGGER.trace("RES<: {}", _text);
        this.out.write(_text.getBytes("UTF8"));
        this.out.flush();
    }

    public void writeItemList(final ListElement... _lists)
            throws IOException
    {
        for (final ListElement list : _lists)  {
            if (SVNSessionStreams.LOGGER.isTraceEnabled())  {
                final ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
                list.write(byteArrayOut);
                SVNSessionStreams.LOGGER.trace("RES<: {}", byteArrayOut.toString());
            }
            list.write(this.out);
        }
        this.out.flush();
    }

    public void writeItemList(final List<ListElement> _lists)
            throws IOException
    {
        for (final ListElement list : _lists)  {
            if (SVNSessionStreams.LOGGER.isTraceEnabled())  {
                final ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
                list.write(byteArrayOut);
                SVNSessionStreams.LOGGER.trace("RES<: {}", byteArrayOut.toString());
            }
            list.write(this.out);
        }
        this.out.flush();
    }

    /**
     * Writes an empty success status.
     *
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public void writeSuccessStatus()
            throws UnsupportedEncodingException, IOException
    {
        this.writeItemList(
                    new ListElement(Word.STATUS_SUCCESS,
                                    new ListElement(new ListElement())));
    }

    /**
     * Writes the failure status.
     *
     * @param _errorCode        error code to write
     * @param _errorMessage     error message to write; if <code>null</code>
     *                          an empty string is written
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public void writeFailureStatus(final ErrorCode _errorCode,
                                   final String _errorMessage)
            throws UnsupportedEncodingException, IOException
    {
        this.writeItemList(
                new ListElement(Word.STATUS_FAILURE,
                        new ListElement(new ListElement(
                                _errorCode.code,
                                (_errorMessage == null) ? "" : _errorMessage,
                                "",
                                0))));
    }

    /**
     * Writes the failure status with unknown error code 0.
     *
     * @param _errorMessage     error message to write; if <code>null</code>
     *                          an empty string is written
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public void writeFailureStatus(final String _errorMessage)
            throws UnsupportedEncodingException, IOException
    {
        this.writeFailureStatus(ErrorCode.UNKNOWN, _errorMessage);
    }

    public void writeFailureStatus(final ServerException _exception)
            throws UnsupportedEncodingException, IOException
    {
        this.writeFailureStatus(_exception.getErrorCode(), _exception.getMessage());
    }


    public void writeWithoutFlush(final String _text)
            throws UnsupportedEncodingException, IOException
    {
        this.out.write(_text.getBytes("UTF8"));
    }

    public void writeWithoutFlush(final byte[] _buffer)
            throws IOException
    {
        this.out.write(_buffer);
    }

    public void writeWithoutFlush(final byte[] _buffer,
                                  final int _offset,
                                  final int _length)
            throws IOException
    {
        this.out.write(_buffer, _offset, _length);
    }

    public void writeWithoutFlush(final byte _byte)
            throws IOException
    {
        this.out.write(_byte);
    }

    public void writeWithoutFlush(final char _byte)
            throws IOException
    {
        this.out.write(_byte);
    }

    /**
     *
     * @param _source       input stream from the original file
     * @param _target       target stream from the file for which the delta to
     *                      the <code>_source</code> must be computed
     * @param _output
     * @param _computeMD5   <i>true</i> means that the MD5 checksum is computed
     * @return if <code>_computeMD5</code> is <i>true</i> the MD5 checksum is
     *         returned; otherwise <code>null</code> is returned
     * @see DelteOutputStream
     */
    public String writeFileDelta(final InputStream _source,
                                 final InputStream _target,
                                 final DeltaOutputStream _output,
                                 final boolean _computeMD5)
    {
        String md5 = null;
        SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
        try {
            md5 = deltaGenerator.sendDelta(
                    "",
                    (_source == null) ? SVNFileUtil.DUMMY_IN : _source,
                    0,
                    _target,
                    new ISVNDeltaConsumer() {
                        boolean writeHeader = true;

                        public void applyTextDelta(final String s, final String s1)
                        {
                        }

                        public OutputStream textDeltaChunk(final String s,
                                                           final SVNDiffWindow svndiffwindow)
                            throws SVNException
                        {
                            try {
                                svndiffwindow.writeTo(_output, this.writeHeader, true);
                            } catch (final IOException e) {
                                throw new SVNException(SVNErrorMessage.UNKNOWN_ERROR_MESSAGE, e);
                            }
                            this.writeHeader = false;
                            return null;
                        }

                        public void textDeltaEnd(final String s)
                        {
                        }
                    },
                    _computeMD5);
        } catch (final SVNException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
        return md5;
    }


    public void flush()
            throws IOException
    {
        this.out.flush();
    }

    public ListElement readItemList()
            throws IOException
    {
        final ListElement list = new ListElement();
        final int ch = this.in.read();
        if (ch != -1)  {
            if (((char) ch) != '(')  {
                throw new Error("fehler:" + (char)ch);
            }
            if (!Character.isWhitespace((char) this.in.read()))  {
                throw new Error("fehler");
            }
            list.read(this.in);
            if (SVNSessionStreams.LOGGER.isTraceEnabled())  {
                final ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
                list.write(byteArrayOut);
                SVNSessionStreams.LOGGER.trace("REQ>: {}", byteArrayOut.toString());
            }
        }
        return (ch == -1) ? null : list;
    }

    /**
     * Reads exact one element and returns them.
     *
     * @return element which was read
     * @throws IOException if an I/O error occurred while reading an element
     * @see #traceRead(String, Object...)
     */
    public AbstractElement<?> readElement()
            throws IOException
    {
        return this.dummyList.readElement(this.in);
    }

    /**
     * <p>Depending on the quality-of-protection property of the Sasl server
     * the streams {@link #in} and {@link #out} must by encrypted.</p>
     * <p>If the  quality-of-protection property is set to
     * <code>auth-int</code> or </code>auth-conf</code>, the input and output
     * streams {@link #in} and {@link #out} must be encrypted. This is done by
     * filtering the original streams via the {@link SaslInputStream} and
     * {@link SaslOutputStream}.</p>
     *
     * @param _saslServer   Sasl server
     * @see #in
     * @see #out
     * @see SaslInputStream
     * @see SaslOutputStream
     * @see #authenticate(String)
     */
    protected void updateStreams(final SaslServer _saslServer)
    {
        final String qop = (String) _saslServer.getNegotiatedProperty(Sasl.QOP);
        if ("auth-int".equals(qop) || "auth-conf".equals(qop))  {

            // get output buffer size
            final String outBuffSizeStr = (String) _saslServer.getNegotiatedProperty(Sasl.RAW_SEND_SIZE);
            int outBuffSize = 1000;
            if (outBuffSizeStr != null) {
                try {
                    outBuffSize = Integer.parseInt(outBuffSizeStr);
                } catch (final NumberFormatException nfe) {
                    outBuffSize = 1000;
                }
            }

            // get input buffer size
            final String inBuffSizeStr = (String) _saslServer.getNegotiatedProperty(Sasl.MAX_BUFFER);
            int inBuffSize = 1000;
            if (inBuffSizeStr != null)  {
                try {
                    inBuffSize = Integer.parseInt(inBuffSizeStr);
                } catch (final NumberFormatException nfe) {
                    inBuffSize = 1000;
                }
            }

            this.out = new SaslOutputStream(_saslServer, outBuffSize, this.out);
            this.in = new SaslInputStream(_saslServer, inBuffSize, this.in);
        }
    }

    /**
     * @see SVNSessionStreams#writeFileDelta(InputStream, InputStream, DeltaOutputStream, boolean)
     */
    public abstract static class DeltaOutputStream
            extends OutputStream
    {
        @Override
        public abstract void write(final byte[] _bytes,
                                   final int _offset,
                                   final int _len)
            throws IOException;

        @Override
        public void write(final byte[] _bytes)
            throws IOException
        {
            write(_bytes, 0, _bytes.length);
        }

        @Override
        public void write(final int _byte)
            throws IOException
        {
            write(new byte[]{(byte) (_byte & 0xFF)});
        }
    }
}
