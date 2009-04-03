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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.sasl.AuthorizeCallback;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;
import javax.security.sasl.SaslServerFactory;

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
     * Stores the Sasl server factory used from the SVN server. Because the
     * factory is used from different threads, the implementation must be
     * thread safe. If no specific factory is defined, wrapper class
     * {@link SVNSaslServerFactory} for {@link Sasl} is used.
     *
     * @see #setSaslServerFactory(SaslServerFactory)
     * @see SVNSaslServerFactory
     */
    private SaslServerFactory saslServerFactory = new SVNSaslServerFactory();

    /**
     * <p>Stores the used callback handler. The callback handler must handle
     * the related {@link Callback} instanced depending on the Sasl mechanism.
     * The callback handler is used in multiple threads and so must be thread
     * safe.</p>
     * <p>E.g. for mechanism <code>CRAM-MD5</code> the callback handler must
     * handle callbacks {@link AuthorizeCallback}, {@link NameCallback} and
     * {@link PasswordCallback}.</p>
     *
     * @see #setCallbackHandler(CallbackHandler)
     */
    private CallbackHandler callbackHandler;

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
     * Defines the Sasl server factory which is used from the SVN server
     * session instance from {@link SVNServerSession} for authentication.
     *
     * @param _saslServerFactory    specific Sasl server factory
     */
    public void setSaslServerFactory(final SaslServerFactory _saslServerFactory)
    {
        this.saslServerFactory = _saslServerFactory;
    }

    /**
     * Defines the callback handler used to get the name and passwords needed
     * from {@link SaslServer}.
     *
     * @param _callbackHandler      callback handler
     * @see #callbackHandler
     */
    public void setCallbackHandler(final CallbackHandler _callbackHandler)
    {
        this.callbackHandler = _callbackHandler;
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
         * within a buffered output stream. The {@link SVNServerSession} runs
         * in a separate thread.
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
                                                               SVNServer.this.repositoryFactory,
                                                               null,
                                                               SVNServer.this.saslServerFactory,
                                                               SVNServer.this.callbackHandler);
            svnServer.start();
        }

        @Override
        public void sessionClosed(IoSession session)
        throws Exception
        {
// TODO: use the method to close the SVN server session
            System.out.println("sessionClosed.start");
super.sessionClosed(session);
System.out.println("sessionClosed.end");

        }
    }

    /**
     * Class to wrap {@link Sasl} to the {@link SaslServerFactory} interface
     * which is used from {@link SVNServerSession}
     */
    private class SVNSaslServerFactory
            implements SaslServerFactory
    {
        /**
         * Returns an instance of {@link SaslServer} for given parameters.
         * Internally
         * {@link Sasl#createSaslServer(String, String, String, Map, CallbackHandler)}
         * is used to return the instance.
         *
         * @param _mechanism    mechanism (e.g. <code>CRAM-MD5</code>)
         * @param _protocol     protocoll
         * @param _serverName   name of the server
         * @param _cbh          call back handler
         * @return instance of {@link SaslServer} if for given parameters or
         *         <code>null</code> if no {@link SaslSever} implements given
         *         parameters
         * @see SVNSaslServerFactory#createSaslServer(String, String, String, Map, CallbackHandler)
         * @see Sasl#createSaslServer(String, String, String, Map, CallbackHandler)
         */
        public SaslServer createSaslServer(final String _mechanism,
                                           final String _protocol,
                                           final String _serverName,
                                           final Map<String, ?> _props,
                                           final CallbackHandler _cbh)
                throws SaslException
        {
            return Sasl.createSaslServer(_mechanism, _protocol, _serverName, _props, _cbh);
        }

        /**
         * Returns all mechanism names which could be handled by the Sasl
         * servers. It is a collection of all mechanism names implements from
         * all {@link SVNSaslServerFactory} defined from {@link Sasl}.
         *
         * @param _props    defines the policy properties
         * @return array with all mechanism names
         * @see SaslServerFactory#getMechanismNames(Map)
         * @see Sasl#getSaslServerFactories()
         */
        public String[] getMechanismNames(Map<String, ?> _props)
        {
            final List<String> mechs = new ArrayList<String>();
            final Enumeration<SaslServerFactory> en = Sasl.getSaslServerFactories();
            while (en.hasMoreElements())  {
                for (final String name : en.nextElement().getMechanismNames(_props))  {
                    mechs.add(name);
                }
            }
            return mechs.toArray(new String[mechs.size()]);
        }
    }
}
