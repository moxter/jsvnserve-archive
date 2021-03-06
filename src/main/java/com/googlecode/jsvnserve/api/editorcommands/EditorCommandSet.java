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

package com.googlecode.jsvnserve.api.editorcommands;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import com.googlecode.jsvnserve.SVNSessionStreams;
import com.googlecode.jsvnserve.api.ServerException;
import com.googlecode.jsvnserve.api.ServerException.ErrorCode;
import com.googlecode.jsvnserve.element.AbstractElement;
import com.googlecode.jsvnserve.element.ListElement;
import com.googlecode.jsvnserve.element.WordElement.Word;
import com.googlecode.jsvnserve.util.Client2Server;
import com.googlecode.jsvnserve.util.Server2Client;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class EditorCommandSet
{
    /**
     * Stores one delta depending from the path and the related delta.
     *
     * @see #getDelta(String)
     * @see #getDeltas()
     * @see #addDelta(AbstractDelta)
     */
    private final Map<String,AbstractDelta> deltas = new TreeMap<String,AbstractDelta>();

    /**
     * Maps between the token and the related real path name.
     *
     * @see #addDelta(AbstractDelta)
     */
    private final Map<String,AbstractDelta> mapToken2Delta = new HashMap<String,AbstractDelta>();

    /**
     * Current token index used to give each directory path a specific token
     * index.
     */
    int tokenIndex;

    /**
     * The revision for which this delta is defined for.
     *
     * @see #DeltaEditor(long)
     */
    private final long targetRevision;

    /**
     *
     * @param _targetRevision   target revision described from this delta; if
     *                          <code>-1</code> the editor command set is
     *                          used to read from SVN client
     * @see #targetRevision
     */
    public EditorCommandSet(final long _targetRevision)
    {
        this.targetRevision = _targetRevision;
    }

    /**
     * Returns the target revision described from this delta. The method is the
     * getter method for {@link #targetRevision}.
     *
     * @return target revision described from this delta
     * @see #targetRevision
     */
    public long getTargetRevision()
    {
        return this.targetRevision;
    }

    /**
     *
     * @return delta collection (values from {@link #deltas})
     * @see #deltas
     */
    public Collection<AbstractDelta> getDeltas()
    {
        return this.deltas.values();
    }

    /**
     * Adds a new delta to this editor command set. Depending on the path
     * to new delta is put into the {@link #deltas} map. Also depending on the
     * token the related path is stored.
     *
     * @param _delta    delta to add
     * @see #deltas
     * @see #mapToken2Path
     */
    protected void addDelta(final AbstractDelta _delta)
    {
        this.deltas.put(_delta.getPath(), _delta);
        this.mapToken2Delta.put(_delta.getToken(), _delta);
    }

    /**
     *
     * @param _path     path for which the delta is searched
     * @return searched delta or <code>null</code> if not found
     * @see #deltas
     */
    public AbstractDelta getDelta(final String _path)
    {
        return this.deltas.get(_path);
    }

    private String getNewToken(final char _prefix)
    {
        return new StringBuilder().append(_prefix).append(++this.tokenIndex).toString();
    }

    public AbstractDelta updateRoot(final Long _revision)
    {
        final AbstractDelta delta = new DeltaRootOpen(this.getNewToken('d'), _revision);
        this.addDelta(delta);
        return delta;
    }

    public AbstractDelta createDir(final String _path)
    {
        final AbstractDelta delta = new DeltaDirectoryCreate(this.getNewToken('d'), _path);
        this.addDelta(delta);
        return delta;
    }

    public AbstractDelta updateDir(final String _path,
                                   final Long _revision)
    {
        final AbstractDelta delta = new DeltaDirectoryOpen(this.getNewToken('d'), _path, _revision);
        this.addDelta(delta);
        return delta;
    }

    /**
     *
     * @param _path         path of the file (identically on client and server
     *                      side)
     * @return new create delta instance
     * @see DeltaFileCreate
     * @see #createFile(String, String)
     */
    public AbstractDelta createFile(final String _path)
    {
        final AbstractDelta delta = new DeltaFileCreate(this.getNewToken('f'), _path);
        this.addDelta(delta);
        return delta;
    }

    /**
     *
     * @param _path         path of the file (on the client side)
     * @param _serverPath   path of the file (on the server); the path is used
     *                      to get the input stream from the file and returned
     *                      to the client as <code>_path</code>
     * @return new create delta instance
     * @see DeltaFileCreate
     * @see #createFile(String)
     */
    public AbstractDeltaFile createFile(final String _path,
                                        final String _serverPath)
    {
        final AbstractDeltaFile delta = new DeltaFileCreate(this.getNewToken('f'), _path, _serverPath);
        this.addDelta(delta);
        return delta;
    }

    /**
     *
     * @param _currentPath      path of the file (on the client side)
     * @param _currentRevision  current revision of the file on the client
     *                          (base file, which is updated)
     * @param _orgPath          path of the file (on the server); the path is
     *                          used to get the input stream
     * @return new update file delta instance
     * @see DeltaFileCreate
     */
    public AbstractDeltaFile updateFile(final String _currentPath,
                                        final Long _currentRevision,
                                        final String _orgPath)
    {
        final AbstractDeltaFile delta = new DeltaFileOpen(this.getNewToken('f'),
                                                          _currentPath,
                                                          _orgPath,
                                                          _currentRevision);
        this.addDelta(delta);
        return delta;
    }

    /**
     *
     * @param _path
     * @param _revision
     * @return
     */
    public AbstractDelta delete(final String _path,
                                final Long _revision)
    {
        final AbstractDelta delta = new DeltaDelete(_path, _revision);
        this.addDelta(delta);
        return delta;
    }

    /**
     *
     * @param _streams      SVN in - / output streams
     * @throws IOException
     * @throws URISyntaxException   if for copied directories or files the
     *                              copied path could not parsed
     * @throws ServerException      if the client has aborted or if an unknown
     *                              key from client was sent
     */
    @Client2Server
    public void read(final SVNSessionStreams _streams)
            throws IOException, URISyntaxException, ServerException
    {
        boolean closed = false;
        boolean aborted = false;
        final Set<String> unknownCommands = new TreeSet<String>();
        while (!closed)  {
            final ListElement list = _streams.readItemList();
            final Word key = list.getList().get(0).getWord();
            final List<AbstractElement<?>> params = list.getList().get(1).getList();
            switch (key)  {
                case OPEN_ROOT:
                    this.addDelta(new DeltaRootOpen(params));
                    break;
                case DELETE_ENTRY:
                    this.addDelta(new DeltaDelete(params));
                    break;
                case ADD_DIR:
                    final String dirPath = params.get(0).getString();
                    final String dirToken = params.get(2).getString();
                    final List<AbstractElement<?>> copyList = params.get(3).getList();
                    // new directory
                    if (copyList.isEmpty())  {
                        this.addDelta(new DeltaDirectoryCreate(dirToken, dirPath));
                    // directory is copied
                    } else  {
                        final String copyPath = _streams.getSession().extractPathFromURL(copyList.get(0).getString());
                        final long copyRevision = copyList.get(1).getNumber();
                        this.addDelta(new DeltaDirectoryCopy(dirToken, dirPath, copyPath, copyRevision));
                    }
                    break;
                case OPEN_DIR:
                    this.addDelta(new DeltaDirectoryOpen(params));
                    break;
                case CHANGE_DIR_PROP:
                    final String cdpToken = params.get(0).getString();
                    final AbstractDelta cdpDelta = this.mapToken2Delta.get(cdpToken);
                    cdpDelta.put(params.get(1).getString(),
                                 params.get(2).getList().get(0).getString());
                    break;
                case CLOSE_DIR:
                    break;
                case ADD_FILE:
                    this.addDelta(new DeltaFileCreate(params.get(2).getString(),
                                                      params.get(0).getString()));
                    break;
                case OPEN_FILE:
                    this.addDelta(new DeltaFileOpen(params.get(2).getString(),
                                                    params.get(0).getString(),
                                                    params.get(0).getString(),
                                                    params.get(3).getList().get(0).getNumber()));
                    break;
                case APPLY_TEXTDELTA:
                    final String apToken = params.get(0).getString();
                    final List<AbstractElement<?>> baseMD5List = params.get(1).getList();
                    final String baseMD5 = baseMD5List.isEmpty() ? null : baseMD5List.get(0).getString();
                    final AbstractDeltaFile apDelta = (AbstractDeltaFile) this.mapToken2Delta.get(apToken);
                    apDelta.applyTextDelta(_streams, baseMD5);
                    break;
                case TEXTDELTA_CHUNK:
                    final String tcToken = params.get(0).getString();
                    final byte[] tcBuffer = params.get(1).getByteArray();
                    final AbstractDeltaFile tcDelta = (AbstractDeltaFile) this.mapToken2Delta.get(tcToken);
                    tcDelta.textDeltaChunk(tcBuffer);
                    break;
                case TEXTDELTA_END:
                    final String teToken = params.get(0).getString();
                    final AbstractDeltaFile teDelta = (AbstractDeltaFile) this.mapToken2Delta.get(teToken);
                    teDelta.textDeltaEnd();
                    break;
                case CHANGE_FILE_PROP:
                    final String cfpToken = params.get(0).getString();
                    final AbstractDelta cfpDelta = this.mapToken2Delta.get(cfpToken);
                    cfpDelta.put(params.get(1).getString(),
                                 params.get(2).getList().get(0).getString());
                    break;
                case CLOSE_FILE:
                    final String cfToken = params.get(0).getString();
                    final String cfMD5;
                    if ((params.size() > 1) && (params.get(1).getList().size() > 0))  {
                        cfMD5 = params.get(1).getList().get(0).getString();
                    } else  {
                        cfMD5 = null;
                    }
                    final AbstractDeltaFile cfDelta = (AbstractDeltaFile) this.mapToken2Delta.get(cfToken);
                    cfDelta.closeFile(cfMD5);
                    break;
                case ABORT_EDIT:
                    closed = true;
                    aborted = true;
                    break;
                case CLOSE_EDIT:
                    closed = true;
                    break;
                default:
                    unknownCommands.add(key.value);
            }
        }

        if (!unknownCommands.isEmpty())  {
// TODO: i18n
throw new ServerException("Unknown command(s) " + unknownCommands);
        }

        if (aborted)  {
            // original message "Delta source ended unexpectedly"
            throw new ServerException(ErrorCode.SVN_ERR_INCOMPLETE_DATA);
        }
    }

    @Client2Server
    public void close()
    {
        for (final AbstractDelta delta : getDeltas())  {
            delta.close();
        }
    }

    /**
     *
     * @param _streams      SVN server session streams
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    @Server2Client
    public void write(final SVNSessionStreams _streams)
            throws UnsupportedEncodingException, IOException
    {
        _streams.writeItemList(new ListElement(Word.TARGET_REV, new ListElement(getTargetRevision())));

        final Stack<AbstractDelta> stack = new Stack<AbstractDelta>();
        for (final AbstractDelta delta : getDeltas())  {
            final String parentToken;
            if (stack.isEmpty())  {
                parentToken = null;
            } else  {
                final File file = new File(delta.getPath());
                final String parentDir = (file.getParent() == null) ? "" : file.getParent();

                while (!stack.peek().getPath().equals(parentDir)/* && (stack.size() > 1)*/)  {
                    stack.pop().writeClose(_streams);
                }

                parentToken = stack.peek().getToken();
            }

            stack.add(delta);
            delta.writeOpen(this.targetRevision, _streams, parentToken);
        }

        while (!stack.empty())  {
            stack.pop().writeClose(_streams);
        }

        _streams.writeItemList(new ListElement(Word.CLOSE_EDIT, new ListElement()));
    }
}
