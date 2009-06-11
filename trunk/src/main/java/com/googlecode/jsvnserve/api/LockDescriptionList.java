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

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * List of all lock / unlock descriptions depending on the path.
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class LockDescriptionList
{
    /**
     * Holds depending on the path all related lock descriptions.
     */
    private final Map<String, LockDescription> lockDescriptions = new TreeMap<String, LockDescription>();


    /**
     * Returns the list of lock descriptions.
     *
     * @return list of all lock descriptions
     * @see #lockDescriptions
     */
    public Collection<LockDescription> getLockDescriptions()
    {
        return this.lockDescriptions.values();
    }

    /**
     * Adds to the lock list a successful lock.
     *
     * @param _path     path which is successfully locked
     * @param _token    lock token (id)
     * @param _owner    owner of the lock
     * @param _comment  comment of the lock (or <code>null</code> if no comment
     *                  defined)
     * @param _created  created date of the lock
     * @param _expires  expire date of the lock (or <code>null</code> if not
     *                  defined)
     */
    public void addLockedSuccessfully(final String _path,
                                      final String _token,
                                      final String _owner,
                                      final String _comment,
                                      final Date _created,
                                      final Date _expires)
    {
        this.lockDescriptions.put(_path, new LockDescription(Status.SUCCESSFULLY,
                                                             _path, _token, _owner, _comment, _created, _expires));
    }

    /**
     *
     * @param _path     path which is successfully unlocked
     */
    public void addUnlockedSuccessfully(final String _path)
    {
        this.lockDescriptions.put(_path, new LockDescription(Status.SUCCESSFULLY,
                                                             _path, null, null, null, null, null));
    }

    /**
     *
     * @param _path     path which is failed
     * @param _comment  error message why the lock failed
     */
    public void addFailed(final String _path,
                          final String _comment)
    {
        this.lockDescriptions.put(_path, new LockDescription(Status.FAILED,
                                                             _path, null, null, _comment, null, null));
    }

    /**
     * Status enumeration of a lock / unlock description.
     */
    public enum Status
    {
        /** Locking was successfully. */
        SUCCESSFULLY,

        /** Locking failed. */
        FAILED;
    }

    /**
     * For each path which is locked or unlocked, a description must be
     * returned (if the lock was successfully or was failed).
     */
    public static final class LockDescription
    {
        /**
         * Locking status of the path.
         */
        private final Status status;

        /**
         * Path which is locked / unlocked.
         */
        private final String path;

        /**
         * Lock token of the path (only if the locking of the path was
         * successfully).
         */
        private final String token;

        /**
         * Owner of the locking.
         */
        private final String owner;

        /**
         * Comment of the locking.
         */
        private final String comment;

        /**
         * Date at which the locking was done.
         */
        private final Date created;

        /**
         * Date at which the locking expires.
         */
        private final Date expires;

        /**
         * Constructor to initialize all instance variables.
         *
         * @param _status
         * @param _path
         * @param _token
         * @param _owner
         * @param _comment
         * @param _created
         * @param _expires
         */
        private LockDescription(final Status _status,
                                final String _path,
                                final String _token,
                                final String _owner,
                                final String _comment,
                                final Date _created,
                                final Date _expires)
        {
            this.status = _status;
            this.path = _path;
            this.token = _token;
            this.owner = _owner;
            this.comment = _comment;
            this.created = _created;
            this.expires = _expires;
        }

        public static LockDescription create(final String _path,
                                             final String _token,
                                             final String _owner,
                                             final String _comment,
                                             final Date _created,
                                             final Date _expires)
        {
            return new LockDescription(Status.SUCCESSFULLY,
                                       _path, _token, _owner, _comment, _created, _expires);
        }

        /**
         *
         * @return status of the lock (successfully or failed)
         * @see #status
         */
        public Status getStatus()
        {
            return this.status;
        }

        /**
         *
         * @return status which is locked or unlocked
         * @see #path
         */
        public String getPath()
        {
            return this.path;
        }

        /**
         *
         * @return token of the lock
         * @see #token
         */
        public String getToken()
        {
            return this.token;
        }

        /**
         *
         * @return owner of the lock
         * @see #owner
         */
        public String getOwner()
        {
            return this.owner;
        }

        /**
         *
         * @return comment of the lock or error message if lock / unlock failed
         *         (see {@link #status})
         * @see #comment
         */
        public String getComment()
        {
            return this.comment;
        }

        /**
         *
         * @return created date of the lock
         * @see #created
         */
        public Date getCreated()
        {
            return this.created;
        }

        /**
         *
         * @return date when the lock expires
         * @see #expires
         */
        public Date getExpires()
        {
            return this.expires;
        }
    }
}
