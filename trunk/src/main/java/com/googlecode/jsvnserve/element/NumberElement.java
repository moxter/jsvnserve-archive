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

package com.googlecode.jsvnserve.element;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class NumberElement
        extends AbstractElement<Long>
{
    public NumberElement(final CharSequence _value)
    {
        super(Long.parseLong(_value.toString()));
    }

    public NumberElement(final Long _value)
    {
        super(_value);
    }

    public NumberElement(final Integer _value)
    {
        super(_value.longValue());
    }

    @Override
    public void write(final OutputStream _out)
            throws IOException
    {
        _out.write(String.valueOf(this.getValue()).getBytes());
        _out.write(' ');
    }

    /**
     * Returns the long value for which this SVN element is defined.
     *
     * @return long of current value
     * @see #getValue()
     */
    @Override
    public Long getNumber()
    {
        return this.getValue();
    }

    @Override
    public String toString()
    {
        return "NUMBER '" + this.getValue() + "'";
    }
}