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

package com.googlecode.jsvnserve.test.svnproxy;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.AuthorizeCallback;
import javax.security.sasl.RealmCallback;

/**
 * Dummy callback handler which accept every user if the name and password is
 * equal.
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class SVNProxyCallbackHandler
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
            pc.setPassword(nc.getDefaultName().toCharArray());
        }
    }
}
