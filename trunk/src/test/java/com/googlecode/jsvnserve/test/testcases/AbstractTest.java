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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
     * directories are deleted if a working path already exists. At last a
     * first checkout of the new initialized repository is done.
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecuteException
     * @see #wcPath
     */
    @BeforeTest(dependsOnMethods = {"initRepositoryURL", "initUser"},
                dependsOnGroups = "init.repository")
    public void initWC()
            throws IOException, InterruptedException, ExecuteException
    {
        final String testPath = System.getProperty(PROPERTY_TEST_PATH);
        this.wcPath = new File(testPath);
        this.wcPath.mkdirs();
        Runtime.getRuntime().exec(new String[]{"rm", "-r", this.wcPath.toString()}).waitFor();
        this.wcPath.mkdirs();
        this.execute(true, "co", this.repository, this.wcPath.toString());
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
        return bck.toString().trim();
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

    /**
     *
     * @return set with all current existing directory entries
     * @throws InterruptedException
     * @throws IOException
     * @throws ExecuteException             if SVN command &quot;ls&quot;
     *                                      failed
     * @throws ParserConfigurationException if XML parser could not be
     *                                      initialized
     * @throws SAXException                 if XML with the directory entries
     *                                      could not be parsed
     */
    public Map<String,DirEntry> readDir() throws InterruptedException, IOException,
            ExecuteException, ParserConfigurationException, SAXException
    {
        final String xml = this.execute(true,
                                        "--revision", "HEAD",
                                        "--depth", "infinity",
                                        "--xml",
                                        "ls");

        final Map<String,DirEntry> ret = new HashMap<String,DirEntry>();

        final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        final Document doc = docBuilder.parse(new ByteArrayInputStream(xml.getBytes()));

        Node node = doc.getDocumentElement().getFirstChild();
        while (node != null)  {
            if (node.getNodeType() == Node.ELEMENT_NODE)  {
                final NodeList list = node.getChildNodes();
                for (int idx = 0; idx < list.getLength(); idx++)  {
                    final Node subNode = list.item(idx);
                    if (subNode.getNodeType() == Node.ELEMENT_NODE)  {
                        final Element entry = (Element) subNode;
                        final String name = entry.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
                        final DirKind kind = DirKind.valueOf(entry.getAttribute("kind").toUpperCase());
                        final int size;
                        if (kind == DirKind.FILE)  {
                            final String sizeStr = entry.getElementsByTagName("size")
                                                        .item(0).getFirstChild().getNodeValue();
                            size = Integer.parseInt(sizeStr);
                        } else  {
                            size = 0;
                        }
                        ret.put(name, new DirEntry(name, kind, size));
                    }
                }
            }
            node = node.getNextSibling();
        }
        return ret;
    }

    /**
     * Class to hold one directory entry.
     */
    protected class DirEntry
    {
        /**
         * Name of the directory entry.
         */
        public final String name;

        /**
         * Kind of the directory entry.
         */
        public final DirKind kind;

        /**
         * Size for files.
         */
        public final int size;

        /**
         * Default constructor.
         *
         * @param _name     name of the directory entry
         * @param _kind     kind of the directory entry
         * @param _size     size if <code>_kind</code> of directory entry is
         *                  {@link DirKind#FILE}
         */
        private DirEntry(final String _name,
                         final DirKind _kind,
                         final int _size)
        {
            this.name = _name;
            this.kind = _kind;
            this.size = _size;
        }

        /**
         * Returns string representation of the directory entry. This includes
         * the name and kind of directory entry.
         *
         * @return string representation
         * @see #name
         * @see #kind
         */
        @Override
        public String toString()
        {
            return "[name = " + this.name + ", kind = " + this.kind + "]";
        }
    }

    /**
     * Enumeration for directory kinds.
     */
    protected enum DirKind
    {
        /** Directory. */
        DIR,

        /** File. */
        FILE;
    }
}

