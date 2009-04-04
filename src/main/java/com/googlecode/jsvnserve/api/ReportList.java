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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.googlecode.jsvnserve.SVNServerSession;
import com.googlecode.jsvnserve.element.AbstractElement;
import com.googlecode.jsvnserve.element.ListElement;
import com.googlecode.jsvnserve.element.WordElement.Word;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class ReportList
{
    private final List<AbstractCommand> list = new ArrayList<AbstractCommand>();

    public List<AbstractCommand> getList()
    {
        return list;
    }

    public void add(final AbstractCommand _command)
    {
        this.list.add(_command);
    }

    /**
     *
     * @return all commands
     * @see #list
     */
    public List<AbstractCommand> values()
    {
        return this.list;
    }

    public void read(final SVNServerSession _session)
            throws IOException
    {
        ListElement list = _session.readItemList();
        Word key = list.getList().get(0).getWord();
        while ((key != Word.FINISH_REPORT) && (key != Word.ABORT_REPORT))  {
            final List<AbstractElement<?>> params = list.getList().get(1).getList();
            switch (list.getList().get(0).getWord())  {
                case SET_PATH:
                    final int size = params.size();
                    // extract lock token
                    final String lockToken;
                    if (size > 3)  {
                        final List<AbstractElement<?>> lockTokenParams = params.get(3).getList();
                        if (!lockTokenParams.isEmpty())  {
                            lockToken = lockTokenParams.get(0).getString();
                        } else  {
                            lockToken = null;
                        }
                    } else  {
                        lockToken = null;
                    }
                    // extract depth
                    final Depth pathDepth;
                    if (size > 4)  {
                        pathDepth = Depth.valueOf(params.get(4).getWord());
                    } else  {
                        pathDepth = Depth.UNKNOWN;
                    }

                    this.add(new SetPath(params.get(0).getString(),
                                         params.get(1).getNumber(),
                                         (params.get(2).getWord() == Word.BOOLEAN_TRUE),
                                         lockToken,
                                         pathDepth));
                    break;
                case DELETE_PATH:
                    this.add(new DeletePath(params.get(0).getString()));
                    break;
                case LINK_PATH:
                    break;
            }
            list = _session.readItemList();
            key = list.getList().get(0).getWord();
        }
    }

    public static abstract class AbstractCommand
    {
        private final String path;

        protected AbstractCommand(final String _path)
        {
            this.path = _path;
        }

        public String getPath()
        {
            return path;
        }
    }

    public static class SetPath
            extends AbstractCommand
    {

        private final long revision;

        private final boolean startEmpty;

        private final String lockToken;

        private final Depth depth;

        public SetPath(final String _path,
                       final long _revision,
                       final boolean _startEmpty,
                       final String _lockToken,
                       final Depth _depth)
        {
            super(_path);
            this.revision = _revision;
            this.startEmpty = _startEmpty;
            this.lockToken = _lockToken;
            this.depth = _depth;
        }

        public long getRevision()
        {
            return revision;
        }

        public boolean isStartEmpty()
        {
            return startEmpty;
        }

        public String getLockToken()
        {
            return lockToken;
        }

        public Depth getDepth()
        {
            return depth;
        }
    }

    public static class DeletePath
            extends AbstractCommand
    {
        public DeletePath(final String _path)
        {
            super(_path);
        }
    }
}
