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
     * Log message of revision 1.
     *
     * @see #createDirectory()
     * @see #propGetLogRev1()
     */
    private final static String REV1_LOG = "My Message of revision 1";

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
    @Test(expectedExceptions = ExecuteException.class, timeOut = 10000)
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
    @Test(expectedExceptions = AssertionError.class, timeOut = 10000)
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
     * @see #REV1_LOG
     */
    @Test(timeOut = 10000)
    public void createDirectory()
            throws InterruptedException, IOException, ExecuteException, ParserConfigurationException, SAXException
    {
        final File dir = new File(this.getWCPath(), "temp1");
        dir.mkdir();
        this.execute(false, "add", "temp1");
        this.execute(true, "--message", REV1_LOG, "commit");
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
    @Test(dependsOnMethods = "createDirectory", timeOut = 10000)
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
    @Test(dependsOnMethods = "createSubDirectory", timeOut = 10000)
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
    @Test(dependsOnMethods = "createDirectoryAndSubDirectory", timeOut = 10000)
    public void createFile()
            throws IOException, InterruptedException, ExecuteException, ParserConfigurationException, SAXException
    {
        final File file = new File(this.getWCPath(), "temp1/file1.txt");
        final OutputStream out = new FileOutputStream(file);
        final String text = "This is the file '" + file.getName() + "'.\n";
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
        Assert.assertEquals(entries.get("temp1/file1.txt").size, text.length());
    }

    /**
     * Test if the revision properties for revision 0 could be listed.
     *
     * @throws InterruptedException
     * @throws IOException
     * @throws ExecuteException
     */
    @Test(timeOut = 10000)
    public void propListRev0()
            throws InterruptedException, IOException, ExecuteException
    {
        this.execute(true, "--verbose", "--revprop", "--revision", "0", "proplist", this.getRepositoryURL());
    }

    /**
     * Test if the revision properties for revision 1 could be listed.
     *
     * @throws InterruptedException
     * @throws IOException
     * @throws ExecuteException
     */
    @Test(dependsOnMethods = "createDirectory", timeOut = 10000)
    public void propListRev1()
            throws InterruptedException, IOException, ExecuteException
    {
        this.execute(true, "--verbose", "--revprop", "--revision", "1", "proplist", this.getRepositoryURL());
    }

    /**
     * Tests that the log message from revision 1 fetched with <i>propget</i>
     * is equal to the used log message in {@link #createDirectory()}.
     *
     * @throws InterruptedException
     * @throws IOException
     * @throws ExecuteException
     * @see #REV1_LOG
     */
    @Test(dependsOnMethods = "createDirectory", timeOut = 10000)
    public void propGetLogRev1()
            throws InterruptedException, IOException, ExecuteException
    {
        final String log = this.execute(true, "--revprop", "--revision", "1", "propget", "svn:log", this.getRepositoryURL());
        Assert.assertEquals(log, REV1_LOG);
    }

    /**
     * Property 'svn:eol-style' is set for file created with
     * {@link #createFile()}. The result is checked.
     *
     * @throws InterruptedException
     * @throws IOException
     * @throws ExecuteException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    @Test(dependsOnMethods = "createFile", timeOut = 10000)
    public void setFileProperty()
            throws InterruptedException, IOException, ExecuteException, ParserConfigurationException, SAXException
    {
        this.execute(false, "--force", "propset", "svn:eol-style", "LF", "temp1/file1.txt");
        this.execute(true, "--message", "My Message", "commit");

        final Map<Long,LogEntry> log = this.readLog();
        final long lastRev = (Long) log.keySet().toArray()[log.size() - 1];
        final LogEntry logEntry = log.get(lastRev);
        Assert.assertTrue(!logEntry.paths.isEmpty(), "last commit had one file modified");
        Assert.assertEquals((String) logEntry.paths.keySet().toArray()[0], "/temp1/file1.txt");
        Assert.assertEquals(this.execute(true, "propget", "svn:eol-style", this.getRepositoryURL() + "/temp1/file1.txt"), "LF", "check property value");
    }

    /**
     * Check that the get property value for 'svn:eol-style' for non existing
     * file failed.
     *
     * @throws InterruptedException
     * @throws IOException
     * @throws ExecuteException
     */
    @Test(expectedExceptions = AssertionError.class, timeOut = 10000)
    public void testFilePropertyOnNonExisting()
            throws InterruptedException, IOException, ExecuteException
    {
        try  {
            this.execute(true, "propget", "svn:eol-style", this.getRepositoryURL() + "/temp1/file2.txt.tmp");
        } catch (final ExecuteException ex)  {
            Assert.assertFalse(ex.getMessage().contains("does not exist"));
        }
    }

    /**
     * Creates new directory &quot;/temp3&quot; directly on the SVN server.
     * That the new created directory exists is tested.
     *
     * @throws InterruptedException
     * @throws IOException
     * @throws ExecuteException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    @Test(dependsOnMethods = "createFile", timeOut = 10000)
    public void createTemp3DirOnServer()
            throws InterruptedException, IOException, ExecuteException, ParserConfigurationException, SAXException
    {
        this.execute(true, "--message", "Temp3 created", "mkdir", this.getRepositoryURL() + "/temp3");
        final Map<String,DirEntry> entries = this.readDir();
        Assert.assertEquals(entries.size(), 6);
        Assert.assertTrue(entries.containsKey("temp3"));
    }

    /**
     * Makes and update of the working copy.
     *
     * @throws ExecuteException
     * @throws IOException
     * @throws InterruptedException
     *
     */
    @Test(dependsOnMethods = "createTemp3DirOnServer", timeOut = 10000)
    public void updateWC() throws InterruptedException, IOException, ExecuteException
    {
        this.execute(true, "update");
    }

    /**
     * Deletes directory &quot;/temp3&quot; directly on the SVN server.
     * That the deleted directory not exists is tested. Used to test the delete
     * entry of the command set from client to server.
     *
     * @throws InterruptedException
     * @throws IOException
     * @throws ExecuteException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    @Test(dependsOnMethods = "updateWC", timeOut = 10000)
    public void deleteTemp3DirOnServer()
            throws InterruptedException, IOException, ExecuteException, ParserConfigurationException, SAXException
    {
        this.execute(true, "--message", "Temp3 deleted", "rm", this.getRepositoryURL() + "/temp3");
        final Map<String,DirEntry> entries = this.readDir();
        Assert.assertEquals(entries.size(), 5);
        Assert.assertFalse(entries.containsKey("temp3"));
    }

    /**
     * Makes and update of the working copy. Used to test the delete entry
     * of the command set from server to client.
     *
     * @throws ExecuteException
     * @throws IOException
     * @throws InterruptedException
     *
     */
    @Test(dependsOnMethods = "deleteTemp3DirOnServer", timeOut = 10000)
    public void updateWCAfterDeleteTemp3()
            throws InterruptedException, IOException, ExecuteException
    {
        this.execute(true, "update");
        final File file = new File(this.getWCPath(), "temp3");
        Assert.assertFalse(file.exists(), "path not deleted locally!");
    }

    /**
     * Copy directory &quot;temp1&quot; to &quot;temp3&quot; on the server.
     * @throws ExecuteException
     * @throws IOException
     * @throws InterruptedException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    @Test(dependsOnMethods = "updateWCAfterDeleteTemp3", timeOut = 10000)
    public void copyTemp12Temp3DirOnServer() throws InterruptedException, IOException, ExecuteException, ParserConfigurationException, SAXException
    {
        this.execute(true, "--message", "temp1 copied to temp3", "copy",
                     this.getRepositoryURL() + "/temp1",
                     this.getRepositoryURL() + "/temp3");
        final Map<String,DirEntry> entries = this.readDir();
        Assert.assertEquals(entries.size(), 9);
        Assert.assertTrue(entries.containsKey("temp3"));
    }

    /**
     * Makes and update of the working copy. Used to test the delete entry
     * of the command set from server to client.
     *
     * @throws ExecuteException
     * @throws IOException
     * @throws InterruptedException
     *
     */
    @Test(dependsOnMethods = "copyTemp12Temp3DirOnServer", timeOut = 10000)
    public void updateWCAfterCopyTemp12Temp3DirOnServer()
            throws InterruptedException, IOException, ExecuteException
    {
        this.execute(true, "update");
        Assert.assertTrue(new File(this.getWCPath(), "temp3/file1.txt").exists());
        Assert.assertTrue(new File(this.getWCPath(), "temp3/sub1").exists());
        Assert.assertTrue(new File(this.getWCPath(), "temp3/sub2").exists());
    }

    /**
     * Sets property &quot;test-property&quot; to value &quot;test-value&quot;
     * for directory &quot;temp3&quot;.
     *
     * @throws InterruptedException
     * @throws IOException
     * @throws ExecuteException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    @Test(dependsOnMethods = "updateWCAfterCopyTemp12Temp3DirOnServer", timeOut = 10000)
    public void setTemp3DirProperty()
            throws InterruptedException, IOException, ExecuteException, ParserConfigurationException, SAXException
    {
        this.execute(false, "--force", "propset", "test-property", "test-value", "temp3");
        this.execute(true, "--message", "My Message", "commit");

        final Map<Long,LogEntry> log = this.readLog();
        final long lastRev = (Long) log.keySet().toArray()[log.size() - 1];
        final LogEntry logEntry = log.get(lastRev);
        Assert.assertTrue(!logEntry.paths.isEmpty(), "last commit had one directory modified");
        Assert.assertEquals((String) logEntry.paths.keySet().toArray()[0], "/temp3");
        Assert.assertEquals(this.execute(true, "propget", "test-property", this.getRepositoryURL() + "/temp3"), "test-value", "check property value");
    }
}
