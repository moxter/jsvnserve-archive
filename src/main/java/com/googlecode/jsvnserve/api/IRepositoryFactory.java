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
 * To create a new instance of a repository {@link IRepository}, this factory
 * must be implemented.
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public interface IRepositoryFactory
{
    /**
     * <p>Returns for given path related repository instance. The path includes
     * the path of the repository and the path which defines the root.</p>
     * <p><b>Example paths:</b><br/>
     * <code>svn+ssh://server/repository/</code><br/>
     * <code>svn+ssh://server/repository/trunk</code><br/>
     * <code>svn+ssh://server/repository/branches/V1.0.9</code><br/>
     * In all cases &quot;<code>repository</code>&quot; is the repository path
     * (see {@link IRepository#getRepositoryPath()}), &quot;&quot;,
     * &quot;<code>trunk</code>&quot; and
     * &quot;<code>branches/V1.0.9</code>&quot; are the root paths (see
     * {@link IRepository#getRootPath()}).</p>
     * <p>A repository path could also include more than one name (e.g.
     * &quot;<code>repositories/data</code>&quot; or
     * &quot;<code>repositories/pictures</code>&quot;). All paths could depends
     * of the user. So the &quot;real world&quot; path
     * &quot;<code>repository/data/trunk</code>&quot; could be accessed by
     * the user with &quot;symbolic path&quot; &quot;<code>trunk</code>&quot;
     * (in this case the repository path for the user is &quot;&quot; and the
     * root path &quot;<code>trunk</code>&quot;).</p>
     *
     * @param _user     name of current context user which wants to use given
     *                  repository
     * @param _path     path for which the repository instance must be created
     *                  (including repository path and root path)
     * @return repository instance
     * @see IRepository#getRepositoryPath()
     * @see IRepository#getRootPath()
     */
    public IRepository createRepository(final String _user,
                                        final String _path);
}
