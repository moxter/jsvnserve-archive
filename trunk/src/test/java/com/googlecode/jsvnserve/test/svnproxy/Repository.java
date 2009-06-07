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

package com.googlecode.jsvnserve.test.svnproxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import org.tmatesoft.svn.core.ISVNDirEntryHandler;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.ISVNFileRevisionHandler;
import org.tmatesoft.svn.core.io.ISVNLocationEntryHandler;
import org.tmatesoft.svn.core.io.ISVNLockHandler;
import org.tmatesoft.svn.core.io.ISVNReporter;
import org.tmatesoft.svn.core.io.ISVNReporterBaton;
import org.tmatesoft.svn.core.io.SVNFileRevision;
import org.tmatesoft.svn.core.io.SVNLocationEntry;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
import org.tmatesoft.svn.core.io.diff.SVNDiffWindow;
import org.tmatesoft.svn.core.wc.SVNClientManager;

import com.googlecode.jsvnserve.api.CommitInfo;
import com.googlecode.jsvnserve.api.Depth;
import com.googlecode.jsvnserve.api.DirEntry;
import com.googlecode.jsvnserve.api.DirEntryList;
import com.googlecode.jsvnserve.api.IRepository;
import com.googlecode.jsvnserve.api.LocationEntries;
import com.googlecode.jsvnserve.api.LockDescriptionList;
import com.googlecode.jsvnserve.api.LogEntryList;
import com.googlecode.jsvnserve.api.OtherServerException;
import com.googlecode.jsvnserve.api.ReportList;
import com.googlecode.jsvnserve.api.ServerException;
import com.googlecode.jsvnserve.api.LockDescriptionList.LockDescription;
import com.googlecode.jsvnserve.api.LogEntryList.LogEntry;
import com.googlecode.jsvnserve.api.ReportList.AbstractCommand;
import com.googlecode.jsvnserve.api.ReportList.DeletePath;
import com.googlecode.jsvnserve.api.ReportList.SetPath;
import com.googlecode.jsvnserve.api.ServerException.ErrorCode;
import com.googlecode.jsvnserve.api.editorcommands.AbstractDelta;
import com.googlecode.jsvnserve.api.editorcommands.AbstractDeltaDirectory;
import com.googlecode.jsvnserve.api.editorcommands.AbstractDeltaFile;
import com.googlecode.jsvnserve.api.editorcommands.DeltaDelete;
import com.googlecode.jsvnserve.api.editorcommands.DeltaDirectoryCopy;
import com.googlecode.jsvnserve.api.editorcommands.DeltaDirectoryCreate;
import com.googlecode.jsvnserve.api.editorcommands.DeltaDirectoryOpen;
import com.googlecode.jsvnserve.api.editorcommands.DeltaFileCreate;
import com.googlecode.jsvnserve.api.editorcommands.DeltaFileOpen;
import com.googlecode.jsvnserve.api.editorcommands.DeltaRootOpen;
import com.googlecode.jsvnserve.api.editorcommands.DirectoryNotExistsException;
import com.googlecode.jsvnserve.api.editorcommands.EditorCommandSet;
import com.googlecode.jsvnserve.api.editorcommands.FileNotExistsException;
import com.googlecode.jsvnserve.api.filerevisions.FileRevision;
import com.googlecode.jsvnserve.api.filerevisions.FileRevisionsList;
import com.googlecode.jsvnserve.api.properties.Properties;
import com.googlecode.jsvnserve.api.properties.Revision0PropertyValues;
import com.googlecode.jsvnserve.api.properties.RevisionPropertyValues;
import com.googlecode.jsvnserve.util.Timestamp;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class Repository
            implements IRepository
{
    private final SVNURL baseUrl;

    /**
     *
     */
    final SVNRepository svnRepository;

    final SVNClientManager clientManager;

    /**
     * Repository path.
     *
     * @see #getRepositoryPath()
     */
    final String repositoryPath;

    /**
     * Root path of the repository.
     *
     * @see #getLocationPath()
     */
    String location;

    public Repository(final SVNURL _svnUrl,
                      final String _user,
                      final String _repositoryPath,
                      final String _location)
            throws SVNException
    {
        this.baseUrl = _svnUrl;
        this.repositoryPath = _repositoryPath;
        this.location = _location;
        this.clientManager = SVNClientManager.newInstance(null, new BasicAuthenticationManager(_user , _user));
        this.svnRepository = this.clientManager.createRepository(_svnUrl.appendPath(this.location, true), false);
    }

        public UUID getUUID()
        {
            try {
                return UUID.fromString(this.svnRepository.getRepositoryUUID(true));
            } catch (final SVNException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * @see #repositoryPath
         */
        public CharSequence getRepositoryPath()
        {
            return this.repositoryPath;
        }

        /**
         * Returns the current location path of the repository.
         *
         * @returns repository root path
         * @see #location
         */
        public CharSequence getLocationPath()
        {
            return this.location;
        }

        /**
         * Updates current location path within repository.
         *
         * @param _newPath  new location path
         * @see #location
         */
        public void setLocationPath(final CharSequence _newPath)
        {
            this.location = _newPath.toString();
            try {
                this.svnRepository.setLocation(this.baseUrl.appendPath(_newPath.toString(), false), false);
            } catch (final SVNException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        /**
         * @see com.googlecode.jsvnserve.api.IRepository#close()
         */
        public void close()
        {
        }


        public CommitInfo commit(final String _logMessage,
                                 final Map<String,String> _locks,
                                 final boolean _keepLocks,
                                 final Properties _revisionProps,
                                 final EditorCommandSet _editor)
                throws DirectoryNotExistsException, FileNotExistsException, OtherServerException
        {
            CommitInfo commitInfo = null;

            try {
                final ISVNEditor editor = this.svnRepository.getCommitEditor(_logMessage,
                                                                             _locks,
                                                                             _keepLocks,
                                                                             SVNProperties.wrap(_revisionProps),
                                                                             null);
                final Stack<AbstractDelta> stack = new Stack<AbstractDelta>();
                for (final AbstractDelta delta : _editor.getDeltas())  {
                    if (!stack.isEmpty())  {
                        final File file = new File(delta.getPath());
                        final String parentDir = (file.getParent() == null) ? "" : file.getParent();

                        while (!stack.peek().getPath().equals(parentDir))  {
                            if (stack.pop() instanceof AbstractDeltaDirectory)  {
                                editor.closeDir();
                            } else  {
                                // file close
                            }
                        }
                    }

                    stack.add(delta);

                    if (delta instanceof DeltaRootOpen)  {
                        editor.openRoot(-1);
                        for (final Map.Entry<String,String> entry : delta.entrySet())  {
                            editor.changeDirProperty(entry.getKey(),
                                                     SVNPropertyValue.create(entry.getValue()));
                        }
                    } else if (delta instanceof DeltaDirectoryCopy)  {
                        editor.addDir(delta.getPath(), delta.getCopiedPath(), delta.getCopiedRevision());
                        for (final Map.Entry<String,String> entry : delta.entrySet())  {
                            editor.changeDirProperty(entry.getKey(),
                                                      SVNPropertyValue.create(entry.getValue()));
                        }
                    } else if (delta instanceof DeltaDirectoryCreate)  {
                        editor.addDir(delta.getPath(), null, -1);
                        for (final Map.Entry<String,String> entry : delta.entrySet())  {
                            editor.changeDirProperty(entry.getKey(),
                                                     SVNPropertyValue.create(entry.getValue()));
                        }
                    } else if (delta instanceof DeltaDirectoryOpen)  {
                        editor.openDir(delta.getPath(), -1);
                        for (final Map.Entry<String,String> entry : delta.entrySet())  {
                            editor.changeDirProperty(entry.getKey(),
                                                     SVNPropertyValue.create(entry.getValue()));
                        }
                    } else if (delta instanceof DeltaFileOpen)  {
                        final DeltaFileOpen fileOpen = (DeltaFileOpen) delta;
                        editor.openFile(fileOpen.getPath(), fileOpen.getBaseRevision());
                        for (final Map.Entry<String,String> entry : fileOpen.entrySet())  {
                            editor.changeFileProperty(fileOpen.getPath(),
                                                      entry.getKey(),
                                                      SVNPropertyValue.create(entry.getValue()));
                        }
                        if (fileOpen.isContentChanged())  {
                            editor.applyTextDelta(fileOpen.getPath(), null);
                            SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
                            String checksum = deltaGenerator.sendDelta(delta.getPath(), fileOpen.getInputStream(), editor, true);
                        }
                    } else if (delta instanceof DeltaFileCreate)  {
                        final DeltaFileCreate fileCreate = (DeltaFileCreate) delta;
                        editor.addFile(fileCreate.getPath(), null, -1);
                        for (final Map.Entry<String,String> entry : fileCreate.entrySet())  {
                            editor.changeFileProperty(fileCreate.getPath(),
                                                      entry.getKey(),
                                                      SVNPropertyValue.create(entry.getValue()));
                        }
                        if (fileCreate.isContentChanged())  {
                            editor.applyTextDelta(fileCreate.getPath(), null);
                            SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
                            String checksum = deltaGenerator.sendDelta(delta.getPath(), fileCreate.getInputStream(), editor, true);
                        }
                    } else if (delta instanceof DeltaDelete)  {
                        final DeltaDelete delete = (DeltaDelete) delta;
                        editor.deleteEntry(delete.getPath(),
                                           (delete.getRevision() == null) ? -1 : delete.getRevision());
                    } else  {
                        editor.abortEdit();
                        throw new OtherServerException("class '" + delta.getClass() + "' not supported for commit!");
                    }
                }
                while (!stack.empty())  {
                    final AbstractDelta delta = stack.pop();
                    if (delta instanceof AbstractDeltaDirectory)  {
                        editor.closeDir();
                    } else if (delta instanceof DeltaFileCreate)  {
                        final AbstractDeltaFile fileDelta = (AbstractDeltaFile) delta;
                        editor.closeFile(fileDelta.getPath(), fileDelta.getChecksum());
                    }
                }

                final SVNCommitInfo svnCommitInfo = editor.closeEdit();

                commitInfo = new CommitInfo(svnCommitInfo.getNewRevision(),
                                            svnCommitInfo.getAuthor(),
                                            svnCommitInfo.getDate());
            } catch (final SVNException e) {
                final int errorCode = e.getErrorMessage().getErrorCode().getCode();
                if (errorCode == ErrorCode.SVN_ERR_FS_NOT_FOUND.code)  {
                    throw new FileNotExistsException("PATH");
                }
                if (errorCode == ErrorCode.SVN_ERR_FS_NOT_DIRECTORY.code)  {
                    throw new DirectoryNotExistsException("PATH");
                }
                throw new OtherServerException(e);
            }

            return commitInfo;
        }

        public LogEntryList getLog(final long _startRevision,
                                   final long _endRevision,
                                   final boolean _includeChangedPaths,
                                   final CharSequence... _paths)
        {
            final LogEntryList ret = new LogEntryList();

            try {
                final List<SVNLogEntry> logEntries = new ArrayList<SVNLogEntry>();

                final ISVNLogEntryHandler logHandler = new ISVNLogEntryHandler()  {
                    public void handleLogEntry(final SVNLogEntry _logEntry)
                            throws SVNException
                    {
                        logEntries.add(_logEntry);
                    }
                };

                final String[] paths = new String[_paths.length];
                for (int idx = 0; idx < _paths.length; idx++)
                {
                    paths[idx] = _paths[idx].toString();
                }

                this.svnRepository.log(paths,
                                       _startRevision,
                                       _endRevision,
                                       _includeChangedPaths,
                                       false, /* if true then copy history (if any) is not to be traversed*/
                                       logHandler);

                for (final SVNLogEntry logEntry : logEntries)  {
                    final LogEntry geha = ret.addLogEntry(logEntry.getRevision(), logEntry.getAuthor(), logEntry.getDate(), logEntry.getMessage());
                    if (_includeChangedPaths)  {
                        for (final Object logEntryPathObj : logEntry.getChangedPaths().values())  {
                            final SVNLogEntryPath logEntryPath = (SVNLogEntryPath) logEntryPathObj;
                            if (SVNLogEntryPath.TYPE_ADDED == logEntryPath.getType())  {
                                if (logEntryPath.getCopyPath() != null)  {
                                    geha.addCopied(logEntryPath.getPath(), logEntryPath.getCopyPath(), logEntryPath.getCopyRevision());
                                } else  {
                                    geha.addLogCreated(logEntryPath.getPath());
                                }
                            } else if (SVNLogEntryPath.TYPE_DELETED == logEntryPath.getType())  {
                                geha.addDeleted(logEntryPath.getPath());
                            } else if (SVNLogEntryPath.TYPE_MODIFIED == logEntryPath.getType())  {
                                geha.addModified(logEntryPath.getPath());
                            } else if (SVNLogEntryPath.TYPE_REPLACED == logEntryPath.getType())  {
                                geha.addRead(logEntryPath.getPath());
                            }
                        }
                    }
                }
            } catch (final SVNException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return ret;
        }

        public DirEntryList getDir(final Long _revision,
                                 final CharSequence _path,
                                 final boolean _retFileSize,
                                 final boolean _retHasProps,
                                 final boolean _retCreatedRev,
                                 final boolean _retModified,
                                 final boolean _retAuthor)
        {
            final DirEntryList ret = new DirEntryList();

            final SVNProperties props = new SVNProperties();

            try {
                final Set<SVNDirEntry> dirEntries = new HashSet<SVNDirEntry>();
                final ISVNDirEntryHandler handler = new ISVNDirEntryHandler() {
                    public void handleDirEntry(final SVNDirEntry _dirEntry)
                    {
                        dirEntries.add(_dirEntry);
                    }
                };
                this.svnRepository.getDir(_path.toString(), _revision, props, 63, handler);
                for (final SVNDirEntry dirEntry : dirEntries)  {
                    if (dirEntry.getKind().equals(SVNNodeKind.DIR))  {
                        ret.addDirectory(dirEntry.getName(),
                                         dirEntry.getRevision(),
                                         Timestamp.valueOf(dirEntry.getDate()),
                                         dirEntry.getAuthor());
                    } else if (dirEntry.getKind().equals(SVNNodeKind.FILE))  {
                        ret.addFile(dirEntry.getName(),
                                    dirEntry.getRevision(),
                                    Timestamp.valueOf(dirEntry.getDate()),
                                    dirEntry.getAuthor(),
                                    (int) dirEntry.getSize());
                    } else  {
                        System.err.println("wrong directory entry " + dirEntry);
                    }
                }
            } catch (final SVNException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return ret;
        }

        public InputStream getFile(final Long _revision,
                                   final CharSequence _path)
        {
            try {
                final File temp = File.createTempFile("SVN", ".temp");
                temp.deleteOnExit();
                final OutputStream out = new FileOutputStream(temp);
                try  {
                    // if a absolute path is defined, the absolute path must be
                    // defined depending the root path of the SVN repository
                    final String path;
                    if (_path.toString().startsWith("/"))  {
                        final String absPath = this.baseUrl.appendPath(_path.toString(), false).getPath();
                        final String rootPath = this.svnRepository.getRepositoryRoot(false).getPath();
                        path = absPath.substring(rootPath.length());
                    } else  {
                        path = _path.toString();
                    }
                    this.svnRepository.getFile(path,
                                               _revision,
                                               null,
                                               out);
                } finally  {
                    out.flush();
                    out.close();
                }
                final InputStream in = new FileInputStream(temp);
                return in;
            } catch (final SVNException e)  {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    public FileRevisionsList getFileRevs(final String _path,
                                         final long _startRev,
                                         final long _endRev,
                                         final boolean _mergeInfo)
            throws ServerException
    {
        final FileRevisionsList revList = new FileRevisionsList();
        try {
            this.svnRepository.getFileRevisions(_path, _startRev, _endRev, _mergeInfo,
                    new ISVNFileRevisionHandler() {

                        private SVNFileRevision svnFileRevision;

                        private boolean contentModified;

                        @SuppressWarnings("unchecked")
                        public void closeRevision(String _path)
                        {
                            final FileRevision fileRevision = new FileRevision(this.svnFileRevision.getRevision(),
                                                                               this.svnFileRevision.getPath(),
                                                                               this.contentModified,
                                                                               this.svnFileRevision.isResultOfMerge());
                            revList.add(fileRevision);
                            for (final Map.Entry<String,SVNPropertyValue> entry
                                    : ((Map<String,SVNPropertyValue>) this.svnFileRevision.getRevisionProperties().asMap()).entrySet())  {
                                fileRevision.getRevisionProperties().put(entry.getKey(), entry.getValue().getString());
                            }
                            for (final Map.Entry<String,SVNPropertyValue> entry
                                    : ((Map<String,SVNPropertyValue>) this.svnFileRevision.getPropertiesDelta().asMap()).entrySet())  {
                                fileRevision.getFileDeltaProperties().put(entry.getKey(), entry.getValue().getString());
                            }
                            this.svnFileRevision = null;
                            this.contentModified = false;
                        }

                        public void openRevision(SVNFileRevision _svnFileRevision)
                        {
                            this.svnFileRevision = _svnFileRevision;
                        }

                        public void applyTextDelta(String s, String s1)
                        {
                           this.contentModified = true;
                        }

                        public OutputStream textDeltaChunk(String s, SVNDiffWindow svndiffwindow)
                        {
                            return null;
                        }

                        public void textDeltaEnd(String s)
                        {
                        }
                });
        } catch (final SVNException e) {
            throw new ServerException(e.getMessage(), e);
        }
        return revList;
    }

        public DirEntry stat(final Long _revision,
                             final CharSequence _path,
                             final boolean _includeProperties)
                throws ServerException
        {
            DirEntry dirEntry = null;

            try {
                final SVNNodeKind nodeKind = this.svnRepository.checkPath(_path.toString(),
                                                                          (_revision == null) ? -1 : _revision);
                final SVNProperties props = new SVNProperties();
                if (SVNNodeKind.DIR.equals(nodeKind))  {
                    this.svnRepository.getDir(_path.toString(),
                                              (_revision == null) ? -1 : _revision,
                                              props,
                                              0,
                                              (Collection<?>) null);
                    dirEntry = DirEntry.createDirectory(_path.toString(), null, null, null);
                } else if (SVNNodeKind.FILE.equals(nodeKind))  {
                    this.svnRepository.getFile(_path.toString(),
                                               (_revision == null) ? -1 : _revision,
                                               props,
                                               null);
                    dirEntry = DirEntry.createFile(_path.toString(), null, null, null, 0, null);
                }
System.out.println("nodeKind="+nodeKind);

                if (dirEntry != null)  {
                    for (final Map.Entry<String,SVNPropertyValue> entry
                                : ((Map<String,SVNPropertyValue>) props.asMap()).entrySet())  {
                        if (!"svn:entry:uuid".equals(entry.getKey()))  {
                            dirEntry.put(entry.getKey(),
                                         (entry.getValue() != null) ? entry.getValue().getString() : null);
                        }
                    }
                }
            } catch (final SVNException e) {
                throw new ServerException(e.getMessage(), e);
            }
            return dirEntry;
        }

        public long getLatestRevision()
        {
            try {
                return this.svnRepository.getLatestRevision();
            } catch (final SVNException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return 0;
        }

        @SuppressWarnings("unchecked")
        public RevisionPropertyValues getRevisionProperties(final long _revision)
                throws ServerException
        {
            final RevisionPropertyValues ret = new RevisionPropertyValues();
            try {
                final SVNProperties props = this.svnRepository.getRevisionProperties(_revision, null);
                for (final Map.Entry<String,SVNPropertyValue> entry
                        : ((Map<String,SVNPropertyValue>) props.asMap()).entrySet())  {
                    ret.put(entry.getKey(), entry.getValue().getString());
                }
            } catch (final SVNException ex) {
                throw new ServerException(ex.getMessage(), ex);
            }
            return ret;
        }

        @SuppressWarnings("unchecked")
        public Revision0PropertyValues getRevision0Properties()
                throws ServerException
        {
            final Revision0PropertyValues ret = new Revision0PropertyValues();
            try {
                final SVNProperties props = this.svnRepository.getRevisionProperties(0, null);
                for (final Map.Entry<String,SVNPropertyValue> entry
                        : ((Map<String,SVNPropertyValue>) props.asMap()).entrySet())  {
                    ret.put(entry.getKey(), entry.getValue().getString());
                }
            } catch (final SVNException ex) {
                throw new ServerException(ex.getMessage(), ex);
            }
            return ret;
        }

        public LockDescriptionList lock(final String _comment,
                                        final boolean _stealLock,
                                        final Map<String,Long> _pathsWithRevision)
                throws ServerException
        {
            final LockDescriptionList locks = new LockDescriptionList();

            try {
                final ISVNLockHandler lockHandler = new ISVNLockHandler()  {
                    public void handleLock(final String _path,
                                           final SVNLock _svnLock,
                                           final SVNErrorMessage _svnErrorMessage)
                            throws SVNException
                    {
                        if (_svnLock != null)  {
                            locks.addLockedSuccessfully(_path,
                                                  _svnLock.getID(),
                                                  _svnLock.getOwner(),
                                                  _svnLock.getComment(),
                                                  _svnLock.getCreationDate(),
                                                  _svnLock.getExpirationDate());
                        } else  {
                            locks.addFailed(_path, _svnErrorMessage.getFullMessage());
                        }
                    }

                    /** Not needed. */
                    public void handleUnlock(final String _path,
                                             final SVNLock _svnLock,
                                             final SVNErrorMessage _svnerrormessage)
                            throws SVNException
                    {
                    }
                };

                this.svnRepository.lock(_pathsWithRevision, _comment, _stealLock, lockHandler);
            } catch (final SVNException e) {
                e.printStackTrace();
            }
            return locks;
        }

        public LockDescriptionList unlock(final boolean _breakLock,
                           final Map<String,String> _pathsWithTokens)
        {
            final LockDescriptionList locks = new LockDescriptionList();

            try {
                final ISVNLockHandler lockHandler = new ISVNLockHandler()  {
                    /** Not needed. */
                    public void handleLock(final String _path,
                                           final SVNLock _svnLock,
                                           final SVNErrorMessage _svnErrorMessage)
                            throws SVNException
                    {
                    }
                    public void handleUnlock(final String _path,
                                             final SVNLock _svnLock,
                                             final SVNErrorMessage _svnErrorMessage)
                            throws SVNException
                    {
                        if (_svnErrorMessage != null)  {
                            locks.addFailed(_path, _svnErrorMessage.getFullMessage());
                        } else  {
                            locks.addUnlockedSuccessfully(_path);
                        }
                    }
                };

                this.svnRepository.unlock(_pathsWithTokens, _breakLock, lockHandler);
            } catch (final SVNException e) {
                e.printStackTrace();
            }
            return locks;

        }

        public LockDescription getFileLock(final CharSequence _filePath)
        {
            LockDescription lockDesc = null;

            try {
                final SVNLock svnLock = this.svnRepository.getLock(_filePath.toString());
                if (svnLock != null)  {
                    lockDesc = LockDescription.create(svnLock.getPath(),
                                                      svnLock.getID(),
                                                      svnLock.getOwner(),
                                                      svnLock.getComment(),
                                                      svnLock.getCreationDate(),
                                                      svnLock.getExpirationDate());
                }
            } catch (final SVNException e) {
                e.printStackTrace();
            }

            return lockDesc;
        }

        public LockDescriptionList getLocks(final CharSequence _path)
        {
            final LockDescriptionList lockList = new LockDescriptionList();
            try {
                final SVNLock[] svnLocks = this.svnRepository.getLocks(_path.toString());
                if (svnLocks != null)  {
                    for (final SVNLock svnLock : svnLocks)  {
                        lockList.addLockedSuccessfully(svnLock.getPath(),
                                                 svnLock.getID(),
                                                 svnLock.getOwner(),
                                                 svnLock.getComment(),
                                                 svnLock.getCreationDate(),
                                                 svnLock.getExpirationDate());
                    }
                }
            } catch (final SVNException e) {
                e.printStackTrace();
            }
            return lockList;
        }

        public LocationEntries getLocations(final long _pegRevision,
                                            final String _path,
                                            final long... _revisions)
        {
            final LocationEntries entries = new LocationEntries();
            try {
                this.svnRepository.getLocations(_path, _pegRevision, _revisions,
                        new ISVNLocationEntryHandler()  {

                            public void handleLocationEntry(final SVNLocationEntry _svnlocationentry)
                                    throws SVNException
                            {
                                entries.add(_svnlocationentry.getRevision(),
                                            _svnlocationentry.getPath());
                            }
                        });
            } catch (final SVNException e) {
                e.printStackTrace();
            }

            return entries;
        }


        public EditorCommandSet getSwitchEditor(final String _newPath,
                                                final Long _revision,
                                                final String _path,
                                                final Depth _depth,
                                                final ReportList _report)
                throws ServerException
        {

            final SVNEditor editor = new SVNEditor();

            SVNDepth svnDepth = SVNDepth.UNKNOWN;
            switch (_depth)  {
                case EXCLUDE:       svnDepth = SVNDepth.EXCLUDE;break;
                case EMPTY:         svnDepth = SVNDepth.EMPTY;break;
                case FILES:         svnDepth = SVNDepth.FILES;break;
                case IMMEDIATES:    svnDepth = SVNDepth.IMMEDIATES;break;
                case INFINITY:      svnDepth = SVNDepth.INFINITY;break;
            }


try {
    System.out.println("_path="+_path);
System.out.println("_newPath="+_newPath);
System.out.println("this.baseUrl="+this.baseUrl);
System.out.println("this.baseUrl.appendPath(_newPath, false)="+this.baseUrl.appendPath(_newPath, false).getPath());
//editor.startIdx = _newPath.length();

editor.newPath = _path;
editor.orgPath = _newPath;

    // TODO: path could not start with / (why?)
    this.svnRepository.update(this.baseUrl.appendPath(_newPath, false),
                              (_revision != null) ? _revision : -1,
                              _path,
                              svnDepth,
                              new ISVNReporterBaton()  {

        public void report(final ISVNReporter isvnreporter) throws SVNException
        {
            for (final AbstractCommand command : _report.values())  {
                if (command instanceof SetPath)  {
                    final SetPath setPath = (SetPath) command;
                    isvnreporter.setPath(setPath.getPath(),
                                         setPath.getLockToken(),
                                         setPath.getRevision(),
                                         SVNDepth.INFINITY,
                                         setPath.isStartEmpty());
                } else if (command instanceof DeletePath)  {
                    isvnreporter.deletePath(command.getPath());
                }
            }
            isvnreporter.finishReport();
            System.out.println("isvnreporter="+isvnreporter);
        }

    }, editor);
} catch (final SVNException e) {
    throw new ServerException(e.getMessage(), e);
}

System.out.println("editor.deltaEditor="+editor.deltaEditor);

            return editor.deltaEditor;
        }

        public EditorCommandSet getStatus(final Long _revision,
                                  final String _path,
                                  final Depth _depth,
                                  final ReportList _report)
                throws ServerException
        {
            final SVNEditor editor = new SVNEditor();

            SVNDepth svnDepth = SVNDepth.UNKNOWN;
            switch (_depth)  {
                case EXCLUDE:       svnDepth = SVNDepth.EXCLUDE;break;
                case EMPTY:         svnDepth = SVNDepth.EMPTY;break;
                case FILES:         svnDepth = SVNDepth.FILES;break;
                case IMMEDIATES:    svnDepth = SVNDepth.IMMEDIATES;break;
                case INFINITY:      svnDepth = SVNDepth.INFINITY;break;
            }

try {
    // TODO: path could not start with / (why?)
    this.svnRepository.status((_revision != null) ? _revision : -1,
                              _path,
                              svnDepth,
                              new ISVNReporterBaton()  {

        public void report(final ISVNReporter isvnreporter) throws SVNException
        {
            for (final AbstractCommand command : _report.values())  {
                if (command instanceof SetPath)  {
                    final SetPath setPath = (SetPath) command;
                    isvnreporter.setPath(setPath.getPath(),
                                         setPath.getLockToken(),
                                         setPath.getRevision(),
                                         SVNDepth.INFINITY,
                                         setPath.isStartEmpty());
                } else if (command instanceof DeletePath)  {
                    isvnreporter.deletePath(command.getPath());
                }
            }
            isvnreporter.finishReport();
            System.out.println("isvnreporter="+isvnreporter);
        }

    }, editor);
} catch (final SVNException e) {
    throw new ServerException(e.getMessage(), e);
}

            return editor.deltaEditor;
        }

    class SVNEditor
            implements ISVNEditor
    {
        EditorCommandSet deltaEditor;

String newPath;
String orgPath;

        private final Stack<AbstractDelta> stack = new Stack<AbstractDelta>();

        public void abortEdit() throws SVNException
        {
            System.out.println("abortEdit()");
        }

        public void absentDir(final String s) throws SVNException
        {
            System.out.println("absentDir("+s+")");
        }

        public void addDir(final String _path,
                           final String _copiedPath,
                           final long _copiedRevision)
                throws SVNException
        {
            System.out.println("addDir("+_path+","+_copiedPath+","+_copiedRevision+")");
            final AbstractDelta delta;
            if ((_copiedPath != null) && (_copiedRevision >= 0))  {
delta= null;
            } else  {
                delta = this.deltaEditor.createDir(_path);
            }
            this.stack.add(delta);
        }

        public void changeDirProperty(final String _propKey,
                                      final SVNPropertyValue _propValue)
                throws SVNException
        {
            System.out.println("changeDirProperty("+_propKey+","+_propValue+")");
            if (!"svn:entry:uuid".equals(_propKey))  {
                this.stack.peek().put(_propKey, (_propValue != null) ? _propValue.toString() : null);
            }
        }

        public void closeDir() throws SVNException
        {
            System.out.println("closeDir()");
            this.stack.pop();
        }

        public void openDir(final String _path, final long _revision)
        {
            System.out.println("openDir("+_path+","+_revision+")");
            final AbstractDelta delta = this.deltaEditor.updateDir(_path, (_revision >= 0) ? _revision : null);
            this.stack.add(delta);
        }

        public void absentFile(final String s)
        {
            System.out.println("absentFile("+s+")");
        }

        public void addFile(final String _path,
                            final String _copiedPath,
                            final long _copiedRevision)
        {
            System.out.println("addFile("+_path+","+_copiedPath+","+_copiedRevision+")");
            final AbstractDelta delta;
            if ((_copiedPath != null) && (_copiedRevision >= 0))  {
delta= null;
            } else  {
                // must the file get on the server from original path?
                if (this.newPath != null)  {
                    delta = this.deltaEditor.createFile(_path,
                                                        this.orgPath + _path.substring(this.newPath.length()));
                } else  {
                    delta = this.deltaEditor.createFile(_path);
                }
            }
            this.stack.add(delta);
        }

        public void changeFileProperty(final String _path,
                                       final String _propKey,
                                       final SVNPropertyValue _propValue)
        {
            System.out.println("changeFileProperty("+_propKey+","+_propValue+")");
            if (!"svn:entry:uuid".equals(_propKey))  {
                this.stack.peek().put(_propKey, (_propValue != null) ? _propValue.toString() : null);
            }
        }

        public void closeFile(final String s, final String s1)
        {
            System.out.println("closeFile("+s+","+s1+")");
            this.stack.pop();
        }

        public void openFile(final String _path,
                             final long _revision)
        {
            System.out.println("openFile("+_path+","+_revision+")");
            final AbstractDelta delta = this.deltaEditor.updateFile(_path, (_revision >= 0) ? _revision : null, _path);
            this.stack.add(delta);
        }

        public SVNCommitInfo closeEdit()
        {
            System.out.println("closeEdit()");
            return null;
        }

        public void deleteEntry(final String _path, final long _revision)
        {
            System.out.println("deleteEntry("+_path+","+_revision+")");
            this.deltaEditor.delete(_path, (_revision >= 0) ? _revision : null);
        }

        public void openRoot(final long _revision) throws SVNException
        {
            System.out.println("openRoot("+_revision+")");
            final AbstractDelta delta = this.deltaEditor.updateRoot((_revision >= 0) ? _revision : null);
            this.stack.add(delta);
        }

        public void targetRevision(final long _targetRevision)
        {
            this.deltaEditor = new EditorCommandSet(_targetRevision);
        }

        public void applyTextDelta(final String s, final String _baseMD5) throws SVNException
        {
            System.out.println("applyTextDelta("+s+","+_baseMD5+")");
            ((AbstractDeltaFile) this.stack.peek()).setBaseCheckSumMD5(_baseMD5);
        }

        public OutputStream textDeltaChunk(final String s, final SVNDiffWindow svndiffwindow)
                throws SVNException
        {
            System.out.println("textDeltaChunk("+s+","+svndiffwindow.toString()+")");
            return null;
        }

        public void textDeltaEnd(final String s) throws SVNException
        {
            System.out.println("textDeltaEnd("+s+")");
        }
    }
}
