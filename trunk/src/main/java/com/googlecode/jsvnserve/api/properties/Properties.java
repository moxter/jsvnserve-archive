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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
@SuppressWarnings("serial")
public class Properties
        extends HashMap<String,String>
{
    /**
     * Enumeration used to define all SVN standard property keys.
     */
    public enum PropertyKey
    {
        /**
         * If present on a file, the client will make the file executable in
         * Unix-hosted working copies.
         */
        VERSION_EXECUTABLE("svn:executable", true, false, false, false),

        /**
         * If present on a file, the value indicates the file's mime type. This
         * allows the client to decide whether line-based contextual merging is
         * safe to perform during updates, and can also affect how the file
         * behaves when fetched via web browser.
         */
        VERSION_MIME_TYPE("svn:mime-type", true, false, false, false),

        /**
         * If present on a directory, the value is a list of unversioned file
         * patterns to be ignored by SVN status and other sub commands.
         */
        VERSION_IGNORE("svn:ignore", true, false, false, false),

        /**
         * If present on a file, the value tells the client how to expand
         * particular keywords within the file.
         */
        VERSION_KEYWORDS("svn:keywords", true, false, false, false),

        /**
         * If present on a file, the value tells the client how to manipulate
         * the file's line-endings in the working copy, and in exported trees.
         */
        VERSION_EOL_STYLE("svn:eol-style", true, false, false, false),

        /**
         * If present on a directory, the value is a multi-line list of other
         * paths and URLs the client should check out.
         */
        VERSION_EXTERNALS("svn:externals", true, false, false, false),

        /**
         * If present on a file, indicates that the file is not an ordinary
         * file, but a symbolic link or other special object.
         */
        VERSION_SPECIAL("svn:special", true, false, false, false),

        /**
         * If present on a file, tells the client to make the file read-only in
         * the working copy, as a reminder that the file should be locked
         * before editing begins.
         */
        VERSION_NEEDS_LOCK("svn:needs-lock", true, false, false, false),

        /**
         * Contains the authenticated user name of the person who created the
         * revision.
         *
         * @see RevisionProperties
         */
        REVISION_AUTHOR("svn:author", false, true, false, false),

        /**
         * Contains the UTC time the revision was created, in ISO 8601 format.
         * The value comes from the server machine's clock, not the client's.
         *
         * @see RevisionProperties
         */
        REVISION_DATE("svn:date", false, true, false, false),

        /**
         *
         *
         * @see RevisionProperties
         */
        REVISION_ORIGINAL_DATE("svn:original-date", false, true, false, false),

        /**
         * Contains the log message describing the revision.
         *
         * @see RevisionProperties
         */
        REVISION_LOG("svn:log", false, true, false, false),

        /**
         * If present, the revision was created via the auto-versioning
         * feature.
         *
         * @see RevisionProperties
         */
        REVISION_AUTOVERSIONED("svn:autoversioned", false, true, false, false),

        /**
         * For a SVN sync, the property holds the revision which is currently
         * copied. The property could be only defined as revision property for
         * revision 0.
         *
         * @see Revision0Properties
         */
        REVISION0_CURRENTLY_COPYING("svn:sync-currently-copying", false, false, true, false),

        /**
         * For a SVN sync, the property hold the original SVN URL from where is
         * synchronized. The property could be only defined as revision
         * property for revision 0.
         *
         * @see Revision0Properties
         */
        REVISION0_FROM_URL("svn:sync-from-url", false, false, true, false),

        /**
         * For a SVN sync, the property hold the UUID of the original SVN
         * instance from where it is synchronized. The property could be only
         * defined as revision property for revision 0.
         *
         * @see Revision0Properties
         */
        REVISION0_FROM_UUID("svn:sync-from-uuid", false, false, true, false),

        /**
         * For a SVN sync, the property holds the revision number of the last
         * synchronized revision. The property could be only defined as
         * revision property for revision 0.
         *
         * @see Revision0Properties
         */
        REVISION0_LAST_MERGED_REVISION("svn:sync-last-merged-rev", false, false, true, false),

        /**
         * For a SVN sync, the property holds the lock string if a SVN sync
         * runs. The property could be only defined as revision property for
         * revision 0.
         *
         * @see Revision0Properties
         */
        REVISION0_LOCK("svn:sync-lock", false, false, true, false),

        /**
         * System entry property for the UUID of the repository.
         */
        ENTRY_REPOSITORY_UUID("svn:entry:uuid", false, false, false, true),
        ENTRY_DIR_ENTRY_AUTHOR("svn:entry:last-author", false, false, false, true),
        ENTRY_DIR_ENTRY_REVISION("svn:entry:committed-rev", false, false, false, true),
        ENTRY_DIR_ENTRY_DATE("svn:entry:committed-date", false, false, false, true),

        /**
         * System entry property used for files to define the MD5 checksum.
         */
        ENTRY_DIR_ENTRY_CHECKSUM("svn:entry:checksum", false, false, false, true);

        private final String svnKey;

        /**
         * Is the property a versioned property? Means that the property could
         * be used for paths and files.
         *
         * @see #isVersioned()
         */
        private final boolean versioned;

        /**
         * Could the property used for revisions?
         *
         * @see #isRevision()
         */
        private final boolean revision;

        /**
         * Could the property used for revision 0?
         *
         * @see #isRevision0()
         */
        private final boolean revision0;

        /**
         * Is the property a standard entry SVN property?
         *
         * @see #isEntry()
         */
        private final boolean entry;

        private PropertyKey(final String _name,
                             final boolean _versioned,
                             final boolean _revision,
                             final boolean _revision0,
                             final boolean _entry)
        {
            this.svnKey = _name;
            this.versioned = _versioned;
            this.revision = _revision;
            this.revision0 = _revision0;
            this.entry = _entry;
            Properties.MAPSVNKEY2PROPKEY.put(this.svnKey, this);
        }

        public static PropertyKey valueBySVNKey(final String _key)
        {
            return Properties.MAPSVNKEY2PROPKEY.get(_key);
        }

        public String getSVNKey()
        {
            return this.svnKey;
        }

        public boolean isVersioned()
        {
            return this.versioned;
        }

        public boolean isRevision()
        {
            return this.revision;
        }

        public boolean isRevision0()
        {
            return this.revision0;
        }

        public boolean isEntry()
        {
            return this.entry;
        }
    }

    /**
     * Mapping between the SVN property key name and the property enumeration.
     *
     * @see PropertyKey#valueBySVNKey(String)
     */
    private final static Map<String, PropertyKey> MAPSVNKEY2PROPKEY = new HashMap<String, PropertyKey>();

    /**
     *
     * @param _key      property key
     * @return found value for related property key (or <code>null</code> if
     *         not found)
     */
    public String get(final PropertyKey _key)
    {
        return this.get(_key.svnKey);
    }

    /**
     *
     * @param _key      property key
     * @param _value    value for <code>_key</code>
     */
    public void put(final PropertyKey _key,
                    final String _value)
    {
        this.put(_key.svnKey, _value);
    }

    /**
     * Remove given property key from this map if present. Because the map uses
     * original string, the {@link PropertyKey#svnKey} is removed from this
     * map.
     *
     * @param _key  key to remove from this map
     */
    public void remove(final PropertyKey _key)
    {
        this.remove(_key.svnKey);
    }
}
