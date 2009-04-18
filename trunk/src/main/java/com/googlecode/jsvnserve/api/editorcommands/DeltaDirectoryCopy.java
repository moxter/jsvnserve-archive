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

import com.googlecode.jsvnserve.SVNSessionStreams;

/**
 * Represents the directory copy from the command editor.
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class DeltaDirectoryCopy
        extends AbstractDeltaDirectory
{
    /**
     * Serial version UID of this delta class.
     */
    private static final long serialVersionUID = 3507742802264441376L;

    DeltaDirectoryCopy(final String _token,
                       final String _path,
                       final String _copiedPath,
                       final long _copiedRevision)
    {
        super(_token, _path,
              _copiedPath, _copiedRevision);
    }

    @Override
    protected void writeOpen(final SVNSessionStreams _streams,
                             final String _parentToken)
            throws UnsupportedEncodingException, IOException
    {
// TODO: implement
        /*_streams.writeItemList(
                new ListElement(Word.ADD_DIR,
                                new ListElement(this.getPath(),
                                                _parentToken, this.getToken(),
                                                new ListElement())));
        this.writeAllProperties(_streams, Word.CHANGE_DIR_PROP);*/
throw new Error("not implemented!!");
    }
}
