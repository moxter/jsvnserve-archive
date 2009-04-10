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

package com.googlecode.jsvnserve.test.testcases;

/**
 * Exception which is thrown if a SVN call failed.
 *
 * @author jSVNServe Team
 * @version $Id$
 * @see AbstractTest#execute(boolean, String...)
 */
public class ExecuteException
        extends Exception
{
    /**
     *
     */
    private static final long serialVersionUID = 4056636583610251503L;

    public ExecuteException(final String _message)
    {
        super(_message);
    }
}
