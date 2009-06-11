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

package com.googlecode.jsvnserve.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.googlecode.jsvnserve.SVNSessionStreams;
import com.googlecode.jsvnserve.element.ListElement;

/**
 * The class is used to hold location entries for a locations lookup.
 *
 * @author jSVNServe Team
 * @version $Id$
 * @see IRepository#getLocations(long, String, long...)
 */
public class LocationEntries
{
    /**
     * Holds depending in the revision related path names (location).
     */
    private final Map<Long, String> entries = new HashMap<Long, String>();

    /**
     * Adds a new location to the list of location entries.
     *
     * @param _revision     revision to add
     * @param _path         path name for given revision
     */
    public void add(final long _revision,
                    final String _path)
    {
        this.entries.put(_revision, _path);
    }

    /**
     * <p>For each entry in {@link #entries} a location entry is written to the
     * output stream of the SVN server to the SVN client. After the last
     * location entry, a &quot;<code>done</code>&quot; is written.</p>
     *
     * <p><b>SVN Response for each Location Entry</b><br/>
     * <code style="color:green">( rev:number abs-path:string )</code></p>
     *
     * <p><b>SVN Response after last Location Entry</b><br/>
     * <code style="color:green">done</code></p>
     *
     * @param _streams      SVN session streams where the location entries must
     *                      be written
     * @throws UnsupportedEncodingException if a string could not be UTF8
     *                                      encoded
     * @throws IOException                  if an I/O error occurred
     */
    public void write(final SVNSessionStreams _streams)
        throws UnsupportedEncodingException, IOException
    {
        for (final Map.Entry<Long, String> entry : this.entries.entrySet())  {
            _streams.writeItemList(new ListElement(entry.getKey(), entry.getValue()));
        }
        _streams.write("done ");
    }
}
