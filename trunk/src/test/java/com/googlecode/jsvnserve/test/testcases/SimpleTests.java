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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.testng.Assert;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

/**
 * Some simple automatic tests for the SVN server implementation jSVNServe.
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class SimpleTests
        extends AbstractTest
{
    /**
     * The method tests if for the repository URL {@link #getRepositoryURL()}
     * appended with &quot;Test&quot; SVN command &quot;info&quot; not works,
     * because for this case the SVN repository path must not exists.
     *
     * @throws ExecuteException
     * @throws IOException
     * @throws InterruptedException
     *
     */
    @Test(expectedExceptions = ExecuteException.class)
    public void testNotExistingRepositoryPath()
            throws InterruptedException, IOException, ExecuteException
    {
        this.execute(true, "info", this.getRepositoryURL() + "Test");
    }

    /**
     * Test that authentication to SVN failed with wrong password.
     *
     * @throws InterruptedException
     * @throws IOException
     * @throws ExecuteException
     */
    @Test(expectedExceptions = AssertionError.class)
    @Parameters({"svnUser", "svnPassword"})
    public void testFailedUserAutentication(final String _svnUser,
                                            final String _svnPassword)
            throws InterruptedException, IOException
    {
        try  {
            this.execute(false,
                    "--username", _svnUser,
                    "--password", _svnPassword + _svnPassword,
                    "--non-interactive",
                    "--verbose",
                    "ls", this.getRepositoryURL());
        } catch (final ExecuteException ex)  {
            Assert.assertFalse(ex.getMessage().contains("Authentication error from server"));
        }
    }

    /**
     * Creates new directory 'temp1' and commits.
     *
     * @throws InterruptedException
     * @throws IOException
     * @throws ExecuteException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    @Test
    public void createDirectory()
            throws InterruptedException, IOException, ExecuteException, ParserConfigurationException, SAXException
    {
        final File dir = new File(this.getWCPath(), "temp1");
        dir.mkdir();
        this.execute(false, "add", "temp1");
        this.execute(true, "--message", "My Message", "commit");
        final Map<String,DirEntry> entries = this.readDir();
        Assert.assertEquals(entries.size(), 1, "only directory 'temp1' was submitted, but found " + entries);
        Assert.assertTrue(entries.containsKey("temp1"));
        Assert.assertEquals(entries.get("temp1").kind, DirKind.DIR, "'temp1' is not a directory");
    }

    /**
     * Creates new sub directory 'sub1' within directory 'temp1' and commits.
     *
     * @throws InterruptedException
     * @throws IOException
     * @throws ExecuteException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    @Test(dependsOnMethods = "createDirectory")
    public void createSubDirectory()
            throws InterruptedException, IOException, ExecuteException, ParserConfigurationException, SAXException
    {
        final File dir = new File(this.getWCPath(), "temp1/sub1");
        dir.mkdir();
        this.execute(false, "add", "temp1/sub1");
        this.execute(true, "--message", "My Message", "commit");
        final Map<String,DirEntry> entries = this.readDir();
        Assert.assertEquals(entries.size(), 2);
        Assert.assertTrue(entries.containsKey("temp1"));
        Assert.assertTrue(entries.containsKey("temp1/sub1"));
        Assert.assertEquals(entries.get("temp1").kind, DirKind.DIR);
        Assert.assertEquals(entries.get("temp1/sub1").kind, DirKind.DIR);
    }

    /**
     * Creates directory 'temp2' and sub directory 'sub2' within directory
     * 'temp1' and commits.
     *
     * @throws InterruptedException
     * @throws IOException
     * @throws ExecuteException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    @Test(dependsOnMethods = "createSubDirectory")
    public void createDirectoryAndSubDirectory()
            throws InterruptedException, IOException, ExecuteException, ParserConfigurationException, SAXException
    {
        new File(this.getWCPath(), "temp1/sub2").mkdir();
        new File(this.getWCPath(), "temp2").mkdir();
        this.execute(false, "add", "temp1/sub2");
        this.execute(false, "add", "temp2");
        this.execute(true, "--message", "My Message", "commit");
        final Map<String,DirEntry> entries = this.readDir();
        Assert.assertEquals(entries.size(), 4);
        Assert.assertTrue(entries.containsKey("temp1"));
        Assert.assertTrue(entries.containsKey("temp1/sub1"));
        Assert.assertTrue(entries.containsKey("temp1/sub2"));
        Assert.assertTrue(entries.containsKey("temp2"));
        Assert.assertEquals(entries.get("temp1/sub2").kind, DirKind.DIR);
        Assert.assertEquals(entries.get("temp2").kind, DirKind.DIR);
    }

    /**
     * Creates file &quot;temp1/file1.text&quot; and commits.
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecuteException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    @Test(dependsOnMethods = "createDirectoryAndSubDirectory")
    public void createFile()
            throws IOException, InterruptedException, ExecuteException, ParserConfigurationException, SAXException
    {
        final File file = new File(this.getWCPath(), "temp1/file1.txt");
        final OutputStream out = new FileOutputStream(file);
        out.write(("This is the file '" + file.getName() + "'.\n").getBytes());
        out.close();
        this.execute(false, "add", "temp1/file1.txt");
        this.execute(true, "--message", "My Message", "commit");
        final Map<String,DirEntry> entries = this.readDir();
        Assert.assertEquals(entries.size(), 5);
        Assert.assertTrue(entries.containsKey("temp1"));
        Assert.assertTrue(entries.containsKey("temp1/file1.txt"));
        Assert.assertTrue(entries.containsKey("temp1/sub1"));
        Assert.assertTrue(entries.containsKey("temp1/sub2"));
        Assert.assertTrue(entries.containsKey("temp2"));
        Assert.assertEquals(entries.get("temp1/file1.txt").kind, DirKind.FILE);
    }
}
