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

package com.googlecode.jsvnserve.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

/**
 * <p>A thin wrapper around {@link Date} to identify SVN time stamps which
 * includes microseconds. The class could handle also the related string
 * representation of the SVN time stamps in GMT0 time zone (encode and decode
 * to / from strings). The string representation of the SVN time stamp uses the
 * format <code>yyyy-MM-dd'T'HH:mm:ss.SSSfff</code> (<code>fff</code> stands
 * for the microseconds).</p>
 * <p><b>Attention!</b> The class uses the {@link #date} instance to hold the
 * time stamp milliseconds. The microseconds of the SVN time stamp are stored
 * separately in {@link #microseconds}.<p/>
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class Timestamp
{
    /**
     * Date time format from SVN used to format date instance to strings.<br/>
     * Example:<br/>
     * <code>2009-03-14T18:49:06.097886Z</code>
     *
     * @see #valueOf(String)
     * @see #toString()
     */
    private static final DateFormat DATETIMEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    static
    {
        Timestamp.DATETIMEFORMAT.setTimeZone(new SimpleTimeZone(0, "GMT"));
    }

    /**
     * Date instance to hold the part of the date in milliseconds.
     */
    private final Date date = new Date();

    /**
     * Holds the part of the date in microseconds (must be between 0 and 999
     * microseconds, otherwise {@link #date} must be used).
     */
    private final int microseconds;

    /**
     * Constructor to create a new time stamp instance.
     *
     * @param _time         milliseconds since January 1, 1970, 00:00:00 GMT.
     *                      A negative number is the number of milliseconds
     *                      before January 1, 1970, 00:00:00 GMT.
     * @param _microseconds part of time stamp
     * @see java.util.Calendar for more information
     * @see #date
     * @see #microseconds
     */
    public Timestamp(final long _time,
                     final int _microseconds)
    {
        final int miliseconds = (_microseconds / 1000);
        this.date.setTime(_time + miliseconds);
        this.microseconds = _microseconds - (miliseconds * 1000);
    }

    /**
     * Returns the string formatted in SVN of the time stamp. First,
     * {@link #date} is formatted with {@link #DATETIMEFORMAT}, then as prefix
     * the microseconds are appended.
     *
     * @return string representation of a time stamp
     * @see #date
     * @see #microseconds
     */
    @Override
    public String toString()
    {
        return new StringBuilder()
                .append(Timestamp.DATETIMEFORMAT.format(this.date))
                .append(String.format("%03d", this.microseconds))
                .append('Z')
                .toString();
    }

    /**
     * Returns a SVN time stamp instance encoded in a string representation.
     *
     * @param _value    string value to parse
     * @return related SVN time stamp
     */
    public static Timestamp valueOf(final String _value)
    {
        final Timestamp timestamp;
        if (_value == null)  {
            timestamp = null;
        } else  {
            final int pointIdx = _value.indexOf('.');
            final Date date;
            try  {
                date = Timestamp.DATETIMEFORMAT.parse(_value.substring(0, pointIdx + 4));
            } catch (final ParseException ex)  {
                throw new IllegalArgumentException("Timestamp format must be yyyy-MM-dd'T'HH:mm:ss.ffffff");
            }
            timestamp = new Timestamp(date.getTime(),
                                      Integer.parseInt(_value.substring(pointIdx + 4, pointIdx + 7)));
        }

        return timestamp;
    }

    /**
     * Returns a SVN time stamp instance in microseconds. Because the date
     * instance has only milliseconds, the microseconds will be
     * &quot;<code>0</code>&quot;.
     *
     * @param _value    date instance in milliseconds
     * @return SVN time stamp with microseconds
     */
    public static Timestamp valueOf(final Date _value)
    {
        return new Timestamp(_value.getTime(), 0);
    }

    /**
     * Returns a SVN timestamp instance in microseconds. Because the given
     * SQL timestamp instance uses nanoseconds, only the microsecond part is
     * used (rounded).
     *
     * @param _timestamp    SQL time stamp with nanoseconds
     * @return SVN time stamp with microseconds
     */
    public static Timestamp valueOf(final java.sql.Timestamp _timestamp)
    {
        return new Timestamp(_timestamp.getTime(), (_timestamp.getNanos() + 500) / 1000);
    }
}
