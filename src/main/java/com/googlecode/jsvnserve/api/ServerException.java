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
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class ServerException
        extends Exception
{
    /**
     *
     */
    private static final long serialVersionUID = 8887754181709870728L;

    private final static long APR_OS_START_USERERR = 120000;

    private final static long SVN_ERR_CATEGORY_SIZE = 5000;

    private final static long SVN_ERR_FS_CATEGORY_START = APR_OS_START_USERERR + ( 8 * SVN_ERR_CATEGORY_SIZE);
    private final static long SVN_ERR_RA_CATEGORY_START = APR_OS_START_USERERR + (10 * SVN_ERR_CATEGORY_SIZE);
    private final static long SVN_ERR_CLIENT_CATEGORY_START = APR_OS_START_USERERR + (15 * SVN_ERR_CATEGORY_SIZE);
    public static enum ErrorCode
    {
        /**
         * The error code is unknown.
         */
        UNKNOWN(0, "Unknown error"),

        /**
         * Defines the error code for &quot;Bad property name&quot;. The error
         * code is used e.g. within commit to check that only allowed revision
         * properties are defined.
         */
        SVN_ERR_CLIENT_PROPERTY_NAME(SVN_ERR_CLIENT_CATEGORY_START + 11, "Bad property name"),

        SVN_ERR_FS_NOT_FOUND(SVN_ERR_FS_CATEGORY_START + 13, "Filesystem has no item"),

        SVN_ERR_FS_NOT_DIRECTORY(SVN_ERR_FS_CATEGORY_START + 16, "Name does not refer to a filesystem directory"),

        /** @since New in 1.2. */
//        SVN_ERR_FS_PATH_ALREADY_LOCKED(SVN_ERR_FS_CATEGORY_START + 35, "Path is already locked"),

        /*  */
//        SVN_ERR_RA_ILLEGAL_URL(SVN_ERR_RA_CATEGORY_START + 0, "Bad URL passed to RA layer"),

//        SVN_ERR_RA_NOT_AUTHORIZED(SVN_ERR_RA_CATEGORY_START + 1, "Authorization failed"),

//        SVN_ERR_RA_UNKNOWN_AUTH(SVN_ERR_RA_CATEGORY_START + 2, "Unknown authorization method"),

//        SVN_ERR_RA_NOT_IMPLEMENTED(SVN_ERR_RA_CATEGORY_START + 3, "Repository access method not implemented"),

//        SVN_ERR_RA_OUT_OF_DATE(SVN_ERR_RA_CATEGORY_START + 4, "Item is out of date"),

//        SVN_ERR_RA_NO_REPOS_UUID(SVN_ERR_RA_CATEGORY_START + 5, "Repository has no UUID"),

//        SVN_ERR_RA_UNSUPPORTED_ABI_VERSION(SVN_ERR_RA_CATEGORY_START + 6, "Unsupported RA plugin ABI version"),

        /** @since New in 1.2. */
//        SVN_ERR_RA_NOT_LOCKED(SVN_ERR_RA_CATEGORY_START + 7, "Path is not locked"),

        /** @since New in 1.5. */
//        SVN_ERR_RA_PARTIAL_REPLAY_NOT_SUPPORTED(SVN_ERR_RA_CATEGORY_START + 8, "Server can only replay from the root of a repository"),

        /** @since New in 1.5. */
//        SVN_ERR_RA_UUID_MISMATCH(SVN_ERR_RA_CATEGORY_START + 9, "Repository UUID does not match expected UUID"),

        /** @since New in 1.6. */
        SVN_ERR_RA_REPOS_ROOT_URL_MISMATCH(SVN_ERR_RA_CATEGORY_START + 10, "Repository root URL does not match expected root URL");

        public final long code;

        public final String text;

        ErrorCode(final long _code,
                  final String _text)
        {
            this.code = _code;
            this.text = _text;
        }
    }

    private final ErrorCode errorCode;

    public ServerException(final ErrorCode _errorCode)
    {
        super(_errorCode.text);
        this.errorCode = _errorCode;
    }

    protected ServerException(final ErrorCode _errorCode,
                              final String _message)
    {
        super(_message);
        this.errorCode = _errorCode;
    }

    protected ServerException(final ErrorCode _errorCode,
                              final String _message,
                              final Exception _cause)
    {
        super(_message, _cause);
        this.errorCode = _errorCode;
    }

    protected ServerException(final ErrorCode _errorCode,
                              final Exception _cause)
    {
        super(_cause.getMessage(), _cause);
        this.errorCode = _errorCode;
    }

    public ServerException(final String _message)
    {
        super(_message);
        this.errorCode = ErrorCode.UNKNOWN;
    }

    public ServerException(final String _message,
                           final Exception _cause)
    {
        super(_message, _cause);
        this.errorCode = ErrorCode.UNKNOWN;
    }

    /**
     * Returns the SVN error code as item of enumeration {@link ErrorCode}.
     *
     * @return SVN error code as enumeration item
     * @see #errorCode
     */
    public ErrorCode getErrorCode()
    {
        return errorCode;
    }
}
