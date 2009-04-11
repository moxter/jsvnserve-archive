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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class ListElement
            extends AbstractElement<List<AbstractElement<?>>>
{
    public ListElement()
    {
        super(new ArrayList<AbstractElement<?>>());
    }

    public ListElement(final Object... _args)
            throws UnsupportedEncodingException
    {
        super(new ArrayList<AbstractElement<?>>());
        this.add(_args);
    }

    public void add(final Object... _args)
            throws UnsupportedEncodingException
    {
        for (final Object arg : _args)  {
            if (arg instanceof String)  {
                this.getValue().add(new StringElement((String) arg));
            } else if (arg instanceof Integer)  {
                this.getValue().add(new NumberElement((Integer) arg));
            } else if (arg instanceof Long)  {
                this.getValue().add(new NumberElement((Long) arg));
            } else if (arg instanceof WordElement.Word)  {
                this.getValue().add(new WordElement((WordElement.Word) arg));
            } else if (arg instanceof AbstractElement)  {
                this.getValue().add((AbstractElement<?>) arg);
            } else if (arg instanceof Date)  {
                this.getValue().add(new StringElement((Date) arg));
            } else if (arg instanceof byte[])  {
                this.getValue().add(new StringElement((byte[]) arg));
            } else  {
                System.err.println("unknown class "+ arg.getClass());
            }
        }
    }

    public void read(final InputStream _in)
            throws IOException
    {
        AbstractElement<?> element;
        while (true)  {
            element = readElement(_in);
            if (element.getWord() == WordElement.Word.BRACE_OPEN)  {
                ListElement list = new ListElement();
                list.read(_in);
                this.getValue().add(list);
            } else if (element.getWord() == WordElement.Word.BRACE_CLOSE)  {
                break;
            } else  {
                this.getValue().add(element);
            }
        }
    }

    public AbstractElement<?> readElement(final InputStream _in)
        throws IOException
    {
        AbstractElement<?> ret = null;
        final StringBuilder buf = new StringBuilder();
        char ch = (char) _in.read();
        if (Character.isDigit(ch))  {
            while (Character.isDigit(ch))  {
                buf.append(ch);
                ch = (char) _in.read();
            }
            if (ch == ':')  {
                final int length = Integer.parseInt(buf.toString());
                final byte[] byteBuffer = new byte[length];
                _in.read(byteBuffer, 0, length);
                ret = new StringElement(byteBuffer);
                ch = (char) _in.read();
            } else  {
                ret = new NumberElement(buf);
            }
            if (!Character.isWhitespace(ch))  {
                throw new Error("Malformed network data");
            }
        } else  {
            while (!Character.isWhitespace(ch))  {
                buf.append(ch);
                ch = (char) _in.read();
            }
            ret = new WordElement(buf);
        }
        return ret;
    }

    @Override
    public void write(final OutputStream _out)
            throws IOException
    {
        _out.write("( ".getBytes());
        for (final AbstractElement<?> element : this.getValue())  {
            element.write(_out);
        }
        _out.write(") ".getBytes());
    }

    /**
     * Returns the list value for which this SVN element is defined.
     *
     * @return list value
     * @see #getValue()
     */
    @Override
    public List<AbstractElement<?>> getList()
    {
        return this.getValue();
    }

    @Override
    public String toString()
    {
        return this.getValue().toString();
    }
}