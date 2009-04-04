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

package com.googlecode.jsvnserve.api.delta;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import com.googlecode.jsvnserve.SVNServerSession;
import com.googlecode.jsvnserve.element.ListElement;
import com.googlecode.jsvnserve.element.WordElement.Word;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class Editor
{
    /**
     * Stores one delta depending from the path and the related delta.
     *
     * @see #getDelta(String)
     * @see #getDeltas()
     */
    private final Map<String,AbstractDelta> deltas = new TreeMap<String,AbstractDelta>();

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
     * @param _targetRevision   target revision described from this delta
     * @see #targetRevision
     */
    public Editor(final long _targetRevision)
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
     *
     * @param _path     path for which the delta is searched
     * @return searched delta or <code>null</code> if not found
     * @see #deltas
     */
    public AbstractDelta getDelta(final String _path)
    {
        return this.deltas.get(_path);
    }

    public AbstractDelta updateRoot(final String _lastAuthor,
                                    final Long _committedRevision,
                                    final Date _committedDate)
    {
        final AbstractDelta delta = new DeltaRootOpen(this, _lastAuthor, _committedRevision, _committedDate);
        this.deltas.put("", delta);
        return delta;
    }

    public AbstractDelta createDir(final String _path,
                                   final String _lastAuthor,
                                   final Long _committedRevision,
                                   final Date _committedDate)
    {
        final AbstractDelta delta = new DeltaDirectoryCreate(this, _path, _lastAuthor, _committedRevision, _committedDate);
        this.deltas.put(_path, delta);
        return delta;
    }

    public AbstractDelta updateDir(final String _path,
                                   final String _lastAuthor,
                                   final Long _committedRevision,
                                   final Date _committedDate)
    {
        final AbstractDelta delta = new DeltaDirectoryOpen(this, _path, _lastAuthor, _committedRevision, _committedDate);
        this.deltas.put(_path, delta);
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
                                    final Long _revision,
                                    final Date _date)
    {
        final AbstractDelta delta = new DeltaFileCreate(this, _path, _lastAuthor, _revision, _date);
        this.deltas.put(_path, delta);
        return delta;
    }

    /**
     *

     * @param _session
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public void write(final SVNServerSession _session)
            throws UnsupportedEncodingException, IOException
    {
        _session.writeItemList(new ListElement(Word.TARGET_REV, new ListElement(this.getTargetRevision())));

        final Stack<AbstractDelta> stack = new Stack<AbstractDelta>();
        for (final AbstractDelta delta : this.getDeltas())  {
            final String parentToken;
            if (stack.isEmpty())  {
                parentToken = null;
            } else  {
                final File file = new File(delta.getPath());
                final String parentDir = (file.getParent() == null) ? "" : file.getParent();

                while (!stack.peek().getPath().equals(parentDir))  {
                    stack.pop().writeClose(_session);
                }

                parentToken = stack.peek().getToken();
            }

            stack.add(delta);
            delta.writeOpen(_session, parentToken);
        }

        while (!stack.empty())  {
            stack.pop().writeClose(_session);
        }

        _session.writeItemList(new ListElement(Word.CLOSE_EDIT, new ListElement()));
    }
}