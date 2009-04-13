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

package com.googlecode.jsvnserve.api.properties;

import com.googlecode.jsvnserve.util.Timestamp;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
@SuppressWarnings("serial")
public class RevisionPropertyValues
        extends Properties
{
    /**
     * @return author of the revision
     * @see PropertyKey#REVISION_AUTHOR
     */
    public String getAuthor()
    {
        return this.get(PropertyKey.REVISION_AUTHOR);
    }

    /**
     * @param _author   author of the revision to set
     * @see PropertyKey#REVISION_AUTHOR
     */
    public void setAuthor(final String _author)
    {
        this.put(PropertyKey.REVISION_AUTHOR, _author);
    }

    /**
     * @return date of revision
     * @see PropertyKey#REVISION_DATE
     */
    public Timestamp getDate()
    {
        return Timestamp.valueOf(this.get(PropertyKey.REVISION_DATE));
    }

    /**
     * @param _date      date to set
     * @see PropertyKey#REVISION_DATE
     */
    public void setDate(final Timestamp _date)
    {
        this.put(PropertyKey.REVISION_DATE, _date.toString());
    }

    /**
     * @return original date of the revision
     * @see PropertyKey#REVISION_ORIGINAL_DATE
     */
    public Timestamp getOriginalDate()
    {
        return Timestamp.valueOf(this.get(PropertyKey.REVISION_ORIGINAL_DATE));
    }

    /**
     * @param _originalDate     original date to set
     * @see PropertyKey#REVISION_ORIGINAL_DATE
     */
    public void setOriginalDate(final Timestamp _originalDate)
    {
        this.put(PropertyKey.REVISION_ORIGINAL_DATE, _originalDate.toString());
    }

    /**
     * @return revision log
     * @see PropertyKey#REVISION_LOG
     */
    public String getLog()
    {
        return this.get(PropertyKey.REVISION_LOG);
    }

    /**
     * @param log       text of the log to set
     * @see PropertyKey#REVISION_LOG
     */
    public void setLog(final String _log)
    {
        this.put(PropertyKey.REVISION_LOG, _log);
    }

    /**
     * @return the autoversioned
     * @see PropertyKey#REVISION_AUTOVERSIONED
     */
    public boolean isAutoversioned()
    {
        return Boolean.parseBoolean(this.get(PropertyKey.REVISION_AUTOVERSIONED));
    }

    /**
     * @param _autoversioned the autoversioned to set
     * @see PropertyKey#REVISION_AUTOVERSIONED
     */
    public void setAutoversioned(final boolean _autoversioned)
    {
        this.put(PropertyKey.REVISION_AUTOVERSIONED, String.valueOf(_autoversioned));
    }
}
