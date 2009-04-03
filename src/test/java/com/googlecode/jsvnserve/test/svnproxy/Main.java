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

import java.io.IOException;

import org.tmatesoft.svn.core.SVNException;

import com.googlecode.jsvnserve.SVNServer;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class Main
{
    public static void main(final String... _args)
            throws IOException, InterruptedException, SVNException
    {
        SVNServer svnServer = new SVNServer();
        svnServer.setPort(9999);
        svnServer.setRepositoryFactory(new RepositoryFactory());
        svnServer.setCallbackHandler(new SVNProxyCallbackHandler());
        svnServer.start();
        Thread.sleep(100000);

        System.out.println("SVN Server end!");
        System.exit(0);
    }
}
