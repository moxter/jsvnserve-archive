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
import com.googlecode.jsvnserve.api.editorcommands.DirectoryNotExistsException;
import com.googlecode.jsvnserve.api.editorcommands.EditorCommandSet;
import com.googlecode.jsvnserve.api.editorcommands.FileNotExistsException;
import com.googlecode.jsvnserve.api.filerevisions.FileRevisionsList;
import com.googlecode.jsvnserve.api.properties.Properties;
import com.googlecode.jsvnserve.api.properties.Revision0PropertyValues;
import com.googlecode.jsvnserve.api.properties.RevisionPropertyValues;

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
     * @see #getLocationPath()
     * @see IRepositoryFactory#createRepository(String, String)
     */
    public CharSequence getRepositoryPath();

    /**
     * Returns the location path within the repository. For explanation see
     * {@link IRepositoryFactory#createRepository(String, String)}.
     *
     * @return root path within the repository
     * @see #getRepositoryPath()
     * @see #setLocationPath(CharSequence)
     * @see IRepositoryFactory#createRepository(String, String)
     */
    public CharSequence getLocationPath();

    /**
     * A reparent of the repository itself is done. This means the current
     * location path within the repository is changed.
     *
     * @param _newPath  new location path
     * @see #getLocationPath()
     */
    public void setLocationPath(final CharSequence _newPath);

    /**
     * Must return for this repository current latest revision.
     *
     * @return current latest revision
     */
    public long getLatestRevision();

    /**
     * Returns for given revision (not revision 0) all revision properties.
     *
     * @param _revision     revision for which the properties are searched
     * @return property information for given <code>_revision</code>
     * @throws ServerException if e.g. <code>_revision</code> does not exists
     *                         in repository
     * @see #getRevision0Properties()
     */
    public RevisionPropertyValues getRevisionProperties(final long _revision)
            throws ServerException;

    /**
     * Returns for revision 0 all revision property. The properties for
     * revision 0 are used from SVN to hold about information about
     * synchronizes with other SVN instances.
     *
     * @return property information for revision 0
     * @throws ServerException
     * @see #getRevisionProperties(long)
     */
    public Revision0PropertyValues getRevision0Properties()
            throws ServerException;

    /**
     * Method is called from the session, just before closing the session
     * itself. It can be used to implement closing and cleaning routines.
     */
    public void close();

    /**
     * Commit changes to the repository. The path root of the commit is current
     * repository location (see {@link #getLocationPath}).
     *
     * @param _logMessage       commit message (log of the revision)
     * @param _locks            map of locks with path as key and lock token as
     *                          value; the lock token must correct for each
     *                          committed and locked path
     * @param _keepLocks        <i>true</i> means to keep existing locks;
     *                          <i>false</i> to release all locks after commit
     * @param _revisionProps    custom specific revision properties
     * @param _editor           editor command set
     * @throws DirectoryNotExistsException  if a directory in the editor
     *                                      command set <code>_editor</code>
     *                                      does not exists
     * @throws FileNotExistsException       if a file in the editor command set
     *                                      <code>_editor</code> does not
     *                                      exists
     * @throws OtherServerException         if commit failed in all other cases
     */
    public CommitInfo commit(final String _logMessage,
                             final Map<String,String> _locks,
                             final boolean _keepLocks,
                             final Properties _revisionProps,
                             final EditorCommandSet _editor)
            throws DirectoryNotExistsException, FileNotExistsException, OtherServerException;

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
     * <p>Returns interesting file revisions for the specified file.</p>
     * <p>The list of interesting file revisions are represented by an instance
     * of {@link FileRevisionsList}. The list of file revisions must only
     * include those revisions in which the file was changed (e.g. file content
     * or file properties).</p>
     *
     * @param _path         path of the file
     * @param _startRev     revision to start from
     * @param _endRev       revision to end at
     * @param _mergeInfo    if <i>true</i> merged revision must be also
     *                      included
     * @return list of interesting file revisions
     * @throws ServerException if interesting file revisions could not be
     *                         returned
     */
    public FileRevisionsList getFileRevs(final String _path,
                                         final long _startRev,
                                         final long _endRev,
                                         final boolean _mergeInfo)
            throws ServerException;

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
     * @return directory entry if the path is a file or a directory; otherwise
     *         <code>null</code> must be returned
     */
    public DirEntry stat(final Long _revision,
                         final CharSequence _path,
                         final boolean _includeProperties)
            throws ServerException;

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
     *                              is <code>null</code> and means the HEAD
     *                              revision
     * @param _path                 udate path
     * @param _depth                depth for update, determines the scope
     * @param _report               report of current directory structure of
     *                              the client
     * @return
     */
    public EditorCommandSet getStatus(final Long _revision,
                                      final String _path,
                                      final Depth _depth,
                                      final ReportList _report)
            throws ServerException;

    /**
     * <p>Returns the path locations in revision history. The location of a
     * path could change from revision to revision, so the method allows to
     * trace the information of the location in different revision.</p>
     * <p>For each revision in <code>_revisions</code> a location entry is
     * returned if for the revision the path exists (maybe with other name).
     * </p>
     * <p><b>Example</b><br/>
     * In revision 1 path &quot;temp&quot; is created. In revision 2
     * &quot;temp&quot; is copied to &quot;tempcopy&quot;. In revision 3
     * &quot;tempnew&quot; is created. Current head revision is 8. So
     * <code>_pegRevision</code> is set to 8.
     * <ul>
     * <li>If <code>_path</code> is set to &quot;tempcopy&quot; and the list of
     *     interesting revisions includes 1, the list of location entries
     *     returns in this case &quot;temp&quot;, because in revision 2 the
     *     path was copied from &quot;temp&quot;.</li>
     * <li>If <code>_path</code> is set to &quot;tempnew&quot; and the list of
     *     interesting revisions includes 1, for revision 1 is no location
     *     entry returned, because the path was created first in revision 3.
     *     </li>
     * </ul></p>
     *
     * @param _pegRevision  revision in which <code>_path</code> is first
     *                      looked up
     * @param _path         path for which the locations are looked up
     * @param _revisions    list of revisions for which the locations are
     *                      looked up; if for a revision the <code>_path</code>
     *                      doesn't exists, the revision is ignored (and not
     *                      returned in the location entries)
     * @return all existing location entries depending on the
     *         <code>_revisions</code>
     */
    public LocationEntries getLocations(final long _pegRevision,
                                        final String _path,
                                        final long... _revisions);
}
