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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;
import javax.security.sasl.SaslServerFactory;

import org.apache.mina.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jsvnserve.api.Depth;
import com.googlecode.jsvnserve.api.DirEntry;
import com.googlecode.jsvnserve.api.DirEntryList;
import com.googlecode.jsvnserve.api.IRepository;
import com.googlecode.jsvnserve.api.IRepositoryFactory;
import com.googlecode.jsvnserve.api.LocationEntries;
import com.googlecode.jsvnserve.api.LockDescriptionList;
import com.googlecode.jsvnserve.api.LogEntryList;
import com.googlecode.jsvnserve.api.ReportList;
import com.googlecode.jsvnserve.api.ServerException;
import com.googlecode.jsvnserve.api.LockDescriptionList.LockDescription;
import com.googlecode.jsvnserve.api.LogEntryList.ChangedPath;
import com.googlecode.jsvnserve.api.LogEntryList.LogEntry;
import com.googlecode.jsvnserve.api.delta.Editor;
import com.googlecode.jsvnserve.element.AbstractElement;
import com.googlecode.jsvnserve.element.ListElement;
import com.googlecode.jsvnserve.element.StringElement;
import com.googlecode.jsvnserve.element.WordElement;
import com.googlecode.jsvnserve.element.WordElement.Word;
import com.googlecode.jsvnserve.sasl.SaslInputStream;
import com.googlecode.jsvnserve.sasl.SaslOutputStream;

