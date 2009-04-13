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

import java.util.UUID;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
@SuppressWarnings("serial")
public class Revision0PropertyValues
        extends RevisionPropertyValues
{
    /**
     * @return revision which is currently copied within a sync; if not present
     *         </code>null</code> is returned
     * @see PropertyKey#REVISION0_CURRENTLY_COPYING
     * @see #setSyncCurrentlyCoping(String)
     */
    public Long getSyncCurrentlyCoping()
    {
        final String tmp = this.get(PropertyKey.REVISION0_CURRENTLY_COPYING);
        return (tmp == null)? null : Long.parseLong(tmp);
    }

    /**
     * @param _revision     revision number which is currently copied (or
     *                      <code>null</code> if not present)
     * @see PropertyKey#REVISION_AUTHOR
     * @see #getSyncCurrentlyCoping()
     */
    public void setSyncCurrentlyCoping(final Long _revision)
    {
        if (_revision == null)  {
            this.put(PropertyKey.REVISION0_CURRENTLY_COPYING, null);
        } else  {
            this.put(PropertyKey.REVISION0_CURRENTLY_COPYING, String.valueOf(_revision));
        }
    }

    /**
     * @return revision of the external SVN instance which is was last
     *         synchronized into this SVN instance; if not present
     *         </code>null</code> is returned
     * @see PropertyKey#REVISION0_LAST_MERGED_REVISION
     * @see #setSyncCurrentlyCoping(String)
     */
    public Long getSyncLastMergedRevision()
    {
        final String tmp = this.get(PropertyKey.REVISION0_LAST_MERGED_REVISION);
        return (tmp == null)? null : Long.parseLong(tmp);
    }

    /**
     * @param _revision     revision number which is currently copied (or
     *                      <code>null</code> if not present)
     * @see PropertyKey#REVISION0_LAST_MERGED_REVISION
     * @see #getSyncCurrentlyCoping()
     */
    public void setSyncLastMergedRevision(final Long _revision)
    {
        if (_revision == null)  {
            this.put(PropertyKey.REVISION0_LAST_MERGED_REVISION, null);
        } else  {
            this.put(PropertyKey.REVISION0_LAST_MERGED_REVISION, String.valueOf(_revision));
        }
    }

    /**
     *
     * @return URL of the SVN instance from where it is synchronized; or
     *         <code>0</code> if not synchronized
     * @see PropertyKey#REVISION0_FROM_URL
     */
    public String getSyncFromURL()
    {
        return this.get(PropertyKey.REVISION0_FROM_URL);
    }

    /**
     *
     * @param _targetSvnUrl     URL of the SVN instance from where it is
     *                          synchronized
     */
    public void setSyncFromURL(final String _targetSvnUrl)
    {
        this.put(PropertyKey.REVISION0_FROM_URL, _targetSvnUrl);
    }

    /**
     *
     * @return UUID of the SVN system from where it is synchronized; or
     *         <code>null</code> if currently not synchronized
     * @see PropertyKey#REVISION0_FROM_UUID
     */
    public UUID getSyncFromUUID()
    {
        final String uuid = this.get(PropertyKey.REVISION0_FROM_UUID);
        return (uuid == null) ? null : UUID.fromString(uuid);
    }

    /**
     *
     * @param _targetUUID   UUID of the target SVN instance from where is
     *                      synchronized; or <code>null</code> if currently
     *                      not synchronized
     * @see PropertyKey#REVISION0_FROM_UUID
     */
    public void setSyncFromUUID(final UUID _targetUUID)
    {
        this.put(PropertyKey.REVISION0_FROM_UUID,
                 (_targetUUID != null) ? _targetUUID.toString() : null);
    }

    /**
     *
     * @return lock token if currently synchronized; or <code>null</code> if
     *         not synchronized
     * @see PropertyKey#REVISION0_LOCK
     */
    public String getSyncLock()
    {
        return this.get(PropertyKey.REVISION0_LOCK);
    }

    /**
     *
     * @param _lockToken    new lock token to define that currently is
     *                      synchronized; or <code>null</code> to unlock
     * @see PropertyKey#REVISION0_LOCK
     */
    public void setSyncLock(final String _lockToken)
    {
        this.put(PropertyKey.REVISION0_LOCK, _lockToken);
    }
}
