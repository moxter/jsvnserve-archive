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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.tmatesoft.svn.core.SVNException;

/**
 *
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public abstract class AbstractTest
{

    private final static String PROPERTY_TEST_PATH = "com.googlecode.jsvnserve.test.path.wc";

    /**
     * File path to the working copy.
     *
     * @see #initWC()
     */
    private File wcPath;

    /**
     * URL of the repository.
     *
     * @see #initRepositoryURL(int, String)
     */
    private String repository;

    /**
     * Name of the used SVN user.
     *
     * @see #svnUser
     */
    private String svnUser;

    /**
     * Password the the used SVN user.
     *
     * @see #svnPassword
     */
    private String svnPassword;

    /**
     * Initialize the working copy path. First all parent directories of the
     * working copy path are created if they are not exists. Then all sub
     * directories are deleted if a working path already exists.
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws SVNException
     * @see #wcPath
     */
    @BeforeTest
    public void initWC()
            throws IOException, InterruptedException, SVNException
    {
        final String testPath = System.getProperty(PROPERTY_TEST_PATH);
        this.wcPath = new File(testPath);
        this.wcPath.mkdirs();
        Runtime.getRuntime().exec(new String[]{"rm", "-r", this.wcPath.toString()}).waitFor();
        this.wcPath.mkdirs();
    }

    /**
     *
     * @param _port         port of the SVN repository
     * @param _repository   sub path of the repository
     * @see #repository
     */
    @BeforeTest
    @Parameters({"port", "repository"})
    public void initRepositoryURL(final int _port,
                                  final String _repository)
    {
        this.repository = "svn://127.0.0.1:" + _port + "/" + _repository;
    }

    /**
     *
     * @param _svnUser      name of the SVN user
     * @param _svnPassword  password of the SVN user
     * @see #svnUser
     * @see #svnPassword
     */
    @BeforeTest
    @Parameters({"svnUser", "svnPassword"})
    public void initUser(final String _svnUser,
                         final String _svnPassword)
    {
        this.svnUser = _svnUser;
        this.svnPassword = _svnPassword;
    }

    /**
     *
     * @param _authenticate
     * @param _cmd          string array of commands
     * @return
     * @throws InterruptedException
     * @throws IOException
     * @throws ExecuteException
     */
    protected String execute(final boolean _authenticate,
                             final String... _cmd)
            throws InterruptedException, IOException, ExecuteException
    {
        final List<String> cmd = new ArrayList<String>();
        cmd.add("/opt/local/bin/svn");
        if (_authenticate)  {
            cmd.add("--username");
            cmd.add(this.svnUser);
            cmd.add("--password");
            cmd.add(this.svnPassword);
            cmd.add("--non-interactive");
        }
        cmd.addAll(Arrays.asList(_cmd));
System.err.println("Execute " + cmd);
        final ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        pb.directory(this.wcPath);
        final Map<String,String> env = pb.environment();
        env.put("LANG", "C");
        final Process process = pb.start();
        final int exit = process.waitFor();

        final StringBuilder bck = new StringBuilder();
        while (process.getInputStream().available() > 0)  {
            bck.append((char) process.getInputStream().read());
        }

        if (exit != 0)  {
            throw new ExecuteException(bck.toString());
        }
        return bck.toString();
    }

    /**
     * Returns the working copy path.
     *
     * @return working copy path
     * @see #wcPath
     */
    protected File getWCPath()
    {
        return this.wcPath;
    }

    /**
     * Returns the URL of the repository.
     *
     * @return repository URL
     * @see #repository
     */
    protected String getRepositoryURL()
    {
        return this.repository;
    }
}
