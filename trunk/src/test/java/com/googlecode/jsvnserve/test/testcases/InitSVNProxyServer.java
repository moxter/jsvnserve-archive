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

import java.io.File;
import java.io.IOException;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.tmatesoft.svn.core.SVNException;

import com.googlecode.jsvnserve.SVNServer;
import com.googlecode.jsvnserve.test.svnproxy.RepositoryFactory;
import com.googlecode.jsvnserve.test.svnproxy.SVNProxyCallbackHandler;

/**
 * Initialize an empty SVN proxy server.
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class InitSVNProxyServer
{
    private final static String PROPERTY_TEST_PATH = "com.googlecode.jsvnserve.test.path.repository";

    private SVNServer svnServer;

    @BeforeTest(groups = "init.repository")
    @Parameters({"port"})
    public void createRepository(final int _port)
            throws IOException, InterruptedException, SVNException
    {
        final String testPath = System.getProperty(PROPERTY_TEST_PATH);
        final File path = new File(testPath);

        System.out.println("create repository on '" + path + "'");
        path.mkdirs();
        System.out.println("- delete existings test path");
        Runtime.getRuntime().exec(new String[]{"rm", "-r", path.toString()}).waitFor();
        System.out.println("- create new repository");
        path.mkdirs();
        final Process process = Runtime.getRuntime().exec(new String[]{"svnadmin",
                                                                       "create",
                                                                       path.toString()});
        process.waitFor();

        System.out.println("- start repository");
        this.svnServer = new SVNServer();
        this.svnServer.setPort(_port);
        this.svnServer.setRepositoryFactory(new RepositoryFactory("file://" + path.toString()));
        this.svnServer.setCallbackHandler(new SVNProxyCallbackHandler());
        this.svnServer.start();
    }

    @AfterTest
    public void stopRepository()
    {
        System.out.println("stop repository");
        this.svnServer.stop();
    }
}
