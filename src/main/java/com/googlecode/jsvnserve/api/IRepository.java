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

import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.googlecode.jsvnserve.api.LockDescriptionList.LockDescription;
import com.googlecode.jsvnserve.api.LogEntryList.LogEntry;
import com.googlecode.jsvnserve.api.delta.Editor;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public interface IRepository
{
    /**
     * Returns the universally unique identifier (UUID) of this repository.
     *
     * @return universally unique identifier of the repository
     */
    public UUID getUUID();

    /**
     * Returns the path of the repository which implements this repository. For
     * explanation see
     * {@link IRepositoryFactory#createRepository(String, String)}.
     *
     * @return repository path
     * @see #getRootPath()
     * @see IRepositoryFactory#createRepository(String, String)
     */
    public CharSequence getRepositoryPath();

    /**
     * Returns the root path within the repository. For explanation see
     * {@link IRepositoryFactory#createRepository(String, String)}.
     *
     * @return root path within the repository
     * @see #getRepositoryPath()
     * @see IRepositoryFactory#createRepository(String, String)
     */
    public CharSequence getRootPath();

    /**
     * Must return for this repository current latest revision.
     *
     * @return current latest revision
     */
    public long getLatestRevision();

    /**
     *
     * @param _revision         revision for which the entries of a directory
     *                          must be returned
     * @param _path             path of the directory
     * @param _retFileSize      must the size of files returned?
     * @param _retHasProps      must the information that properties exists
     *                          returned?
     * @param _retCreatedRev    must the created revision returned?
     * @param _retModified      must the modified date time returned?
     * @param _retAuthor        must the author returned?
     * @return
     */
    public DirEntryList getDir(final Long _revision,
                               final CharSequence _path,
                               final boolean _retFileSize,
                               final boolean _retHasProps,
                               final boolean _retCreatedRev,
                               final boolean _retModified,
                               final boolean _retAuthor);

    /**
     *
     * @param _revision
     * @param _path
     * @return
     */
    public InputStream getFile(final Long _revision,
                               final CharSequence _path);

    /**
     * Lock given file paths (depending on the revisions).
     *
     * @param _comment              comment of the locking
     * @param _stealLock            could existing locking overwritten?
     * @param _pathsWithRevision    map with all file paths and related
     *                              revision to lock
     * @return list of successfully or failed lockings
     * @throws ServerException if lock failed
     */
    public LockDescriptionList lock(final String _comment,
                                    final boolean _stealLock,
                                    final Map<String,Long> _pathsWithRevision)
            throws ServerException;

    /**
     * Removes all locks for given file paths.
     *
     * @param _breakLock        <i>true</i> to remove the lock in any case;
     *                          i.e. to &quot;break&quot; the lock
     * @param _pathsWithTokens  map with all file paths with depending tokens
     *                          to unlock
     */
    public LockDescriptionList unlock(final boolean _breakLock,
                                      final Map<String,String> _pathsWithTokens);

    /**
     * Checks for given file path if the file is locked. If the file is locked,
     * the related lock description is returned.
     *
     * @param _filePath     file paths for which must be checked if locked
     * @return if file is locked the lock description of file is returned;
     *         otherwise <code>null</code> is returned
     */
    public LockDescription getFileLock(final CharSequence _filePath);

    /**
     * Returns all locks on or below given path, that is if the repository
     * entry (located at the path) is a directory then the method returns
     * locks of all locked files (if any) in it.
     *
     * @param _paths    path under which locks are to be retrieved
     * @return list of all locks for given path
     */
    public LockDescriptionList getLocks(final CharSequence _path);

    /**
     *
     * @param _revision             searched revision (or <code>null</code> if
     *                              head revision is searched)
     * @param _path                 searched path
     * @param _includeProperties    must the properties included?
     * @return
     */
    public DirEntry stat(final Long _revision,
                         final CharSequence _path,
                         final boolean _includeProperties);

    /**
     * <p>Returns for given paths depending on the start and end revision the
     * related log.</p>
     * <p>If the changed paths must be included, for each log entry (created
     * with {@link LogEntryList#addLogEntry(long, String, Date, String)}) all
     * changed paths must be added by calling all &quot;add&quot; methods from
     * class {@link LogEntry}:
     * <ul>
     * <li>{@link LogEntry#addCopied(String, String, long)}</li>
     * <li>{@link LogEntry#addDeleted(String)}</li>
     * <li>{@link LogEntry#addLogCreated(String)}</li>
     * <li>{@link LogEntry#addModified(String)}</li>
     * <li>{@link LogEntry#addRead(String)}</li>
     * <li>{@link LogEntry#addRenamed(String, String)}</li>
     * </ul></p>
     *
     * @param _startRevision        revision number to start from
     * @param _endRevision          revision number to end at
     * @param _includeChangedPaths  must also the changed paths included?
     * @param _paths                paths for which the log is searched
     * @return list of log entries
     */
    public LogEntryList getLog(final long _startRevision,
                               final long _endRevision,
                               final boolean _includeChangedPaths,
                               final CharSequence... _paths);

    /**
     *
     * @param _revision             update revision, if not specified the value
     *                              is <code>-1</code> and means the HEAD
     *                              revision
     * @param _path                 udate path
     * @param _depth                depth for update, determines the scope
     * @param _sendCopyFromParams   ?????
     * @param _report               report of current directory structure of
     *                              the client
     * @return
     */
    public Editor update(final long _revision,
                              final String _path,
                              final Depth _depth,
                              final boolean _sendCopyFromParams,
                              final ReportList _report);
}
