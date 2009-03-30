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
        extends AbstractElement<String>
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
    public final static StringElement NULL_DATETIME = new StringElement("1970-01-01T00:00:00.000000Z");

    public StringElement(final CharSequence _value)
    {
        super(_value.toString());
    }

    /**
     *
     * @param _value    date value to format to a string
     * @see #DATETIMEFORMAT
     */
    public StringElement(final Date _value)
    {
        super(DATETIMEFORMAT.format(_value));
    }

    @Override
    public void write(final OutputStream _out)
            throws UnsupportedEncodingException, IOException
    {
        _out.write(String.valueOf(this.getValue().length()).getBytes("UTF8"));
        _out.write(':');
        _out.write(this.getValue().getBytes("UTF8"));
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
        return this.getValue();
    }

    @Override
    public String toString()
    {
        return "STRING '" + this.getValue() + "'";
    }
}