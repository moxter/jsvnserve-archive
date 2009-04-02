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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import com.googlecode.jsvnserve.SVNServerSession;
import com.googlecode.jsvnserve.element.ListElement;
import com.googlecode.jsvnserve.element.WordElement.Word;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
class DeltaDirectoryCreate
        extends AbstractDeltaDirectory
{
    DeltaDirectoryCreate(final Editor _deltaEditor,
                         final String _path,
                         final String _lastAuthor,
                         final Long _committedRevision,
                         final Date _committedDate)
    {
        super(_deltaEditor, 'd', _path, _lastAuthor, _committedRevision, _committedDate);
    }

    @Override
    protected void writeOpen(final SVNServerSession _session,
                             final String _parentToken)
            throws UnsupportedEncodingException, IOException
    {
        _session.writeItemList(
                new ListElement(Word.ADD_DIR,
                                new ListElement(this.getPath(),
                                                _parentToken, this.getToken(),
                                                new ListElement())));
        this.writeAllProperties(_session, Word.CHANGE_DIR_PROP);
    }
}
