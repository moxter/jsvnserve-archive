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

package com.googlecode.jsvnserve.api;

/**
 * The exception must be used if no existing exception matches.
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class OtherServerException
        extends ServerException
{
    /**
     * Serial version UID of this class.
     */
    private static final long serialVersionUID = -9020628267629496480L;

    /**
     * Constructor.
     *
     * @param _message  localized error message
     */
    public OtherServerException(final String _message)
    {
        super(ErrorCode.UNKNOWN, _message);
    }

    /**
     * Constructor.
     *
     * @param _cause    original cause
     */
    public OtherServerException(final Exception _cause)
    {
        super(ErrorCode.UNKNOWN, _cause);
    }

    /**
     * Constructor.
     *
     * @param _message  localized error message
     * @param _cause    original cause
     */
    public OtherServerException(final String _message,
                                final Exception _cause)
    {
        super(ErrorCode.UNKNOWN, _message, _cause);
    }
}
