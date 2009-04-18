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
 * <p>Prepares the root directory on which all operations was invoked.</p>
 *
 * <p><b>SVN call:</b><br/>
 * <code style="color:green">( open-root ( ( ?rev:number ) root-token:string ) )</code>
 * <dl>
 * <dt>rev</dt>
 * <dd>revision of the root directory (optional)</dd>
 * <dt>dir-token</dt>
 * <dd>token of the root directory</dd>
 * </dl></p>
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class DeltaRootOpen
        extends AbstractDeltaDirectory
{
    /**
     * Serial version UID of this delta class.
     */
    private static final long serialVersionUID = -4617459091650970020L;

    /**
     * Revision of the root to open. Could be defined also as
     * <code>null</code> (means in this case the HEAD revision)!
     */
    private final Long revision;

    /**
     * Default constructor.
     *
     * @param _token    token of the root directory
     * @param _revision revision of the root directory
     * @see #revision
     */
    DeltaRootOpen(final String _token,
                  final Long _revision)
    {
        super(_token, "", null, null);
        this.revision = _revision;
    }

    /**
     * Constructor initialized with parameters from the SVN client.
     *
     * @param _params   list of parameters from the SVN client
     *                  <ul>
     *                  <li>(optional) first index of the list is itself a
     *                      list and could defined the revision of the root to
     *                      open</li>
     *                  <li>second index of the list is a string and defines
     *                      token of the root directory</li>
     *                  </ul>
     * @see #revision
     */
    DeltaRootOpen(final List<AbstractElement<?>> _params)
    {
        super(_params.get(1).getString(), "", null, null);
        if (_params.get(0).getList().size() > 0)  {
            this.revision = _params.get(0).getList().get(0).getNumber();
        } else  {
            this.revision = null;
        }
    }

    /**
     *
     * @param _targetRevision   target revision for which the the SVN editor
     *                          commands must be written (not used)
     * @param _streams
     * @param _parentToken      token of the parent directory
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    @Override
    protected void writeOpen(final long _targetRevision,
                             final SVNSessionStreams _streams,
                             final String _parentToken)
            throws UnsupportedEncodingException, IOException
    {
        _streams.writeItemList(
                new ListElement(Word.OPEN_ROOT,
                                new ListElement((this.revision == null)
                                                        ? new ListElement()
                                                        : new ListElement(this.revision),
                                                this.getToken())));
        this.writeAllProperties(_streams, Word.CHANGE_DIR_PROP);
    }
}
