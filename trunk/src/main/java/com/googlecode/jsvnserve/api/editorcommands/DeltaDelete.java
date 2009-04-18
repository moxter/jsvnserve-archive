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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import com.googlecode.jsvnserve.SVNSessionStreams;
import com.googlecode.jsvnserve.element.AbstractElement;
import com.googlecode.jsvnserve.element.ListElement;
import com.googlecode.jsvnserve.element.WordElement.Word;

/**
 * <p>Represents the delete from the command editor. Depending on the editor
 * mode this means:
 * <ul>
 * <li><b>commit:</b>  deletes an entry from a repository</li>
 * <li><b>update:</b> deletes an entry locally (since it has been deleted in
 *     the repository)</li>
 * <li><b>status:</b> informs that an entry has been deleted</li>
 * </ul></p>
 *
 * <p><b>SVN call:</b><br/>
 * <code style="color:green">( delete-entry ( path:string ( ?rev:number ) dir-token:string ) )</code>
 * <dl>
 * <dt>path</dt>
 * <dd>path to delete</dd>
 * <dt>rev</dt>
 * <dd>revision of the path (optional)</dd>
 * <dt>dir-token</dt>
 * <dd>token of the parent directory</dd>
 * </dl></p>
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class DeltaDelete
        extends AbstractDelta
{
    /**
     * Serial version UID of this delta class.
     */
    private static final long serialVersionUID = 5514915296427387629L;

    /**
     * Revision of the path entry to delete. Could be defined also as
     * <code>null</code>!
     */
    private final Long revision;

    /**
     * Default constructor.
     *
     * @param _path         path entry to delete (independent if the path is a
     *                      file or a directory)
     * @param _revision     revision of the path to delete (could be defined as
     *                      </code>null</code>)
     * @see EditorCommandSet#delete(String, Long)
     */
    DeltaDelete(final String _path,
                final Long _revision)
    {
        super(null, _path, null, null);
        this.revision = _revision;
    }

    /**
     * Constructor initialized with parameters from the SVN client.
     *
     * @param _params   list of parameters from the SVN client
     *                  <ul>
     *                  <li>first index of the list is a string and defines the
     *                      path to delete</li>
     *                  <li>(optional) second index of the list is itself a
     *                      list and could defined the revision of the path to
     *                      delete</li>
     *                  </ul>
     */
    DeltaDelete(final List<AbstractElement<?>> _params)
    {
        super(null, _params.get(0).getString(), null, null);
        if ((_params.size() > 1) && (_params.get(1).getList().size() > 0))  {
            this.revision = _params.get(1).getList().get(0).getNumber();
        } else  {
            this.revision = null;
        }
    }

    /**
     * Writes the delete entry word together with the path, the revision (if
     * not <code>null</code>) and the token of the parent directory.
     *
     * @param _targetRevision   target revision for which the the SVN editor
     *                          commands must be written (not used)
     * @param _streams          session streams
     * @param _parentToken      token of parent directory
     */
    @Override
    protected void writeOpen(final long _targetRevision,
                             final SVNSessionStreams _streams,
                             final String _parentToken)
            throws UnsupportedEncodingException, IOException
    {
        _streams.writeItemList(
                new ListElement(Word.DELETE_ENTRY,
                                new ListElement(getPath(),
                                                this.revision == null
                                                        ? new ListElement()
                                                        : new ListElement(this.revision),
                                                _parentToken)));
    }

    /**
     * A delete of an entry must be not closed. Therefore the method is only
     * a stub.
     *
     * @param _streams  not needed
     */
    @Override
    protected void writeClose(final SVNSessionStreams _streams)
    {
    }

    /**
     * Returns the revision of the path entry to delete.
     *
     * @return revision of the path
     * @see #revision
     */
    public Long getRevision()
    {
        return this.revision;
    }
}
