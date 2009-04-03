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

package com.googlecode.jsvnserve.sasl;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.security.sasl.SaslServer;

/**
 * Depending on the Sasl mechanism the input stream must be decoded before
 * it could be used. The original stream {@link #in} is decoded with using
 * {@link SaslServer#unwrap(byte[], int, int)}.
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class SaslInputStream
        extends InputStream
{
    /**
     * Original input stream which must be decoded.
     */
    private final InputStream in;

    /**
     * Sasl server used to decode input stream {@link #in}.
     */
    private final SaslServer saslServer;

    /**
     * Byte buffer used to read from input stream {@link #in} in
     * {@link #readDecodedBuffer()}. The bytes are encoded.
     */
    private byte[] readBuffer;

    /**
     * Buffers decoded bytes which are returned.
     */
    private ByteBuffer byteBuffer;

    /**
     * Default constructor.
     *
     * @param _saslServer   Sasl server used to decode the original input
     *                      stream
     * @param _bufferSize   buffer size
     * @param _in           original input stream which must be decoded
     */
    public SaslInputStream(final SaslServer _saslServer,
                           final int _bufferSize,
                           final InputStream _in)
    {
        this.in = _in;
        this.readBuffer = new byte[_bufferSize * 2];
        this.saslServer = _saslServer;
    }

    /**
     * Closes the original input stream {@link #in}.
     *
     * @see #in
     */
    @Override
    public void close()
            throws IOException
    {
        this.in.close();
    }

    /**
     * Reads next byte of the stream. Because the original steam is encoded,
     * method {@link #read(byte[], int, int)} is used to get next byte.
     *
     * @return next byte from the stream or <code>-1</code> if end of file
     *         reached
     */
    @Override
    public int read()
            throws IOException
    {
        final byte[] buf = new byte[1];
        final int len = this.read(buf, 0, 1);
        return (len == 1) ? buf[0] : -1;
    }

    /**
     * Reads up to <code>_len</code> bytes from {@link #byteBuffer}. If
     * {@link #byteBuffer} is <code>null</code>, {@link #readDecodedBuffer()}
     * is called to decode next block of input stream {@link #in}.
     *
     * @param _bytes    buffer into which the date is read
     * @param _offset   start offset in array <code>_bytes</code> at which the
     *                  data is written
     * @param _len      length of bytes to read
     * @see #byteBuffer
     */
    @Override
    public int read(final byte[] _bytes,
                    final int _offset,
                    final int _len)
            throws IOException
    {
        int read = 0;
        if (_len > 0)  {
            if (this.byteBuffer == null)  {
                this.readDecodedBuffer();
            }
            read = Math.min(_len, this.byteBuffer.remaining());
            this.byteBuffer.get(_bytes, _offset, read);
            if (this.byteBuffer.remaining() == 0)  {
                this.byteBuffer = null;
            }
        }
        return read;
    }

    /**
     * To skip input is not supported from this Sasl input stream. So the
     * method overwrites the original method and returns always <code>0</code>.
     *
     * @param _count    number of bytes to skip
     * @return always <code>0</code> because skip is not supported
     */
    @Override
    public long skip(long _count)
            throws IOException
    {
        return 0;
    }

    /**
     * <p>Reads next block of bytes and decodes them. The next first four bytes
     * are the length of the block (as integer). The block itself must be then
     * decoded.</p>
     * <p>The method blocks until
     * <ul>
     * <li>the complete block to decode was read</li>
     * <li>end of stream was reached</li>
     * <li>or an I/O error was occurred.</li>
     * </ul>
     * </p>
     *
     * @throws EOFException if end of stream is reached
     * @throws IOException  if an I/O error occurs
     */
    private void readDecodedBuffer()
            throws EOFException, IOException
    {
        // get length of bytes to decode
        final int ch1 = in.read();
        final int ch2 = in.read();
        final int ch3 = in.read();
        final int ch4 = in.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)  {
            throw new EOFException();
        }
        final int decodedLength =  ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));

        // check that read buffer is big enough
        if (this.readBuffer.length < decodedLength)  {
            this.readBuffer = new byte[(decodedLength * 3) / 2];
        }

        // read until all bytes needed to decode are read
        int n = 0;
        while (n < decodedLength)  {
            final int count = this.in.read(this.readBuffer, n, decodedLength - n);
            if (count < 0)  {
                throw new EOFException();
            }
            n += count;
        }

        this.byteBuffer = ByteBuffer.wrap(saslServer.unwrap(readBuffer, 0, decodedLength));
    }
}
