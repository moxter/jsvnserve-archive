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

import com.googlecode.jsvnserve.api.ServerException;

/**
 * The exception must be used within all use cases which uses the editor
 * command set if a file which is opened does not exists in the repository.
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class FileNotExistsException
        extends ServerException
{
    /**
     * Serial version UID of this class.
     */
    private static final long serialVersionUID = -7368477903732206328L;

    /**
     * Not existing file path.
     *
     * @see #getFilePath()
     */
    private final String filePath;

    /**
     * Default constructor.
     *
     * @param _filePath     not existing file path
     */
    public FileNotExistsException(final String _filePath)
    {
        super(ErrorCode.SVN_ERR_FS_NOT_FOUND);
        this.filePath = _filePath;
    }

    /**
     * Returns the not existing file path.
     *
     * @return not existing file path
     * @see #filePath
     */
    public String getFilePath()
    {
        return this.filePath;
    }
}
