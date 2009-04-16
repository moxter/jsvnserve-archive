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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import com.googlecode.jsvnserve.SVNSessionStreams;
import com.googlecode.jsvnserve.api.ServerException;
import com.googlecode.jsvnserve.element.AbstractElement;
import com.googlecode.jsvnserve.element.ListElement;
import com.googlecode.jsvnserve.element.WordElement.Word;

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

    public AbstractDelta updateRoot(final String _lastAuthor,
                                    final Long _committedRevision,
                                    final Date _committedDate)
    {
        final AbstractDelta delta = new DeltaRootOpen(getNewToken('d'), _lastAuthor, _committedRevision, _committedDate);
        addDelta(delta);
        return delta;
    }

    public AbstractDelta createDir(final String _path,
                                   final String _lastAuthor,
                                   final Long _committedRevision,
                                   final Date _committedDate)
    {
        final AbstractDelta delta = new DeltaDirectoryCreate(getNewToken('d'), _path, _lastAuthor, _committedRevision, _committedDate);
        addDelta(delta);
        return delta;
    }

    public AbstractDelta updateDir(final String _path,
                                   final String _lastAuthor,
                                   final Long _committedRevision,
                                   final Date _committedDate)
    {
        final AbstractDelta delta = new DeltaDirectoryOpen(getNewToken('d'), _path, _lastAuthor, _committedRevision, _committedDate);
        addDelta(delta);
        return delta;
    }

    /**
     *
     * @param _path         path of the file
     * @param _revision     commited revision of the file
     * @param _date         commited date of the file
     * @return new create delta instance
     * @see DeltaFileCreate
     */
    public AbstractDelta createFile(final String _path,
                                    final String _lastAuthor,
                                    final Long _committedRevision,
                                    final Date _committedDate)
    {
        final AbstractDelta delta = new DeltaFileCreate(getNewToken('f'), _path, _lastAuthor, _committedRevision, _committedDate);
        addDelta(delta);
        return delta;
    }

    public AbstractDelta delete(final String _path,
                                final String _lastAuthor,
                                final Long _committedRevision,
                                final Date _committedDate)
    {
        final AbstractDelta delta = new DeltaDelete(_path, _lastAuthor, _committedRevision, _committedDate);
        addDelta(delta);
        return delta;
    }

    /**
     *
     * @param _streams
     * @throws IOException
     * @throws URISyntaxException   if for copied directories or files the
     *                              copied path could not parsed
     */
    public void read(final SVNSessionStreams _streams)
            throws IOException, URISyntaxException, ServerException
    {
        boolean closed = false;
        final Set<String> unknownCommands = new TreeSet<String>();
        while (!closed)  {
            final ListElement list = _streams.readItemList();
            final Word key = list.getList().get(0).getWord();
            final List<AbstractElement<?>> params = list.getList().get(1).getList();
            switch (key)  {
                case OPEN_ROOT:
                    addDelta(new DeltaRootOpen(params.get(1).getString(),
                                                    null, null, null));
                    break;
                case ADD_DIR:
                    final String dirPath = params.get(0).getString();
                    final String dirToken = params.get(2).getString();
                    final List<AbstractElement<?>> copyList = params.get(3).getList();
                    // new directory
                    if (copyList.isEmpty())  {
                        addDelta(new DeltaDirectoryCreate(dirToken, dirPath, null, null, null));
                    // directory is copied
                    } else  {
                        final String copyPath = _streams.getSession().extractPathFromURL(copyList.get(0).getString());
                        final long copyRevision = copyList.get(1).getNumber();
                        addDelta(new DeltaDirectoryCopy(dirToken, dirPath, copyPath, copyRevision, null, null, null));
                    }
                    break;
                case OPEN_DIR:
                    addDelta(new DeltaDirectoryOpen(params.get(2).getString(),
                                                         params.get(0).getString(),
                                                         null, null, null));
                    break;
                case CLOSE_DIR:
                    break;
                case CLOSE_EDIT:
                    closed = true;
                    break;
                case ADD_FILE:
                    addDelta(new DeltaFileCreate(params.get(2).getString(),
                                                      params.get(0).getString(),
                                                      null, null, null));
                    break;
                case OPEN_FILE:
                    addDelta(new DeltaFileOpen(params.get(2).getString(),
                                                    params.get(0).getString(),
                                                    params.get(3).getList().get(0).getNumber()));
                    break;
                case CHANGE_FILE_PROP:
                    final String cfpToken = params.get(0).getString();
                    final AbstractDelta cfpDelta = this.mapToken2Delta.get(cfpToken);
                    cfpDelta.addProperty(params.get(1).getString(),
                                         params.get(2).getList().get(0).getString());
                    break;
                case APPLY_TEXTDELTA:
                    final String apToken = params.get(0).getString();
                    final AbstractDeltaFile apDelta = (AbstractDeltaFile) this.mapToken2Delta.get(apToken);
                    apDelta.applyTextDelta();
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
                default:
                    unknownCommands.add(key.value);
            }
        }

        if (!unknownCommands.isEmpty())  {
// TODO: i18n
throw new ServerException("Unknown command(s) " + unknownCommands);
        }
    }

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

                while (!stack.peek().getPath().equals(parentDir))  {
                    stack.pop().writeClose(_streams);
                }

                parentToken = stack.peek().getToken();
            }

            stack.add(delta);
            delta.writeOpen(_streams, parentToken);
        }

        while (!stack.empty())  {
            stack.pop().writeClose(_streams);
        }

        _streams.writeItemList(new ListElement(Word.CLOSE_EDIT, new ListElement()));
    }
}
