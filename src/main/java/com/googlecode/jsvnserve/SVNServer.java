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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.googlecode.jsvnserve.api.IRepositoryFactory;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class SVNServer
        extends Thread
{
    /**
     * Factory to create new repository instances depending on the logged in
     * user and path.
     */
    private IRepositoryFactory repositoryFactory;

    /**
     * Port on which the SVN server runs. The default port is 3690.
     */
    private int port = 3690;


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

    @Override
    public void run()
    {
        ServerSocket socket;
        try {
            socket = new ServerSocket(this.port);
        } catch (final IOException e) {
            e.printStackTrace();
            throw new Error("Server socket for port " + this.port + " could not be created:", e);
        }

        while (true)  {
            final Socket clientSocket;
            try {
                System.out.println("waiting...");
                clientSocket = socket.accept();
            } catch (final IOException e) {
                e.printStackTrace();
                throw new Error("Server socket could not be accepted.", e);
            }

            try {
System.out.println("hallo.1");
                clientSocket.setSoTimeout(20000);
System.out.println("hallo.2");
                SVNServerSession  svnServer = new SVNServerSession(clientSocket,
                                                                   clientSocket.getInputStream(),
                                                                   clientSocket.getOutputStream(),
                                                                   "testuser",
                                                                   this.repositoryFactory);
System.out.println("hallo.3");
                svnServer.start();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }
}
