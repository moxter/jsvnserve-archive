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
import com.googlecode.jsvnserve.element.ListElement;
import com.googlecode.jsvnserve.element.WordElement.Word;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public abstract class AbstractDeltaDirectory
        extends AbstractDelta
{
    /**
     * Serial version UID of this delta class.
     */
    private static final long serialVersionUID = -8336853748673925296L;

    AbstractDeltaDirectory(final String _token,
                           final String _path,
                           final String _copiedPath,
                           final Long _copiedRevision)
    {
        super(_token, _path,
              _copiedPath, _copiedRevision);
    }

    @Override
    protected void writeClose(final SVNSessionStreams _streams)
            throws UnsupportedEncodingException, IOException
    {
        _streams.writeItemList(new ListElement(Word.CLOSE_DIR,
                                               new ListElement(this.getToken())));

    }
}
