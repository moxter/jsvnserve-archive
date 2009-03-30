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
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public abstract class AbstractElement<X>
{
    private final X value;

    public AbstractElement(final X _value)
    {
        this.value = _value;
    }

    public abstract void write(final OutputStream _out)
    throws UnsupportedEncodingException, IOException;

    /**
     *
     * @return value of {@link #value}
     */
    public X getValue()
    {
        return this.value;
    }

    /**
     * Returns the {@link #value} as list. Because the value is in most times
     * not a string, a <code>null</code> is returned.
     *
     * @return always <code>null</code>
     */
    public List<AbstractElement<?>> getList()
    {
        return null;
    }

    /**
     * Returns the {@link #value} as number. Because the value is in most times
     * not a number, a <code>null</code> is returned.
     *
     * @return always <code>null</code>
     */
    public Long getNumber()
    {
        return null;
    }

    /**
     * Returns the {@link #value} as word. Because the value is in most times
     * not a word, a <code>null</code> is returned.
     *
     * @return always <code>null</code>
     */
    public String getString()
    {
        return null;
    }

    /**
     * Returns the {@link #value} as word. Because the value is in most times
     * not a word, a <code>null</code> is returned.
     *
     * @return always <code>null</code>
     */
    public WordElement.Word getWord()
    {
        return null;
    }
}