/**
 * <p>The Subversion protocol is specified in terms of the following
 * syntactic elements, specified using ABNF [RFC 2234]:
 * <table>
 * <tr><td valign="top"><a name="item"><b>item</b> =</a></td>
 *     <td><code style="color:green"><a href="#word">word</a>
 *          | <a href="#number">number</a>
 *          | <a href="#string">string</a>
 *          | <a href="#list">list</a></code></td></tr>
 * <tr><td valign="top"><a name="word"><b>word</b> =</a></td>
 *     <td><code style="color:green">ALPHA *(ALPHA / DIGIT / "-") space</code></td></tr>
 * <tr><td valign="top"><a name="number"><b>number</b> =</a></td>
 *     <td><code style="color:green">1*DIGIT space</code></td></tr>
 * <tr><td valign="top"><a name="string"><b>string</b> =</a></td>
 *     <td><code style="color:green">1*DIGIT ":" *OCTET space</code></td>
 *     <td>digits give the byte count of the *OCTET portion</td></tr>
 * <tr><td valign="top"><a name="list"><b>list</b> =</a></td>
 *     <td><code style="color:green">&quot;(&quot; space *item &quot;)&quot; space</code></td></tr>
 * <tr><td valign="top"><a name="space"><b>space</b> =</a></td>
 *     <td><code style="color:green">1*(SP / LF)</code></td></tr>
 * </table></p>
 *
 * <p>Here are some miscellaneous prototypes used by the command sets:
 * <table>
 * <tr><td valign="top"><a name="bool"><b>bool</b> =</a></td>
 *     <td><code style="color:green">true | false</code></td></tr>
 * <tr><td valign="top"><a name="proplist"><b>proplist</b> =</a></td>
 *     <td><code style="color:green">( name:<a href="#string">string</a> </code>
 *         <code style="color:green">value:<a href="#string">string</a> ) ... </code></td></tr>
 * <tr><td valign="top"><a name="nodekind"><b>node-kind</b> =</a></td>
 *     <td><code style="color:green">none | file | dir | unknown</code></td></tr>
 * <tr><td valign="top" rowspan="8"><a name="lockdesc"><b>lockdesc</b> =</a></td>
 *     <td><code style="color:green">(</code></td>
 *     <tr><td>&nbsp;&nbsp;&nbsp;<code style="color:green">path:<a href="#string">string</a></code></td>
 *         <td>path which is locked</td></tr>
 *     <tr><td>&nbsp;&nbsp;&nbsp;<code style="color:green">token:<a href="#string">string</a></code></td>
 *         <td>id of the locking</td></tr>
 *     <tr><td>&nbsp;&nbsp;&nbsp;<code style="color:green">owner:<a href="#string">string</a></code></td>
 *         <td>name of the user who has locked the path</td></tr>
 *     <tr><td>&nbsp;&nbsp;&nbsp;<code style="color:green">( ?comment:<a href="#string">string</a> )</code></td></tr>
 *     <tr><td>&nbsp;&nbsp;&nbsp;<code style="color:green">created:<a href="#string">string</a></code></td>
 *         <td>date when the lock is created</tr>
 *     <tr><td>&nbsp;&nbsp;&nbsp;<code style="color:green">( ?expires:<a href="#string">string</a> )</code></td>
 *     <tr><td><code style="color:green">)</code></td></tr>
 * <tr><td valign="top"><a name="error"><b>error</b> =</a></td>
 *     <td><code style="color:green">( apr-err:number message:string file:string line:number )</code></td></tr>
 * <tr><td valign="top"><nobr><a name="noauthorization"><b>no-authorization</b> =</a></nobr></td>
 *     <td valign="top"><code style="color:green">( success ( ( ) 0: ) )</code></td>
 *     <td>before in the main command set the SVN response is returned, a no
 *         authorization must be returned (defined as pre initialized static
 *         variable in {@link #NO_AUTHORIZATION_NEEDED})</td></tr>
 * <tr><td valign="top"><a name"depth"><b>depth</b> =</a></td>
 *     <td valign="top"><code style="color:green">unknown | exclude | empty |
 *         files | immediates | infinity</code></td></tr>
 * </table></p>
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class SVNServerSession
        extends Thread
{
    public static final String PROPERTY_REPOSITORY_UUID = "svn:entry:uuid";
    public static final String PROPERTY_DIR_ENTRY_AUTHOR = "svn:entry:last-author";
    public static final String PROPERTY_DIR_ENTRY_REVISION = "svn:entry:committed-rev";
    public static final String PROPERTY_DIR_ENTRY_DATE = "svn:entry:committed-date";
    private static final String PROPERTY_DIR_ENTRY_CHECKSUM = "svn:entry:checksum";

    private final static Logger LOGGER = LoggerFactory.getLogger(SVNServerSession.class);

    /**
     * Stores the list element for
     * &quot;<code>( success ( ( ) 0: ) )</code>&quot; which must be returned
     * if no authorization is needed.
     */
    private static final ListElement NO_AUTHORIZATION_NEEDED
            = new ListElement(Word.STATUS_SUCCESS, new ListElement(new ListElement(), ""));

    /**
     * Used for empty SVN response for
     * &quot;<code>( success ( ( ) ) )</code>&quot;.
     */
    public static final ListElement EMPTY_SUCCESS
            = new ListElement(Word.STATUS_SUCCESS, new ListElement(new ListElement()));

    private OutputStream out;

    private InputStream in;

    /**
     * Factory instance to create {@link #repository} instance depending on the
     * defined path from the SVN client.
     */
    private final IRepositoryFactory repositoryFactory;

    /**
     * Repository instance used to evaluate all information.
     */
    private IRepository repository;

    /**
     * Stores the user for the SVN server session.
     *
     * @see #SVNServerSession(InputStream, OutputStream, IRepositoryFactory, String, SaslServerFactory, CallbackHandler)
     */
    private String user;

    /**
     *
     * @see #SVNServerSession(InputStream, OutputStream, IRepositoryFactory, String, SaslServerFactory, CallbackHandler)
     */
    private final SaslServerFactory saslServerFactory;

    /**
     * Callback handler used for authentication via Sasl with a
     * {@link SaslServer} if the authentication was not done externally.
     *
     * @see #SVNServerSession(InputStream, OutputStream, IRepositoryFactory, String, SaslServerFactory, CallbackHandler)
     */
    private final CallbackHandler callbackHandler;

    /**
     * Current root path. Could be changed from the SVN client.
     */
    private String currentPath = "/";

    /**
     *
     * @param _in                   input stream
     * @param _out                  output stream
     * @param _repositoryFactory    repository factory to get related
     *                              repository
     * @param _user                 logged in user if external authentication
     *                              was used; otherwise must be defined
     *                              <code>null</code>
     * @param _saslServerFactory    factory to get {@link SaslServer} instance
     *                              for authentication if user is defined as
     *                              <code>null</code> and the authentication
     *                              must be done from SVN
     * @param _callbackHandler      callback handler for the authentication if
     *                              user is defined as <code>null</code> and
     *                              the authentication must be done from SVN
     */
    public SVNServerSession(final InputStream _in,
                            final OutputStream _out,
                            final IRepositoryFactory _repositoryFactory,
                            final String _user,
                            final SaslServerFactory _saslServerFactory,
                            final CallbackHandler _callbackHandler)
    {
        this.in = _in;
        this.out = _out;
        this.user = _user;
        this.repositoryFactory = _repositoryFactory;
        this.saslServerFactory = _saslServerFactory;
        this.callbackHandler = _callbackHandler;
    }

    @Override
    public void run()
    {
        try {

            this.writeItemList(
                    new ListElement(Word.STATUS_SUCCESS,
                            new ListElement(1, 2, new ListElement(),
                                    new ListElement(Word.EDIT_PIPELINE, Word.SVNDIFF1, Word.ABSENT_ENTRIES, Word.COMMIT_REVPRODS,
                                                    Word.DEPTH,Word.LOG_REVPROPS))));

            final ListElement ret = this.readItemList();

            final String hostName = ret.getValue().get(2).getValue().toString();

            final URI hostUri = new URI(hostName);

            // if no user is defined, user must authenticate
            if (this.user == null)  {
                this.authenticate(hostUri.getHost());
            // otherwise no autorization needed!
            } else  {
                this.writeItemList(NO_AUTHORIZATION_NEEDED);
            }

            this.repository = this.repositoryFactory.createRepository(this.user,
                    "".equals(hostUri.getPath()) ? "/" : hostUri.getPath());

            URI rootURI = new URI(hostUri.getScheme(),
                    null, hostUri.getHost(), hostUri.getPort(),
                    this.getRepository().getRepositoryPath().toString(), null, null);

            this.writeItemList(
                    new ListElement(Word.STATUS_SUCCESS,
                            new ListElement(this.getRepository().getUUID().toString(),
                                            rootURI.toASCIIString(),
                                            new ListElement(Word.MERGEINFO))));

            ListElement items = this.readItemList();
            while (items != null)  {
                switch (items.getValue().get(0).getWord())  {
                    case CHECK_PATH:        this.svnCheckPath(items.getList().get(1).getList());break;
                    case GET_DIR:           this.svnGetDir(items.getList().get(1).getList());break;
                    case GET_FILE:          this.svnGetFile(items.getList().get(1).getList());break;
                    case GET_LATEST_REV:    this.svnGetLatestRev();break;
                    case GET_LOCATIONS:     this.svnGetLocations(items.getList().get(1).getList());break;
                    case GET_LOCK:          this.svnGetLock(items.getList().get(1).getList());break;
                    case GET_LOCKS:         this.svnGetLocks(items.getList().get(1).getList());break;
                    case LOCK_MANY:         this.svnLockMany(items.getList().get(1).getList());break;
                    case LOG:               this.svnLog(items.getList().get(1).getList());break;
                    case REPARENT:          this.svnReparent(items.getList().get(1).getList());break;
                    case STAT:              this.svnStat(items.getList().get(1).getList());break;
                    case STATUS:            this.svnStatus(items.getList().get(1).getList());break;
                    case UNLOCK_MANY:       this.svnUnlockMany(items.getList().get(1).getList());break;
                    case UPDATE:            this.svnUpdate(items.getList().get(1).getList());break;
                    default:                System.err.println("unkown key " + items.getValue().get(0).getWord());break;
                }
                items = this.readItemList();
            }
        } catch (final UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.err.println("close session");
    }

    /**
     * <p>Makes the authentication for the SVN protocol (if not tunneled via
     * SSH). The authentication of an user is done always with Sasl server.
     * Depending on the authentication mechanism, the communication tokens for
     * each step are Base64 coded (for <code>CRAM-MD5</code> the tokens are NOT
     * Base64 coded, because for <code>CRAM-MD5</code> Sasl was not used from
     * subversion).</p>
     * <p>For authentication mechanism <code>DIGEST-MD5</code> the used Sasl
     * implementation from subversion could handle subsequent authentication
     * which is not supported from Java. So the initialize token must be always
     * reseted for this use case.</p>
     * <p>Depending of the used authentication mechanism, the input and output
     * stream must be encrypted (depends on the quality-of-protection property
     * of Sasl). This SVN server supports all quality-of-protections. If the
     * SVN client uses them, the input and output streams {@link #in} and
     * {@link #out} are filtered (see {@link #updateStreams(SaslServer)}).</p>
     * <p>If the user could be authenticated, the user name in {@link #user}
     * is updated.</p>
     *
     * <h3>SVN communication</h3>
     * <p><b>Authentication Request (from server)</b><br/>
     * After the server and client exchanges their supported SVN features,
     * the SVN sends authentication requests with a list of all supported
     * authentication mechanism.
     * <table>
     * <tr><td><code style="color:green"><nobr>( success (</nobr></code></td></tr>
     * <tr><td rowspan="2"></td><td><code style="color:green">( mech:<a href="#word">word</a> )</code></td>
     *     <td>list of all authentication mechanism supported from this Sasl
     *         server</td></tr>
     * <tr><td><code style="color:green">realm:<a href="#string">string</a></code></td>
     *     <td>realm of ths SVN server, value of parameter <code>_host</code>
     *         is used</td></tr>
     * <tr><td><code style="color:green">) )</code></td></tr>
     * </table></p>
     *
     * <p><b>Authentication Response (from client)</b><br/>
     * Then the client response with the selected authentication mechanism
     * and, if required, from selected authentication mechanism, the initial
     * token. For <code>DIGEST-MD5</code> the initial token is used for
     * subsequent authentication (see
     * <a href="http://www.ietf.org/rfc/rfc2831.txt">RFC 2831</a>), which is
     * not supported by Java. So in this case the initial response is set from
     * the method automatically to a zero length string.
     * <table>
     * <tr><td><code style="color:green">(</code></td></tr>
     * <tr><td rowspan="2"></td><td><code style="color:green">mech:<a href="#word">word</a></code></td>
     *     <td>from SVN client selected authentication mechanism</td></tr>
     * <tr><td><code style="color:green">( ?token:<a href="#string">string</a> )</code></td>
     *     <td>initial token for the authentication</td></tr>
     * <tr><td><code style="color:green">)</code></td></tr>
     * </table></p>
     *
     * <p><b>Step Challenge (from server)</b><br/>
     * For each step of the authentication the server sends the token within a
     * step challenge. The token must be Base64 encoded if the authentication
     * mechanism is not <code>CRAM-MD5</code>.
     * <table>
     * <tr><td><code style="color:green">( step (</code></td></tr>
     * <tr><td></td><td><code style="color:green">token:<a href="#string">string</a></code></td>
     *     <td>token of the authentication step (must be Base64 encoded if not
     *         <code>CRAM-MD5</code></td></tr>
     * <tr><td><code style="color:green">) )</code></td></tr>
     * </table></p>
     *
     * <p><b>Step Challenge (answer from client)</b><br/>
     * After a step challenge from the server, the client answers with a single
     * response token. The response token is Base64 encoded for non
     * <code>CRAM-MD5</code> authentication mechanism.<br/>
     * <code style="color:green">token:<a href="#string">string</a></code></p>
     *
     * <p><b>Success Challenge (from server)</b><br/>
     * If the authentication for the user is successfully, the success
     * challenge is returned to the client. The returned token depends on the
     * Sasl authentication mechanism. If the mechanism is not
     * <code>CRAM-MD5</code>, the token is Base64 encoded.</code>
     * <table>
     * <tr><td><code style="color:green">( success (</code></td></tr>
     * <tr><td></td><td><code style="color:green">?token:<a href="#string">string</a></code></td>
     *     <td>optional token if authentication was successfully (Base64
     *         encoded if not <code>CRAM-MD5</code></td></tr>
     * <tr><td><code style="color:green">) )</code></td></tr>
     * </table></p>
     *
     * <p><b>Failure Challenge (from server)</b><br/>
     * If the authentication failed or some answer from the client was not
     * correct, a failure challenge is sent from the SVN server. The failure
     * includes a message (which is never Base64 encoded!). The error text from
     * the thrown exception is used as failure message.</br>
     * In this case the authentication exchange is unsuccessful. The client
     * could could give up or restart the authentication process by making
     * another <b>Authentication Response (from client)</b>.
     * <table>
     * <tr><td><code style="color:green">( failure (</code></td></tr>
     * <tr><td></td><td><code style="color:green">message:<a href="#string">string</a></code></td>
     *     <td>failure message (real text, not encoded!)</td></tr>
     * <tr><td><code style="color:green">) )</code></td></tr>
     * </table></p>
     *
     * @param _host     name of the host for which user must authenticate, also
     *                  used as realm for <code>DIGEST-MD5</code>
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @see #updateStreams(SaslServer)
     * @see #user
     */
    protected void authenticate(final String _host)
            throws UnsupportedEncodingException, IOException
    {
        // evaluate list of authentication mechanism
        final ListElement mechanisms = new ListElement();
        for (final String mechanism : this.saslServerFactory.getMechanismNames(null))  {
            mechanisms.add(new WordElement(mechanism));
        }

        // return to SVN client list of all authentication mechanisms
        this.writeItemList(
                new ListElement(Word.STATUS_SUCCESS,
                        new ListElement(mechanisms, _host)));


        boolean authenticated = false;
        SaslServer saslServer = null;

        while (!authenticated)  {

            // get selected authentication mechanism from SVN client
            final ListElement selMechanismList  = this.readItemList();
            final String selMechanism = selMechanismList.getList().get(0).getString();

            // create new Sasl server depending on selected mechanism from client
            final Map<String,String> props = new HashMap<String,String>();
            props.put(Sasl.QOP, "auth, auth-int, auth-conf");
            props.put(Sasl.POLICY_NOANONYMOUS, "true");
            props.put(Sasl.POLICY_NOPLAINTEXT, "true");
            props.put(Sasl.REUSE, "false");
            saslServer = this.saslServerFactory.createSaslServer(selMechanism,
                                                                 "svn",
                                                                 _host,
                                                                 props,
                                                                 this.callbackHandler);

            final boolean isCramMD5 = "CRAM-MD5".equals(selMechanism);

            // get initial response from SVN client
            final List<AbstractElement<?>> initialResponseList = selMechanismList.getList().get(1).getList();
            byte[] response = initialResponseList.isEmpty()
                              ? new byte[]{}
                              : initialResponseList.get(0).getString().getBytes();

            // RFC2831 (DIGEST-MD5) says the client MAY provide an initial response
            // on subsequent authentication. Java SASL does not (currently) support
            // this and throws an exception if we try. This violates the RFC, so we
            // just strip any initial token.
            if (selMechanism.equals("DIGEST-MD5"))  {
                response = new byte[]{};
            }

            byte[] request = saslServer.evaluateResponse(response);
            final ListElement dummyList = new ListElement();
            SaslException exeption = null;
            while (!saslServer.isComplete() && (exeption == null))  {
                if (SVNServerSession.LOGGER.isTraceEnabled())  {
                    SVNServerSession.LOGGER.trace("REQ>: ( step ( {}:{} ) ) ", request.length, new String(request));
                }

                // send step to client
                this.out.write("( step ( ".getBytes());
                // if not CRAM-MD5 => encode base64
                if (!isCramMD5)  {
                    request = Base64.encodeBase64(request);
                }
                this.out.write(String.valueOf(request.length).getBytes());
                this.out.write(':');
                this.out.write(request);
                this.out.write(" ) ) ".getBytes());
                this.out.flush();

                // get response from client
                final AbstractElement<?> responseElem = dummyList.readElement(this.in);
                response = isCramMD5
                           ? responseElem.getString().getBytes("UTF8")
                           : Base64.decodeBase64(responseElem.getString().getBytes());

                if (SVNServerSession.LOGGER.isTraceEnabled())  {
                    SVNServerSession.LOGGER.trace("RES<: {}", new String(response));
                }
                try  {
                    request = saslServer.evaluateResponse(response);
                } catch(final SaslException ex)  {
                    exeption = ex;
                }
            }

            // no exception => user is authenticated
            if (exeption == null)  {
                if (isCramMD5)  {
                    this.writeItemList(new ListElement(Word.STATUS_SUCCESS, new ListElement()));
                } else  {
                    this.writeItemList(new ListElement(Word.STATUS_SUCCESS, new ListElement(new String(Base64.encodeBase64(request)))));
                }
                this.user = saslServer.getAuthorizationID();
                authenticated = true;
            // exception => user is NOT authenticated
            } else  {
                this.writeItemList(new ListElement(Word.STATUS_FAILURE,
                        new ListElement(exeption.getMessage())));
            }
        }

        this.updateStreams(saslServer);
    }

    /**
     * <p>Depending on the quality-of-protection property of the Sasl server
     * the streams {@link #in} and {@link #out} must by encrypted.</p>
     * <p>If the  quality-of-protection property is set to
     * <code>auth-int</code> or </code>auth-conf</code>, the input and output
     * streams {@link #in} and {@link #out} must be encrypted. This is done by
     * filtering the original streams via the {@link SaslInputStream} and
     * {@link SaslOutputStream}.</p>
     *
     * @param _saslServer   Sasl server
     * @see #in
     * @see #out
     * @see SaslInputStream
     * @see SaslOutputStream
     * @see #authenticate(String)
     */
    protected void updateStreams(final SaslServer _saslServer)
    {
        final String qop = (String) _saslServer.getNegotiatedProperty(Sasl.QOP);
        if ("auth-int".equals(qop) || "auth-conf".equals(qop))  {

            // get output buffer size
            final String outBuffSizeStr = (String) _saslServer.getNegotiatedProperty(Sasl.RAW_SEND_SIZE);
            int outBuffSize = 1000;
            if (outBuffSizeStr != null) {
                try {
                    outBuffSize = Integer.parseInt(outBuffSizeStr);
                } catch (NumberFormatException nfe) {
                    outBuffSize = 1000;
                }
            }

            // get input buffer size
            final String inBuffSizeStr = (String) _saslServer.getNegotiatedProperty(Sasl.MAX_BUFFER);
            int inBuffSize = 1000;
            if (inBuffSizeStr != null)  {
                try {
                    inBuffSize = Integer.parseInt(inBuffSizeStr);
                } catch (final NumberFormatException nfe) {
                    inBuffSize = 1000;
                }
            }

            this.out = new SaslOutputStream(_saslServer, outBuffSize, this.out);
            this.in = new SaslInputStream(_saslServer, inBuffSize, this.in);
        }
    }

    /**
     * Checks given path and returns related node (path) kind.
     *
     * <h3>SVN Call:</h3>
     * <table>
     * <tr><td><code style="color:green"><nobr>( check-path (</nobr></code></td></tr>
     * <tr><td rowspan="2"></td><td><code style="color:green">path:<a href="#string">string</a></code></td>
     *     <td>defines which path must be checked</td></tr>
     * <tr><td><code style="color:green">( ?rev:<a href="#number">number</a> )</code></td>
     *     <td>revision for which the information must returned</td></tr>
     * <tr><td><code style="color:green">) )</code></td></tr>
     * </table>
     *
     * <h3>SVN Response:</h3>
     * <code style="color:green">authorization:<a href="noauthorization">no-authorization</a> ( success ( kind:<a href="nodekind">node-kind</a> ))</code>
     *
     * @param _parameters
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    protected void svnCheckPath(final List<AbstractElement<?>> _parameters)
            throws UnsupportedEncodingException, IOException
    {
        // get path
        final String path = this.buildPath(_parameters.get(0).getString());
        // get revision
        final List<AbstractElement<?>> revParams = _parameters.get(1).getList();
        final Long revision;
        if (revParams.isEmpty())  {
            revision = null;
        } else  {
            revision = revParams.get(0).getNumber();
        }

        final DirEntry entry = this.getRepository().stat(revision, path, false);

        this.writeItemList(SVNServerSession.NO_AUTHORIZATION_NEEDED,
                           new ListElement(Word.STATUS_SUCCESS,
                                           new ListElement(entry.getKind())));
    }

    /**
     *
     * <h3>SVN Call:</h3>
     * <table>
     * <tr><td><code style="color:green"><nobr>( get-dir (</nobr></code></td></tr>
     * <tr><td rowspan="5"></td><td><code style="color:green">path:<a href="#string">string</a></code></td>
     *     <td>for which directory path must information returned</td></tr>
     * <tr><td><code style="color:green">( rev:<a href="#number">number</a> )</code></td>
     *     <td>revision for which the information must returned</td></tr>
     * <tr><td><code style="color:green">want-props:<a href="#bool">bool</a></code></td>
     *     <td>includes the SVN response the directory properties?</td></tr>
     * <tr><td><code style="color:green">want-contents:<a href="#bool">bool</a></code></td>
     *     <td>includes the SVN response the directory content (directory
     *         structure)?</td></tr>
     * <tr><td><nobr><code style="color:green">?( field:dirent-field ... )</code></nobr></td>
     *     <td>if &quot;<code>want-contents</code>&quot; is set to
     *         &quot;<code>true</code>&quot;, the list defines the information
     *         which must be returned for each entry of the directory
     *         structure</td></tr>
     * <tr><td><code style="color:green">) )</code></td></tr>
     * </table>
     * <table>
     * <tr><td valign="top"><b>dirent-field</b> = </td>
     *     <td><code style="color:green">kind | size | has-props | created-rev | time | last-author</code></td></tr>
     * </table>
     *
     * <h3>SVN Response:</h3>
     * <table>
     * <tr><td><code style="color:green">(</code></td><td></td><td></td></tr>
     * <tr><td rowspan="3"></td><td><code style="color:green">rev:<a href="number">number</a></code></td>
     *     <td>the revision is the same defined in the SVN call (the directory
     *          information is returned for this revision)</td></tr>
     * <tr><td><code style="color:green">( ?props:<a href="proplist">proplist</a> )</code></td>
     *     <td>the list of property for the directory is only returned if
     *         parameter &quot;<code>want-props</code>&quot; in the SVN call
     *         was &quot;<code>true</code>&quot;</td></tr>
     * <tr><td><code style="color:green">( ?( entry:dirent ) ... )</code></td>
     *     <td>the entry for the directory is only returned if parameter
     *         &quot;<code>want-contents</code>&quot; in the SVN call was
     *         &quot;<code>true</code>&quot;</td></tr>
     * <tr><td><code style="color:green">)</code></td></tr>
     * </table>
     * <table>
     * <tr><td valign="top"><b>dirent</b> = </td>
     *     <td><code style="color:green">name:<a href="string">string</a></code><br/>
     *         <code style="color:green">kind:<a href="nodekind">node-kind</a></code><br/>
     *         <code style="color:green">size:<a href="number">number</a></code><br/>
     *         <code style="color:green">has-props:<a href="bool">bool</a></code><br/>
     *         <code style="color:green">created-rev:<a href="number">number</a></code><br/>
     *         <code style="color:green">( created-date:<a href="string">string</a> )</code><br/>
     *         <code style="color:green">( ?last-author:<a href="string">string</a> )</code></td></tr>
     * </table>
     * If no &quot;<code>created-date</code>&quot; must returned, null date
     * time {@link StringElement#NULL_DATETIME} is used (because a date is
     * always required). If no &quot;<code>last-author</code>&quot; must be
     * returned, only zero length list &quot;<code>( )</code>&quot; is
     * used.
     *
     * @param _parameters
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    protected void svnGetDir(final List<AbstractElement<?>> _parameters)
            throws UnsupportedEncodingException, IOException
    {
        final String path = this.buildPath(_parameters.get(0).getString());
        final Long revision = _parameters.get(1).getList().get(0).getNumber();
        final boolean wantProps = _parameters.get(2).getWord() == Word.BOOLEAN_TRUE;
        final boolean wantContents = _parameters.get(3).getWord() == Word.BOOLEAN_TRUE;

        final DirEntry dirEntry = this.getRepository().stat(revision, path, wantProps);

        // evaluate properties
        final ListElement propList = new ListElement();
        if (wantProps)  {
            propList.add(new ListElement(SVNServerSession.PROPERTY_REPOSITORY_UUID,
                                         this.getRepository().getUUID().toString()),
                         new ListElement(SVNServerSession.PROPERTY_DIR_ENTRY_REVISION,
                                         String.valueOf(dirEntry.getRevision())),
                         new ListElement(SVNServerSession.PROPERTY_DIR_ENTRY_DATE,
                                         dirEntry.getDate()));
            if (dirEntry.getAuthor() != null)  {
                propList.add(new ListElement(SVNServerSession.PROPERTY_DIR_ENTRY_AUTHOR,
                                             dirEntry.getAuthor()));
            }
            for (final Map.Entry<String,String> prop : dirEntry.getProperties().entrySet())  {
                propList.add(new ListElement(prop.getKey(), prop.getValue()));
            }
        }

        // evaluate content of directory
        final ListElement dirList = new ListElement();
        if (wantContents)  {
            boolean kind = false;
            boolean size = false;
            boolean hasProps = false;
            boolean createdRev = false;
            boolean modified = false;
            boolean author = false;
            for (final AbstractElement<?> elem : _parameters.get(4).getList())  {
                if (elem.getWord() == null)  {
                    LOGGER.error("unknown dirent word '{}'", elem.getString());
                } else  {
                    switch(elem.getWord())  {
                        case LOG_DIRENT_KIND:           kind = true;break;
                        case LOG_DIRENT_SIZE:           size = true;break;
                        case LOG_DIRENT_HAS_PROPS:      hasProps = true;break;
                        case LOG_DIRENT_CREATED_REV:    createdRev = true;break;
                        case LOG_DIRENT_TIME:           modified = true;break;
                        case LOG_DIRENT_LAST_AUTHOR:    author = true;break;
                    }
                }
            }
            final DirEntryList dirEntryList = this.getRepository().getDir(revision,
                                                                     path,
                                                                     size,
                                                                     hasProps,
                                                                     createdRev,
                                                                     modified,
                                                                     author);

            for (final DirEntry entry : dirEntryList.getEntries())  {
                dirList.add(new ListElement(
                        entry.getName(),
                        kind ? entry.getKind() : Word.NODE_KIND_NONE,
                        size ? entry.getFileSize() : 0,
                        hasProps ? Word.BOOLEAN_FALSE : Word.BOOLEAN_FALSE,
                        createdRev ? entry.getRevision() : 0,
                        new ListElement(modified ? entry.getDate() : StringElement.NULL_DATETIME),
                        (author && (entry.getAuthor() != null))
                                ? new ListElement(entry.getAuthor())
                                : new ListElement()));
            }
        }

        this.writeItemList(SVNServerSession.NO_AUTHORIZATION_NEEDED,
                           new ListElement(Word.STATUS_SUCCESS,
                                           new ListElement(revision,
                                                           propList,
                                                           dirList)));
    }

    /**
     * params:   ( path:string [ rev:number ] want-props:bool want-contents:bool )
    response: ( [ checksum:string ] rev:number props:proplist )
    If want-contents is specified, then after sending response, server
     sends file contents as a series of strings, terminated by the empty
     string, followed by a second empty command response to indicate
     whether an error occurred during the sending of the file.

     * @param _parameters
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    protected void svnGetFile(final List<AbstractElement<?>> _parameters)
            throws UnsupportedEncodingException, IOException
    {
        final String path = this.buildPath(_parameters.get(0).getString());
        final Long revision = _parameters.get(1).getList().get(0).getNumber();
        final boolean wantsProps = _parameters.get(2).getWord() == Word.BOOLEAN_TRUE ? true : false;
        final boolean wantsContent = _parameters.get(3).getWord() == Word.BOOLEAN_TRUE ? true : false;

        final DirEntry dirEntry = this.getRepository().stat(revision, path, wantsProps);

        final ListElement props = new ListElement();
        if (wantsProps)  {
            props.add(new ListElement(SVNServerSession.PROPERTY_REPOSITORY_UUID,
                                      this.getRepository().getUUID().toString()),
                      new ListElement(SVNServerSession.PROPERTY_DIR_ENTRY_REVISION,
                                      String.valueOf(dirEntry.getRevision())),
                      new ListElement(SVNServerSession.PROPERTY_DIR_ENTRY_DATE,
                                      dirEntry.getDate()),
                      new ListElement(SVNServerSession.PROPERTY_DIR_ENTRY_CHECKSUM,
                                      dirEntry.getFileMD5()));
            if (dirEntry.getAuthor() != null)  {
                props.add(new ListElement(SVNServerSession.PROPERTY_DIR_ENTRY_AUTHOR,
                                          dirEntry.getAuthor()));
            }
            for (final Map.Entry<String,String> prop : dirEntry.getProperties().entrySet())  {
                props.add(new ListElement(prop.getKey(), prop.getValue()));
            }
        }

        this.writeItemList(SVNServerSession.NO_AUTHORIZATION_NEEDED,
                           new ListElement(Word.STATUS_SUCCESS,
                                           new ListElement(
                                                    new ListElement(dirEntry.getFileMD5()),
                                                    dirEntry.getRevision(),
                                                    props)));

        if (wantsContent)  {
            final InputStream in = this.getRepository().getFile(revision, path);
            final byte[] buffer = new byte[4096];
            int length = in.read(buffer);
            while (length >= 0)  {
                this.out.write(String.valueOf(length).getBytes("UTF8"));
                this.out.write(':');
                this.out.write(buffer, 0, length);
                this.out.write(' ');
                length = in.read(buffer);
            }
            this.out.write(" 0: ( success ( ) ) ".getBytes("UTF8"));
        }
//        ( success ( ( ) 0: ) ) ( success ( 32:24b42e558b8f74c64939aa4257b1daa1 ) 1000 ( ( 14:svn:entry:uuid 36:cf646e2a-176c-4309-8bfa-14680e0fdf19 ) ( 23:svn:entry:committed-rev 4:1000 ) ( 24:svn:entry:committed-date 27:2009-03-21T12:44:38.200000Z ) ) )

// ( get-file ( 0: ( 7 ) true false ) )
// ( success ( ( ) 0: ) ) ( success ( ( 32:d977b7f0d2c9ccab4dd60fa300bdcc59 ) 7 ( ( 14:svn:entry:uuid 36:cf646e2a-176c-4309-8bfa-14680e0fdf19 ) ( 23:svn:entry:committed-rev 1:7 ) ( 24:svn:entry:committed-date 27:2009-03-21T00:59:28.960732Z ) ) ) )
// ( get-file ( 0: ( 7 ) false true ) )
// ( success ( ( ) 0: ) ) ( success ( ( 32:d977b7f0d2c9ccab4dd60fa300bdcc59 ) 7 ( ) ) ) 1281:<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Frameset//EN" "http://www.w3.org/TR/html4/frameset.dtd">

    }

    /**
     * <h3>SVN Call:</h3>
     * <code style="color:green">(get-latest-rev ( ) )</code>
     *
     * <h3>SVN Response:</h3>
     * <code style="color:green">( rev:<a href="#number">number</a> )</code>
     *
     */
    protected void svnGetLatestRev()
            throws UnsupportedEncodingException, IOException
    {
        this.writeItemList(SVNServerSession.NO_AUTHORIZATION_NEEDED,
                           new ListElement(Word.STATUS_SUCCESS,
                                           new ListElement(this.getRepository().getLatestRevision())));
    }

    /**
     * <h3>SVN Call:</h3>
     * <table>
     * <tr><td><code style="color:green">( lock-many (</code></td></tr>
     * <tr><td  rowspan="3"></td><td><code style="color:green">
     *     ( ?comment:<a href="#string">string</a> )</code></td>
     *     <td>optional lock comment; it no lock comment is wanted, only
     *         &quot;<code>( )</code>&quot; could be defined</td></tr>
     * <tr><td><code style="color:green">steal-lock:<a href="#bool">bool</a></code></td>
     *     <td>if &quot;<code>true</code>&quot; and some paths already locked,
     *         the existing locks are overwritten</td></tr>
     * <tr><td><code style="color:green">( ( path:<a href="#string">string</a>
     *           ( ?current-rev:<a href="#number">number</a> ) ) ... )</code></td></tr>
     * <tr><td><code style="color:green">) )</code></td></tr>
     * </table>
     *
     * <h3>SVN Response:</h3>
     * <table>
     * <tr><td><code style="color:green">authorization:<a href="noauthorization">no-authorization</a></code></td></tr>
     * <tr><td><code style="color:green">( success ( lock:<a href="lockdesc">lockdesc</a> ) )  | ( failure ( err:<a href="error">error</a> ) ) | done</code></td>
     *     <td>Before sending response, server sends lock cmd status and
     *         descriptions, ending with &quot;<code>done</code>&quot;.</td></tr>
     * <tr><td><code style="color:green">( success ( ) )</code></td></tr>
     * </table>
     *
     * @param _parameters
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    protected void svnLockMany(final List<AbstractElement<?>> _parameters)
            throws UnsupportedEncodingException, IOException
    {
        // get comment
        final List<AbstractElement<?>> commentParameter = _parameters.get(0).getList();
        final String comment = (commentParameter.isEmpty())
                               ? null
                               : commentParameter.get(0).getString();
        // steal lock?
        final boolean stealLock = (_parameters.get(1).getWord() == Word.BOOLEAN_TRUE);
        // paths with related revisions
        final Map<String,Long> paths = new HashMap<String,Long>();
        for (final AbstractElement<?> onePath : _parameters.get(2).getList())  {
            final List<AbstractElement<?>> revList = onePath.getList().get(1).getList();
            paths.put(onePath.getList().get(0).getString(),
                      revList.isEmpty() ? null : revList.get(0).getNumber());
        }

        LockDescriptionList locks  = null;
        try  {
            locks = this.getRepository().lock(comment, stealLock, paths);
        } catch (final ServerException ex)  {
            this.writeItemList(new ListElement(Word.STATUS_FAILURE,
                    new ListElement(new ListElement(/*ex.errorCode.code*/160035,"","", 0))));
            this.out.write("done ( success ( ( ) 0: ) ) ( success ( ) ) ".getBytes("UTF8"));
        }

        final List<ListElement> ret = new ArrayList<ListElement>();
        ret.add(SVNServerSession.NO_AUTHORIZATION_NEEDED);

        for (final LockDescription lock : locks.getLockDescriptions())  {
            switch (lock.getStatus())  {
                case SUCCESSFULLY:
                    ret.add(new ListElement(Word.STATUS_SUCCESS,
                                            new ListElement(lock.getPath(),
                                                            lock.getToken(),
                                                            lock.getOwner(),
                                                            (lock.getComment() != null)
                                                                    ? new ListElement(lock.getComment())
                                                                    : new ListElement(),
                                                            lock.getCreated(),
                                                            (lock.getExpires() != null)
                                                                    ? new ListElement(lock.getExpires())
                                                                    : new ListElement())));
                    break;
                case FAILED:
                    ret.add(new ListElement(Word.STATUS_FAILURE,
                                            new ListElement(
                                                    new ListElement(160035,
                                                                    (lock.getComment() != null)
                                                                            ? lock.getComment()
                                                                            : "",
                                                                    "",
                                                                    0))));
                    break;
            }
        }

        this.writeItemList(ret);
        this.out.write("done ".getBytes("UTF8"));
        this.writeItemList(SVNServerSession.EMPTY_SUCCESS);
    }

    /**
     * Unlock given paths.
     *
     * <h3>SVN Call:</h3>
     * <table>
     * <tr><td><code style="color:green">( unlock-many (</code></td></tr>
     * <tr><td rowspan="2">
     *     <td><code style="color:green">break-lock:<a href="#bool">bool</a></code></td>
     *     <td>if &quot;<code>true</code>&quot; and some paths are locked from
     *         another user, the existing locks are broken (forced)</td></tr>
     * <tr><td><code style="color:green">( ( path:<a href="#string">string</a>
                 ( ?token:<a href="#string">string</a> ) ) ... )</code></td></tr>
     * <tr><td><code style="color:green">) )</code></td></tr>
     * </table>
     *
     * <h3>SVN Response:</h3>
     * <table>
     * <tr><td><code style="color:green">
     *         authorization:<a href="noauthorization">no-authorization</a>
     *         </code></td></tr>
     * <tr><td><code style="color:green">( success ( path:<a href="string">string</a> ) )  | ( failure ( err:<a href="error">error</a> ) ) | done</code></td>
     *     <td>Before sending response, server sends unlocked paths, ending
     *         with &quot;<code>done</code>&quot;.</td></tr>
     * <tr><td><code style="color:green">( success ( ) )</code></td></tr>
     * </table>
     *
     * @param _parameters
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    protected void svnUnlockMany(final List<AbstractElement<?>> _parameters)
            throws UnsupportedEncodingException, IOException
    {
        // break lock?
        final boolean breakLock = (_parameters.get(0).getWord() == Word.BOOLEAN_TRUE);
        // paths with related lock tokens
        final Map<String,String> paths = new HashMap<String,String>();
        for (final AbstractElement<?> onePath : _parameters.get(1).getList())  {
            final List<AbstractElement<?>> tokenList = onePath.getList().get(1).getList();
            paths.put(onePath.getList().get(0).getString(),
                      tokenList.isEmpty() ? null : tokenList.get(0).getString());
        }

        final LockDescriptionList locks = this.getRepository().unlock(breakLock, paths);

        final List<ListElement> ret = new ArrayList<ListElement>();
        ret.add(SVNServerSession.NO_AUTHORIZATION_NEEDED);

        for (final LockDescription lock : locks.getLockDescriptions())  {
            switch (lock.getStatus())  {
                case SUCCESSFULLY:
                    ret.add(new ListElement(Word.STATUS_SUCCESS,
                                            new ListElement(lock.getPath())));
                    break;
                case FAILED:
                    ret.add(new ListElement(Word.STATUS_FAILURE,
                                            new ListElement(
                                                    new ListElement(160035,
                                                                    (lock.getComment() != null)
                                                                            ? lock.getComment()
                                                                            : "",
                                                                    "",
                                                                    0))));
                    break;
            }
        }

        this.writeItemList(ret);
        this.out.write("done ".getBytes("UTF8"));
        this.writeItemList(SVNServerSession.EMPTY_SUCCESS);
    }

    /**
     * Returns for a file path the lock information.
     *
     * <h3>SVN Call:</h3>
     * <table>
     * <tr><td><code style="color:green">( get-lock (</code></td></tr>
     * <tr><td  rowspan="1"></td><td><code style="color:green">
     *     path:<a href="#string">string</a></code></td>
     *     <td>file path for which the lock description is searched</td></tr>
     * <tr><td><code style="color:green">) )</code></td></tr>
     * </table>
     *
     * <h3>SVN Response:</h3>
     * <table>
     * <tr><td><code style="color:green">authorization:<a href="noauthorization">no-authorization</a></code></td></tr>
     * <tr><td><code style="color:green">( success ( ?lock:<a href="lockdesc">lockdesc</a> ) )</code></td>
     *     <td>for given file path the lock description</td></tr>
     * </table>
     *
     * @param _parameters
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    protected void svnGetLock(final List<AbstractElement<?>> _parameters)
            throws UnsupportedEncodingException, IOException
    {
        final String path = this.buildPath(_parameters.get(0).getString());

        final LockDescription lockDesc = this.getRepository().getFileLock(path);

        final ListElement lock = new ListElement();
        if (lockDesc != null)  {
            lock.add(new ListElement(lockDesc.getPath(),
                                     lockDesc.getToken(),
                                     lockDesc.getOwner(),
                                     (lockDesc.getComment() != null)
                                             ? new ListElement(lockDesc.getComment())
                                             : new ListElement(),
                                     lockDesc.getCreated(),
                                     (lockDesc.getExpires() != null)
                                             ? new ListElement(lockDesc.getExpires())
                                             : new ListElement()));
        }
        this.writeItemList(SVNServerSession.NO_AUTHORIZATION_NEEDED,
                           new ListElement(Word.STATUS_SUCCESS, new ListElement(lock)));
    }

    /**
     * Returns all locks on or below given path, that is if the repository
     * entry (located at the path) is a directory then the method returns
     * locks of all locked files (if any) in it.
     *
     * <h3>SVN Call:</h3>
     * <table>
     * <tr><td><code style="color:green">( get-locks (</code></td></tr>
     * <tr><td  rowspan="1"></td><td><code style="color:green">
     *     path:<a href="#string">string</a></code></td>
     *     <td>path for which or for which below the lock descriptions are
     *         searched</td></tr>
     * <tr><td><code style="color:green">) )</code></td></tr>
     * </table>
     *
     * <h3>SVN Response:</h3>
     * <table>
     * <tr><td><code style="color:green">authorization:<a href="noauthorization">no-authorization</a></code></td></tr>
     * <tr><td><code style="color:green">( success
     *         ( ?lock:<a href="lockdesc">lockdesc</a> ... ) )</code></td>
     *     <td>for given path or below all found lock descriptions</td></tr>
     * </table>
     *
     * @param _parameters
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    protected void svnGetLocks(final List<AbstractElement<?>> _parameters)
            throws UnsupportedEncodingException, IOException
    {
        final String path = this.buildPath(_parameters.get(0).getString());

        final LockDescriptionList lockDescList = this.getRepository().getLocks(path);

        final ListElement locks = new ListElement();
        if (lockDescList != null)  {
            for (final LockDescription lockDesc : lockDescList.getLockDescriptions())  {
                locks.add(new ListElement(lockDesc.getPath(),
                                     lockDesc.getToken(),
                                     lockDesc.getOwner(),
                                     (lockDesc.getComment() != null)
                                             ? new ListElement(lockDesc.getComment())
                                             : new ListElement(),
                                     lockDesc.getCreated(),
                                     (lockDesc.getExpires() != null)
                                             ? new ListElement(lockDesc.getExpires())
                                             : new ListElement()));
            }
        }
        this.writeItemList(SVNServerSession.NO_AUTHORIZATION_NEEDED,
                           new ListElement(Word.STATUS_SUCCESS, new ListElement(locks)));
    }

    /**
     * The current parent path {@link #currentPath} is changed to a new path.
     *
     * <h4>SVN Call:</h4>
     * <code style="color:green">(reparent ( url:<a href="#string">string</a> ))</code>
     *
     * <h4>SVN Return:</h4>
     * <code style="color:green">( success ( ) )</code>
     *
     * @param _parameters   list of SVN re-parent parameters
     * @throws UnsupportedEncodingException if the returned values could not be
     *                                      encoded to UTF8
     * @throws IOException                  if to the output stream could not
     *                                      be written
     * @throws URISyntaxException           if the new parent URI could not be
     *                                      read (because of wrong syntax)
     */
    protected void svnReparent(final List<AbstractElement<?>> _parameters)
            throws UnsupportedEncodingException, IOException, URISyntaxException
    {
        final URI uri = new URI(_parameters.get(0).getString());
        final String path = "".equals(uri.getPath()) ? "/" : uri.getPath();

        this.currentPath = path.substring(this.repository.getRootPath().length()
                                                  + this.repository.getRepositoryPath().length());
        this.writeItemList(SVNServerSession.NO_AUTHORIZATION_NEEDED,
                           new ListElement(Word.STATUS_SUCCESS, new ListElement()));
    }

    /**
     *
     * <h3>SVN Call:</h3>
     * <code style="color:green">(stat (
     *      path:<a href="#string">string</a>
     *      ( rev:<a href="#number">number</a> ) )</code>
     *
     * <h3>SVN Response:</h3>
     * <code style="color:green">( ? ( entry:dirent ) )</code><br/>
     * <table>
     * <tr><td valign="top"><b>dirent</b> = </td>
     *     <td><code style="color:green">kind:<a href="nodekind">node-kind</a></code><br/>
     *         <code style="color:green">size:<a href="number">number</a></code><br/>
     *         <code style="color:green">has-props:<a href="bool">bool</a></code><br/>
     *         <code style="color:green">created-rev:<a href="number">number</a></code><br/>
     *         <code style="color:green">( created-date:<a href="#string">string</a> )</code><br/>
     *         <code style="color:green">( ?last-author:<a href="#string">string</a> )</code></td></tr>
     * </table>
     * If path is non-existent, an empty response
     * &quot;<code>( ( ) )</code>&quot; is returned.
     * If no &quot;<code>created-date</code>&quot; exists, null date
     * time {@link StringElement#NULL_DATETIME} is used (because a date is
     * always required). If no &quot;<code>last-author</code>&quot; exists,
     * only zero length list &quot;<code>( )</code>&quot; is used.
     *
     * @param _parameters
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    protected void svnStat(final List<AbstractElement<?>> _parameters)
           throws UnsupportedEncodingException, IOException
    {
        final String path = this.buildPath(_parameters.get(0).getString());
        final Long revision = _parameters.get(1).getList().get(0).getNumber();

        final DirEntry entry = this.getRepository().stat(revision, path, false);

        if (entry != null)  {
            this.writeItemList(SVNServerSession.NO_AUTHORIZATION_NEEDED,
                    new ListElement(Word.STATUS_SUCCESS,
                                    new ListElement(new ListElement(
                    new ListElement(entry.getKind(),
                                    entry.getFileSize(),
                                    Word.BOOLEAN_FALSE,/* hasprops */
                                    entry.getRevision(),
                                    new ListElement(entry.getDate() != null ? entry.getDate() : StringElement.NULL_DATETIME),
                                    (entry.getAuthor() != null)
                                            ? new ListElement(entry.getAuthor())
                                            : new ListElement())))));
        } else  {
            this.writeItemList(SVNServerSession.NO_AUTHORIZATION_NEEDED,
                               SVNServerSession.EMPTY_SUCCESS);
        }
    }

    /**
     * <h3>SVN Call:</h3>
     * <table>
     * <tr><td><code style="color:green">( update (</code></td></tr>
     * <tr><td rowspan="5"></td><td><code style="color:green">( ?rev:<a href="#number">number</a> )</code></td>
     *     <td></td></tr>
     * <tr><td><code style="color:green">target:<a href="#string">string</a></code></td>
     *     <td></td></tr>
     * <tr><td><code style="color:green">recurse:<a href="#bool">bool</a></code></td>
     *     <td></td></tr>
     * <tr><td><code style="color:green">?depth:<a href="#depth">depth</a></code></td>
     *     <td></td></tr>
     * <tr><td><code style="color:green">send_copy_from_param:<a href="#bool">bool</a></code></td>
     *     <td></td></tr>
     * <tr><td><code style="color:green">) )</code></td></tr>
     * </table>
     *
     * <h3>SVN Response from Server to Client:</h3>
     * <code style="color:green">authorization:<a href="noauthorization">no-authorization</a></code><br/>
     * Server sends that no authorization is needed.
     *
     * <h3>SVN Response from Client to Server:</h3>
     * Client switches to report command set.
     * ....
     *
     * <h3>SVN Respone from Server to Client</h3>
     * <code style="color:green">authorization:<a href="noauthorization">no-authorization</a></code><br/>
     * Server switches to editor command set.
     * After edit completes, server sends response.<br/>
     * response: ( )
     *
     * @param _parameters
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    protected void svnUpdate(final List<AbstractElement<?>> _parameters)
            throws UnsupportedEncodingException, IOException
    {
        // revision number
        final List<AbstractElement<?>> revisionParams = _parameters.get(0).getList();
        final long revision = (revisionParams.isEmpty()) ? -1 : revisionParams.get(0).getNumber();
        // update path
        final String path = this.buildPath(_parameters.get(1).getString());
        // recurse?
        final boolean recurse = (_parameters.get(2).getWord() == Word.BOOLEAN_TRUE);
        // depth and send copy from parameters
        final boolean sendCopyFromParameters;
        Depth depth = Depth.valueOf(_parameters.get(3).getWord());
        if (depth == null)  {
            depth = Depth.UNKNOWN;
            sendCopyFromParameters = (_parameters.get(3).getWord() == Word.BOOLEAN_TRUE);
        } else  {
            sendCopyFromParameters = (_parameters.get(4).getWord() == Word.BOOLEAN_TRUE);
        }
        if ((depth == Depth.UNKNOWN) && recurse)  {
            depth = Depth.INFINITY;
        }


        this.writeItemList(SVNServerSession.NO_AUTHORIZATION_NEEDED);

        final ReportList report = new ReportList();
        report.read(this);

        this.writeItemList(SVNServerSession.NO_AUTHORIZATION_NEEDED);

        final Editor deltaEditor = this.getRepository().getStatus(revision, path, depth, report);

        // editor mode
        deltaEditor.write(this);

        this.writeItemList(SVNServerSession.EMPTY_SUCCESS);

final ListElement result = this.readItemList();
if ((result != null) && (result.getList().get(0).getWord() != Word.STATUS_SUCCESS))  {
    throw new Error("update does not work");
}

    }

    /**
     * Returns for given path the status
     *
     * <p><b>SVN Call:</b></br>
     * <table>
     * <tr><td><code style="color:green"><nobr>( status (</nobr></code>
     *     </td></tr>
     * <tr><td  rowspan="4"></td><td><code style="color:green">
     *     target:<a href="#string">string</a></code></td>
     *     <td>target path for which a status is searched</td></tr>
     * <tr><td><code style="color:green">
     *     recurse:<a href="#bool">bool</a></code></td>
     *     <td>if true and status scope is a directory or unknown (not
     *         defined), descends recursively; otherwise not</td></tr>
     * <tr><td><code style="color:green">
     *     ?( ?rev:<a href="#number">number</a> )</code></td>
     *     <td>revision to get status against; if not defined HEAD revision is
     *         used</td></tr>
     * <tr><td><code style="color:green">
     *     ?depth:<a href="#depth">depth</a></code></td>
     *     <td>defines the depth scope</td></tr>
     * <tr><td><code style="color:green">) )</code></td></tr>
     * </table></p>
     *
     * @param _parameters
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @todo description switch to report command set, switch to editor
     *       command set, empty response
     */
    protected void svnStatus(final List<AbstractElement<?>> _parameters)
            throws UnsupportedEncodingException, IOException
    {
        // status path
        final String path = this.buildPath(_parameters.get(0).getString());
        // recurse?
        final boolean recurse = (_parameters.get(1).getWord() == Word.BOOLEAN_TRUE);
        // revision number
        final List<AbstractElement<?>> revisionParams = (_parameters.size() > 1) ? _parameters.get(2).getList() : null;
        final Long revision = ((revisionParams == null) || revisionParams.isEmpty())
                              ? null
                              : revisionParams.get(0).getNumber();
        // depth
        Depth depth = (_parameters.size() > 2)
                      ? Depth.valueOf(_parameters.get(3).getWord())
                      : Depth.UNKNOWN;
        if (depth == null)  {
            depth = Depth.UNKNOWN;
        }
        if ((depth == Depth.UNKNOWN) && recurse)  {
            depth = Depth.INFINITY;
        }

        this.writeItemList(SVNServerSession.NO_AUTHORIZATION_NEEDED);

        final ReportList report = new ReportList();
        report.read(this);

        this.writeItemList(SVNServerSession.NO_AUTHORIZATION_NEEDED);

        final Editor deltaEditor = this.getRepository().getStatus(revision, path, depth, report);

        // editor mode
        deltaEditor.write(this);

        this.writeItemList(SVNServerSession.EMPTY_SUCCESS);
    }

    /**
     * <h3>SVN Call:</h3>
     * <table>
     * <tr><td><code style="color:green"><nobr>( log (</nobr></code></td></tr>
     * <tr><td  rowspan="8"></td><td><code style="color:green">
     *     ( target-path:<a href="#string">string</a> ... )</code></td>
     *     <td>defines the path for which the log is requested; only one path
     *     is supported</td></tr>
     * <tr><td><code style="color:green">
     *     ( start-rev:<a href="#number">number</a> )</code></td>
     *     <td>defines revision to start log with</td></tr>
     * <tr><td><code style="color:green">
     *     ( end-rev:<a href="#number">number</a> )</code></td>
     *     <td>defines the revision to end log at</td></tr>
     * <tr><td valign="top"><code style="color:green">
     *     changed-paths:<a href="#bool">bool</a></code></td>
     *     <td>if set to &quot;<code>true</code>&quot; then revision
     *         information must also include all changed paths per revision
     *         (the &quot;<code>( change:changed-path-entry )...</code>&quot;
     *         in the SVN response); otherwise not</td></tr>
     * <tr><td><code style="color:green">
     *     strict-node:<a href="#bool">bool</a></code></td>
     *     <td>currently ignored</td></tr>
     * <tr><td><code style="color:green">
     *     ?limit:<a href="#number">number</a></code></td>
     *     <td>currently ignored</td></tr>
     * <tr><td><code style="color:green">
     *     ?include-merged-revisions:<a href="#bool">bool</a></code></td>
     *     <td>currently ignored</td></tr>
     * <tr><td><code style="color:green">
     *     all-revprops | revprops ( revprop:<a href="#string">string</a> ... )
     *     </code></td>
     *     <td>currently ignored</td></tr>
     * <tr><td><code style="color:green">) )</code></td></tr>
     * </table>
     *
     *
     * <h3>SVN Response:</h3>
     * <pre>
     * ( log:log-entry ) ... done ( success ( ) )</pre>
     * <table>
     * <tr><td valign="top" rowspan="9"><b>log-entry</b> = </td>
     *         <td valign="top"><code style="color:green">( ( change:changed-path-entry ) ... )</code></td>
     *         <td>the change path entries are only included if
     *             &quot;<code>changed-paths</code>&quot; from the SVN call was
     *             set to &quot;<code>true</code>; otherwise only zero length
     *             list <nobr>&quot;<code>( )</code>&quot;</nobr> is used</td></tr>
     *     <tr><td><code style="color:green">rev:<a href="#number">number</a></code></td></tr>
     *     <tr><td><code style="color:green">( author:<a href="#string">string</a> )</code></td></tr>
     *     <tr><td><code style="color:green">( date:<a href="#string">string</a> )</code></td></tr>
     *     <tr><td><code style="color:green">( message:<a href="#string">string</a> )</code></td></tr>
     *     <tr><td><code style="color:green">has-children:<a href="#bool">bool</a></code></td></tr>
     *     <tr><td><code style="color:green">invalid-revnum:<a href="#bool">bool</a></code></td></tr>
     *     <tr><td><code style="color:green">revprop-count:<a href="#number">number</a></code></td></tr>
     *     <tr><td><code style="color:green">rev-props:<a href="#proplist">proplist</a></code></td></tr>
     * <tr><td valign="top" rowspan="3"><nobr><b>changed-path-entry</b> =</nobr></td>
     *         <td><code style="color:green">path:<a href="#string">string</a></code></td></tr>
     *     <tr><td valign="top"><code style="color:green">A|D|R|M</code></td>
     *          <td>defines the modified flag:<br/>
     *              A: element is added<br/>
     *              D: element is deleted<br/>
     *              R: replaced(?)<br/>
     *              M: content is modified</td></tr>
     *     <tr><td valign="top"><nobr><code style="color:green">
     *               ( ?copy-path:<a href="#string">string</a>
     *                 ?copy-rev:<a href="#number">number</a> )</code></nobr></td>
     *         <td>The copy path and revision value are only defined if the
     *             modified kind is A (added). Otherwise only the open and
     *             close brace is written.</td></tr>
     * </table>
     *
     * @param _parameters     SVN log parameter
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    protected void svnLog(final List<AbstractElement<?>> _parameters)
            throws UnsupportedEncodingException, IOException
    {
        // get paths from parameter list
        final List<AbstractElement<?>> pathParameters = _parameters.get(0).getList();
        final String[] paths = new String[pathParameters.size()];
        int idx = 0;
        for (final AbstractElement<?> pathParameter : pathParameters)  {
            paths[idx] = this.buildPath(pathParameter.getString());
            idx++;
        }

        final long startRevision = _parameters.get(1).getList().get(0).getNumber();
        final long endRevision = _parameters.get(2).getList().get(0).getNumber();
        final boolean inclChangedPaths = (_parameters.get(3).getWord() == Word.BOOLEAN_TRUE);

        final LogEntryList logEntryList = this.getRepository().getLog(startRevision,
                                                                 endRevision,
                                                                 inclChangedPaths,
                                                                 paths);
        final List<ListElement> list = new ArrayList<ListElement>();
        for (final LogEntry logEntry : logEntryList.getLogEntries())  {
            final ListElement oneLog = new ListElement();
            list.add(oneLog);

            final ListElement changedPathsLE = new ListElement();
            for (final ChangedPath changedPath : logEntry.getChangedPaths())  {
                final ListElement changedPathLE = new ListElement(changedPath.getPath(),
                                                                  changedPath.getKind());
                if (changedPath.getCopiedFromPath() != null)  {
                    changedPathLE.add((new ListElement(changedPath.getCopiedFromPath(),
                                                       changedPath.getCopiedFromRevision())));
                } else  {
                    changedPathLE.add(new ListElement());
                }
                changedPathsLE.add(changedPathLE);
            }
            oneLog.add(changedPathsLE,
                       logEntry.getRevision(),
                       (logEntry.getAuthor() != null) ? new ListElement(logEntry.getAuthor()) : new ListElement(),
                       new ListElement(logEntry.getModified()),
                       (logEntry.getComment() != null) ? new ListElement(logEntry.getComment()) : new ListElement(),
                       Word.BOOLEAN_FALSE,
                       Word.BOOLEAN_FALSE,
                       0,
                       new ListElement());
        }

        this.writeItemList(SVNServerSession.NO_AUTHORIZATION_NEEDED);
        this.writeItemList(list.toArray(new ListElement[list.size()]));
        this.out.write("done ( success ( ) ) ".getBytes("UTF8"));
    }

    /**
     * Returns the path locations in revision history.
     *
     * <p><b>SVN Call:</b></br>
     * <table>
     * <tr><td><code style="color:green"><nobr>( get-locations (</nobr></code>
     *     </td></tr>
     * <tr><td  rowspan="3"></td><td><code style="color:green">
     *     path:<a href="#string">string</a></code></td>
     *     <td>path to look up</td></tr>
     * <tr><td><code style="color:green">
     *     peg-rev:<a href="#number">number</a></code></td>
     *     <td>revision number in which the path are looked up</td></tr>
     * <tr><td><code style="color:green">
     *     ( rev:<a href="#number">number</a> ... )</code></td>
     *     <td>list of interesting revisions</td></tr>
     * <tr><td><code style="color:green">) )</code></td></tr>
     * </table></p>
     *
     * <p><b>SVN Response:</b></br>
     * Before the reponse is sent, the server sends all location entries (see
     * {@link LocationEntries#write(SVNServerSession)}). The response itself
     * is the empty success response {@link #EMPTY_SUCCESS}.</p>
     *
     * @param _parameters   SVN get locations parameters
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @see LocationEntries
     */
    protected void svnGetLocations(final List<AbstractElement<?>> _parameters)
            throws UnsupportedEncodingException, IOException
    {
        // location path
        final String path = this.buildPath(_parameters.get(0).getString());
        // peg revision number
        final long pegRevision = _parameters.get(1).getNumber();
        // interesting revisions
        final List<AbstractElement<?>> revList = _parameters.get(2).getList();
        final long[] revisions = new long[revList.size()];
        int idx = 0;
        for (final AbstractElement<?> revElem : revList)  {
            revisions[idx++] = revElem.getNumber();
        }

        this.writeItemList(SVNServerSession.NO_AUTHORIZATION_NEEDED);

        final LocationEntries entries = this.repository.getLocations(pegRevision, path, revisions);
        entries.write(this);

        this.writeItemList(SVNServerSession.EMPTY_SUCCESS);
    }

    /**
     *
     *
     * @param _path
     * @return
     * @see #currentPath
     * @see #svnReparent(List)
     */
    protected String buildPath(final String _path)
    {
        final StringBuilder completePath = new StringBuilder(this.currentPath);
        if (_path != null)  {
                completePath.append(_path);
        }
        return completePath.toString();
    }

    public ListElement readItemList()
            throws IOException
    {
        final ListElement list = new ListElement();
        final int ch = this.in.read();
        if (ch != -1)  {
            if (((char) ch) != '(')  {
                throw new Error("fehler:" + (char)ch);
            }
            if (!Character.isWhitespace((char) this.in.read()))  {
                throw new Error("fehler");
            }
            list.read(this.in);
            if (SVNServerSession.LOGGER.isTraceEnabled())  {
                final ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
                list.write(byteArrayOut);
                SVNServerSession.LOGGER.trace("RES<: {}", byteArrayOut.toString());
            }
        }
        return (ch == -1) ? null : list;
    }

    public void writeItemList(final ListElement... _lists)
            throws UnsupportedEncodingException, IOException
    {
        for (final ListElement list : _lists)  {
            if (SVNServerSession.LOGGER.isTraceEnabled())  {
                final ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
                list.write(byteArrayOut);
                SVNServerSession.LOGGER.trace("REQ>: {}", byteArrayOut.toString());
            }
            list.write(this.out);
        }
        this.out.flush();
    }

    public void writeItemList(final List<ListElement> _lists)
            throws UnsupportedEncodingException, IOException
    {
        for (final ListElement list : _lists)  {
            if (SVNServerSession.LOGGER.isTraceEnabled())  {
                final ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
                list.write(byteArrayOut);
                SVNServerSession.LOGGER.trace("REQ>: {}", byteArrayOut.toString());
            }
            list.write(this.out);
        }
        this.out.flush();
    }

    public void write(final String _text)
            throws UnsupportedEncodingException, IOException
    {
        SVNServerSession.LOGGER.trace("REQ>: {}", _text);
        this.out.write(_text.getBytes("UTF8"));
        this.out.flush();
    }

    public OutputStream getOut()
    {
        return this.out;
    }

    public IRepository getRepository()
    {
        return this.repository;
    }

}