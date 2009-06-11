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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import javax.security.sasl.SaslServer;

/**
 * Depending on the Sasl mechanism the output stream must be encoded before
 * it could be send to client. The original stream {@link #out} is encoded by
 * using {@link SaslServer#wrap(byte[], int, int)}.
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class SaslOutputStream
        extends OutputStream
{
    /**
     * Original output stream which must be encoded.
     */
    private final OutputStream out;

    /**
     * Sasl server used to encode output stream {@link #out}.
     */
    private final SaslServer saslServer;

    /**
     * Buffer used to store the length of a encoded block.
     *
     * @see #flush()
     */
    private final byte[] lengthBuffer = new byte[4];

    /**
     * Buffer used to buffer written bytes from
     * {@link #write(byte[], int, int)} before they are encoded and written to
     * the original output stream {@link #out}.
     *
     * @see #flush()
     */
    private final ByteBuffer buffer;

    /**
     * Default constructor.
     *
     * @param _saslServer   Sasl server used to decode the original output
     *                      stream
     * @param _bufferSize   buffer size
     * @param _out          original output stream which must be decoded
     */
    public SaslOutputStream(final SaslServer _saslServer,
                            final int _bufferSize,
                            final OutputStream _out)
    {
        this.out = _out;
        this.saslServer = _saslServer;
        this.buffer = ByteBuffer.allocate(_bufferSize);
    }

    /**
     * Writes given byte into {@link #buffer}.
     *
     * @param _byte     byte to write
     * @throws IOException if write failed
     * @see #write(byte[], int, int)
     */
    @Override
    public void write(final int _byte)
        throws IOException
    {
        write(new byte[] {(byte) (_byte & 0xff)});
    }

    /**
     * The <code>_bytes</code> are written to into {@link #buffer}. If the
     * buffer size is reached, the buffer is flushed with {@link #flush()}.
     *
     * @param _bytes    bytes buffer to write
     * @param _offset   offset in <code>_bytes</code> to start to write
     * @param _len      length of bytes to write
     * @throws IOException if write failed
     * @see #buffer
     * @see #flush()
     */
    @Override
    public void write(final byte[] _bytes,
                      final int _offset,
                      final int _len)
        throws IOException
    {
        int offset = _offset;
        int len = _len;
        while (len > 0)  {
            final int toPut = Math.min(this.buffer.remaining(), len);
            this.buffer.put(_bytes, offset, toPut);
            offset += toPut;
            len -= toPut;
            if (this.buffer.remaining() == 0) {
                this.flush();
            }
        }
    }

    /**
     * Flushes current buffered bytes in {@link #buffer} with {@link #flush()}
     * and closes original output stream {@link #out}.
     *
     * @throws IOException if closing failed
     * @see #flush()
     * @see #out
     */
    @Override
    public void close()
        throws IOException
    {
        this.flush();
        this.out.close();
    }

    /**
     * Encodes buffered bytes {@link #buffer} and writes them to the output
     * stream {@link #out}.
     *
     * @throws IOException if flushing failed
     * @see #out
     * @see #buffer
     * @see #lengthBuffer
     */
    @Override
    public void flush()
        throws IOException
    {
        final byte[] bytes = this.buffer.array();
        final int off = this.buffer.arrayOffset();
        final int len = this.buffer.position();
        if (len > 0)  {
            final byte[] encoded = this.saslServer.wrap(bytes, off, len);

            // first length of encoded block must be written
            this.lengthBuffer[0] = (byte) ((encoded.length & 0xff000000) >> 24);
            this.lengthBuffer[1] = (byte) ((encoded.length & 0x00ff0000) >> 16);
            this.lengthBuffer[2] = (byte) ((encoded.length & 0x0000ff00) >> 8);
            this.lengthBuffer[3] = (byte) ((encoded.length & 0x000000ff));
            this.out.write(this.lengthBuffer, 0, this.lengthBuffer.length);

            // write encoded block
            this.out.write(encoded, 0, encoded.length);

            // and flush block
            this.out.flush();
            this.buffer.clear();
        }
    }
}
