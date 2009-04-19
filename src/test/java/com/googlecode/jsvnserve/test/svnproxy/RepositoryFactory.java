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

package com.googlecode.jsvnserve.test.svnproxy;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;

import com.googlecode.jsvnserve.api.IRepository;
import com.googlecode.jsvnserve.api.IRepositoryFactory;
import com.googlecode.jsvnserve.api.ServerException;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class RepositoryFactory
        implements IRepositoryFactory
{
    /**
     * SVN URL to the original property.
     */
    final SVNURL svnURL;

    public RepositoryFactory(final String _svnURI) throws SVNException
    {
        this.svnURL = SVNURL.parseURIDecoded(_svnURI);
        SVNRepositoryFactoryImpl.setup();
        FSRepositoryFactory.setup();
    }

    /**
     * @param _user     name of authenticated user
     * @param _path     path of the repository
     * @throws ServerException if the repository could not initialized or the
     *                         SVN path does not start with '/proxy'
     */
    public IRepository createRepository(final String _user,
                                        final String _path)
            throws ServerException
    {
        if ("/proxy".equals(_path) || _path.startsWith("/proxy/"))  {
            try  {
                final String rootpath = _path.substring("/proxy".length());
                return new Repository(this.svnURL,
                                      _user,
                                      "/proxy",
                                      rootpath);
            } catch (final SVNException ex)  {
                throw new ServerException("initalize of the repository failed", ex);
            }
        } else  {
            throw new ServerException("unknown SVN path '" + _path + "'");
        }
    }
}
