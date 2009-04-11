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

package com.googlecode.jsvnserve.element;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class StringElement
        extends AbstractElement<byte[]>
{
    /**
     * Date time format from SVN used to format date instance to strings.<br/>
     * Example:<br/>
     * <code>2009-03-14T18:49:06.097886Z</code>
     *
     * @see #StringElement(Date)
     */
    private static final DateFormat DATETIMEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'000Z'");
    static
    {
        DATETIMEFORMAT.setTimeZone(new SimpleTimeZone(0, "GMT"));
    }

    /**
     * Default string for the null date time.
     */
    public final static StringElement NULL_DATETIME = new StringElement("1970-01-01T00:00:00.000000Z".getBytes());

    /**
     *
     * @param _buffer   buffer for this string element
     */
    public StringElement(final byte[] _buffer)
    {
        super(_buffer);
    }

    /**
     *
     * @param _value    new value which will be UTF8 encoded
     * @throws UnsupportedEncodingException if the character sequence could not
     *                                      be encoded to byte buffer
     */
    public StringElement(final CharSequence _value)
            throws UnsupportedEncodingException
    {
        super(_value.toString().getBytes("UTF8"));
    }

    /**
     *
     * @param _value    date value to format to a string
     * @see #DATETIMEFORMAT
     */
    public StringElement(final Date _value)
    {
        super(DATETIMEFORMAT.format(_value).getBytes());
    }

    /**
     * Write this string element to output stream <code>_out</code>. First the
     * length of the byte buffer is written, then a colon and at last the byte
     * buffer itself.
     *
     * @param _out      output stream
     * @throws IOException if the byte buffer could not be written
     */
    @Override
    public void write(final OutputStream _out)
            throws IOException
    {
        _out.write(String.valueOf(this.getValue().length).getBytes());
        _out.write(':');
        if (this.getValue().length > 0)  {
            _out.write(this.getValue());
        }
        _out.write(' ');
    }

    /**
     * Returns the string value for which this SVN element is defined.
     *
     * @return string of current value
     * @see #getValue()
     */
    @Override
    public String getString()
    {
        return new String(this.getValue());
    }

    /**
     * Returns the byte array value for which this SVN element is defined.
     *
     * @return byte array of current value
     * @see #getValue()
     */
    @Override
    public byte[] getByteArray()
    {
        return this.getValue();
    }

    /**
     * Returns a 'string' representation of the byte buffer. As prefix,
     * &quot;STRING&quot; is returned in front.
     */
    @Override
    public String toString()
    {
        return "STRING '" + new String(this.getValue()) + "'";
    }
}