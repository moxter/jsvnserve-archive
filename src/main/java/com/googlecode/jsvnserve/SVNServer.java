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

package com.googlecode.jsvnserve;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.stream.StreamIoHandler;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.googlecode.jsvnserve.api.IRepositoryFactory;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class SVNServer
{
    /**
     * Factory to create new repository instances depending on the logged in
     * user and path.
     *
     * @see #setRepositoryFactory(IRepositoryFactory)
     * @see SVNServerHandler#processStreamIo(IoSession, InputStream, OutputStream)
     */
    private IRepositoryFactory repositoryFactory;

    /**
     * Port on which the SVN server runs. The default port is 3690.
     */
    private int port = 3690;

    /**
     *
     * @param _repositoryFactory
     */
    public void setRepositoryFactory(final IRepositoryFactory _repositoryFactory)
    {
        this.repositoryFactory = _repositoryFactory;
    }

    /**
     * Configure the port number to use for this SVN server.
     *
     * @param port the port number for this SVN server
     */
    public void setPort(final int _port)
    {
        this.port = _port;
    }

    /**
     * Starts the SVN server by using as acceptor {@link NioSocketAcceptor}.
     * The handler {@link SVNServerHandler} is used and bind to port
     * {@link #port}.
     *
     * @see #port
     */
    public void start()
            throws IOException
    {
        final IoAcceptor acceptor = new NioSocketAcceptor();
        acceptor.setHandler(new SVNServerHandler());
        acceptor.bind(new InetSocketAddress(this.port));
    }

    /**
     * SVN server handler with stream to start a new session instance from
     * {@link SVNServerSession}.
     */
    class SVNServerHandler
            extends StreamIoHandler
    {
        /**
         * Starts a new SVN server session. Because the client SVN kit could
         * not handle unbuffered output stream, the output stream is embedded
         * within a buffered output stream.
         *
         * @param _iosession    IO session
         * @param _in           input stream
         * @param _out          output stream
         */
        @Override
        protected void processStreamIo(final IoSession _iosession,
                                       final InputStream _in,
                                       final OutputStream _out)
        {
            SVNServerSession  svnServer = new SVNServerSession(_in,
                                                               new BufferedOutputStream(_out),
                                                               null,
                                                               SVNServer.this.repositoryFactory);
            svnServer.start();
        }

        @Override
        public void sessionClosed(IoSession session)
        throws Exception
        {
            System.out.println("sessionClosed.start");
super.sessionClosed(session);
System.out.println("sessionClosed.end");

        }
    }
}
