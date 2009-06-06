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

package com.googlecode.jsvnserve.test.cmdline;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.AuthorizeCallback;
import javax.security.sasl.RealmCallback;

import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

import com.googlecode.jsvnserve.SVNServer;
import com.googlecode.jsvnserve.api.IRepository;
import com.googlecode.jsvnserve.api.ServerException;
import com.googlecode.jsvnserve.test.svnproxy.Repository;
import com.googlecode.jsvnserve.test.svnproxy.RepositoryFactory;

/**
 * @author jSVNServe Team
 * @version $Id$
 */
public class Tests
{
    private SVNServer svnServer;

    private final int testno;

    private final int port;

    private final String path;

    @Parameters({"testno", "port", "path"})
    public Tests(final int _testno,
                 @Optional("9000") final int _port,
                 @Optional("") final String _path)
    {
        if (!"".equals(_path))  {
            this.path = _path;
        } else  {
            final File currentDir = new File(".");
            final String curDirStr;
            try {
                curDirStr = currentDir.getCanonicalPath();
            } catch (final IOException e) {
                throw new Error(e);
            }
            this.path = curDirStr + "/src/test/python/cmdline";
        }

        this.testno = _testno;
        this.port = _port + this.testno;
    }

    @BeforeMethod(groups = "init.repository")
    public void createRepository()
            throws IOException, InterruptedException, SVNException
    {
        System.out.println("- start repository");
        this.svnServer = new SVNServer();
        this.svnServer.setPort(this.port);
        this.svnServer.setRepositoryFactory(new CmdLineRepositoryFactory("file://" + this.path.toString()));
        this.svnServer.setCallbackHandler(new SVNProxyCallbackHandler());
        try  {
            this.svnServer.start();
        } catch (BindException ex)  {
            Thread.sleep(30000);
            this.svnServer.start();
        }
    }

    @AfterMethod
    public void stopRepository()
    {
        System.out.println("stop repository");
        this.svnServer.stop();
    }

    class CmdLineRepositoryFactory
            extends RepositoryFactory
    {
        public CmdLineRepositoryFactory(String _svnuri)
                throws SVNException
        {
            super(_svnuri);
        }

        /**
         * @param _user     name of authenticated user
         * @param _path     path of the repository
         * @throws ServerException if the repository could not initialized or the
         *                         SVN path does not start with '/proxy'
         */
        @Override
        public IRepository createRepository(final String _user,
                                            final String _path)
                throws ServerException
        {
            try  {
                return new Repository(SVNURL.parseURIDecoded("file://" + Tests.this.path), _user, "", _path);
            } catch (final SVNException ex)  {
                throw new ServerException("initalize of the repository failed", ex);
            }
        }
    }


    /**
     * Dummy callback handler which accept every user if the name and password is
     * equal.
     */
    class SVNProxyCallbackHandler
            implements CallbackHandler
    {

        public void handle(final Callback[] _callbacks)
                throws IOException, UnsupportedCallbackException
        {
            AuthorizeCallback ac = null;
            NameCallback nc = null;
            PasswordCallback pc = null;
            RealmCallback rc = null;
            for (final Callback callback : _callbacks)  {
                if (callback instanceof AuthorizeCallback)  {
                    ac = (AuthorizeCallback) callback;
                } else if (callback instanceof NameCallback)  {
                    nc = (NameCallback) callback;
                } else if (callback instanceof PasswordCallback)  {
                    pc = (PasswordCallback) callback;
                } else if (callback instanceof RealmCallback)  {
                    rc = (RealmCallback) callback;
                } else  {
                    throw new UnsupportedCallbackException(callback);
                }
            }
            if (ac != null)  {
                if (ac.getAuthenticationID().equals(ac.getAuthorizationID()))  {
                    ac.setAuthorizedID(ac.getAuthenticationID());
                    ac.setAuthorized(true);
                } else  {
                    ac.setAuthorized(false);
                }
            }
            if ((nc != null) && (pc != null))  {
                pc.setPassword("rayjandom".toCharArray());
            }
        }
    }

    @Test(timeOut = 10000)
    public void test()
            throws Exception
    {
System.err.println("Start test " + this.testno);
Reporter.log("test case " + this.testno);
        final ProcessBuilder pb = new ProcessBuilder(new String[]{
                this.path + "/basic_tests.py",
                "--bin", "/opt/local/bin",
                "--url=svn://localhost:"+this.port,
                String.valueOf(this.testno)
        });
        pb.redirectErrorStream(true);
        pb.directory(new File(this.path));
        final Map<String,String> env = pb.environment();
        env.put("LANG", "C");
        final Process process = pb.start();
        final int exit = process.waitFor();
System.out.println("exit="+exit);

final StringBuilder bck = new StringBuilder();
while (process.getInputStream().available() > 0)  {
    bck.append((char) process.getInputStream().read());
}

if (exit != 0)  {
    throw new Exception("Test "+this.testno+" failed:\n"+bck.toString());
}

    }
}
