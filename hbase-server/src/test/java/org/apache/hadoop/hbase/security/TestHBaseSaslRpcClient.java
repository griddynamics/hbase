/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hbase.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;
import javax.security.sasl.RealmChoiceCallback;
import javax.security.sasl.SaslClient;

import org.apache.hadoop.hbase.SmallTests;
import org.apache.hadoop.hbase.ipc.SaslClients;
import org.apache.hadoop.hbase.ipc.SaslClients.SaslClientProvider;
import org.apache.hadoop.hbase.security.HBaseSaslRpcClient.SaslClientCallbackHandler;
import org.apache.hadoop.security.token.Token;
import org.apache.hadoop.security.token.TokenIdentifier;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import com.google.common.base.Strings;

@Category(SmallTests.class)
public class TestHBaseSaslRpcClient {  
  static final String DEFAULT_USER_NAME = "principal";
  static final String DEFAULT_USER_PASSWORD = "password";

  private static final Logger LOG = Logger.getLogger(TestHBaseSaslRpcClient.class);  

  @BeforeClass
  public static void before() {
    Logger.getRootLogger().setLevel(Level.DEBUG);
  }

  @Test
  public void testSaslClientCallbackHandler() throws UnsupportedCallbackException {
    final Token<? extends TokenIdentifier> token = createTokenMock();
    when(token.getIdentifier()).thenReturn(DEFAULT_USER_NAME.getBytes());
    when(token.getPassword()).thenReturn(DEFAULT_USER_PASSWORD.getBytes());

    final NameCallback nameCallback = mock(NameCallback.class);
    final PasswordCallback passwordCallback = mock(PasswordCallback.class);
    final RealmCallback realmCallback = mock(RealmCallback.class);
    final RealmChoiceCallback realmChoiceCallback = mock(RealmChoiceCallback.class);

    Callback[] callbackArray = {nameCallback, passwordCallback, 
        realmCallback, realmChoiceCallback};
    final SaslClientCallbackHandler saslClCallbackHandler = new SaslClientCallbackHandler(token);
    saslClCallbackHandler.handle(callbackArray);
    verify(nameCallback).setName(anyString());
    verify(realmCallback).setText(anyString());
    verify(passwordCallback).setPassword(any(char[].class));
  }

  @Test
  public void testSaslClientCallbackHandlerWithException() {
    final Token<? extends TokenIdentifier> token = createTokenMock();
    when(token.getIdentifier()).thenReturn(DEFAULT_USER_NAME.getBytes());
    when(token.getPassword()).thenReturn(DEFAULT_USER_PASSWORD.getBytes());    
    final SaslClientCallbackHandler saslClCallbackHandler = new SaslClientCallbackHandler(token);
    try {
      saslClCallbackHandler.handle(new Callback[] { mock(TextOutputCallback.class) });
    } catch (UnsupportedCallbackException expEx) {
      //expected
    } catch (Exception ex) {
      fail("testSaslClientCallbackHandlerWithException error : " + ex.getMessage());
    }
  }

  @Test
  public void testHBaseSaslRpcClientCreation() throws Exception {
    //creation kerberos principal check section
    assertFalse(assertSuccessCreationKerberosPrincipal(null));
    assertFalse(assertSuccessCreationKerberosPrincipal("DOMAIN.COM"));
    assertFalse(assertSuccessCreationKerberosPrincipal("principal/DOMAIN.COM"));
    assertTrue(assertSuccessCreationKerberosPrincipal("principal/localhost@DOMAIN.COM"));

    //creation digest principal check section
    assertFalse(assertSuccessCreationDigestPrincipal(null, null));
    assertFalse(assertSuccessCreationDigestPrincipal("", ""));
    assertFalse(assertSuccessCreationDigestPrincipal("", null));
    assertFalse(assertSuccessCreationDigestPrincipal(null, ""));
    assertTrue(assertSuccessCreationDigestPrincipal(DEFAULT_USER_NAME, DEFAULT_USER_PASSWORD));

    //creation simple principal check section
    assertFalse(assertSuccessCreationSimplePrincipal("", ""));
    assertFalse(assertSuccessCreationSimplePrincipal(null, null));
    assertFalse(assertSuccessCreationSimplePrincipal(DEFAULT_USER_NAME, DEFAULT_USER_PASSWORD));

    //exceptions check section
    assertTrue(assertIOExceptionThenSaslClientIsNull(DEFAULT_USER_NAME, DEFAULT_USER_PASSWORD));
    assertTrue(assertIOExceptionWhenGetStreamsBeforeConnectCall(
        DEFAULT_USER_NAME, DEFAULT_USER_PASSWORD));
  }

  private boolean assertIOExceptionWhenGetStreamsBeforeConnectCall(String principal, String password)
      throws IOException {
    boolean inState = false;
    boolean outState = false;

    SaslClients.setSaslRpcClientProvider(new SaslClientProvider() {
      @Override
      public SaslClient createDigestSaslClient(String[] mechanismNames,
          String saslDefaultRealm, CallbackHandler saslClientCallbackHandler)
              throws IOException {
        return Mockito.mock(SaslClient.class);
      }

      @Override
      public SaslClient createKerberosSaslClient(String[] mechanismNames,
          String userFirstPart, String userSecondPart) throws IOException {
        return Mockito.mock(SaslClient.class);
      }
    });

    HBaseSaslRpcClient rpcClient = createSaslRpcClientForDigest(principal, password);
    try {
      rpcClient.getInputStream(Mockito.mock(InputStream.class));
    } catch(IOException ex) {
      //Sasl authentication exchange hasn't completed yet
      inState = true;
    }

    try {
      rpcClient.getOutputStream(Mockito.mock(OutputStream.class));
    } catch(IOException ex) {
      //Sasl authentication exchange hasn't completed yet
      outState = true;
    }

    return inState && outState;
  }

  private boolean assertIOExceptionThenSaslClientIsNull(String principal, String password) {
    //set provider which return null for test
    SaslClients.setSaslRpcClientProvider(new SaslClientProvider() {
      @Override
      public SaslClient createDigestSaslClient(String[] mechanismNames,
          String saslDefaultRealm, CallbackHandler saslClientCallbackHandler)
              throws IOException {
        return null;
      }

      @Override
      public SaslClient createKerberosSaslClient(String[] mechanismNames,
          String userFirstPart, String userSecondPart) throws IOException {
        return null;
      }
    });

    try {
      createSaslRpcClientForDigest(principal, password);
    } catch (IOException ex) {
      return true;
    }
    return false;
  }

  private boolean assertSuccessCreationKerberosPrincipal(String principal) {
    HBaseSaslRpcClient rpcClient = null;
    try {
      rpcClient = createSaslRpcClientForKerberos(principal);
    } catch(Exception ex) {
      LOG.error(ex.getMessage());
    }
    return rpcClient != null;
  }

  private boolean assertSuccessCreationDigestPrincipal(String principal, String password) {
    HBaseSaslRpcClient rpcClient = null;
    try {
      rpcClient = createSaslRpcClientForDigest(principal, password);
    } catch(Exception ex) {
      LOG.error(ex.getMessage());
    }
    return rpcClient != null;
  }

  private boolean assertSuccessCreationSimplePrincipal(String principal, String password) {
    HBaseSaslRpcClient rpcClient = null;
    try {
      rpcClient = createSaslRpcClientSimple(principal, password);
    } catch(Exception ex) {
      LOG.error(ex.getMessage());
    }
    return rpcClient != null;
  }

  private HBaseSaslRpcClient createSaslRpcClientForKerberos(String principal) throws IOException {
    return new HBaseSaslRpcClient(AuthMethod.KERBEROS, createTokenMock(), principal);
  }

  private HBaseSaslRpcClient createSaslRpcClientForDigest(String principal, String password) throws IOException {    
    Token<? extends TokenIdentifier> token = createTokenMock();
    if (!Strings.isNullOrEmpty(principal) && !Strings.isNullOrEmpty(password)) {
      when(token.getIdentifier()).thenReturn(DEFAULT_USER_NAME.getBytes());
      when(token.getPassword()).thenReturn(DEFAULT_USER_PASSWORD.getBytes());
    }
    return new HBaseSaslRpcClient(AuthMethod.DIGEST, token, principal);
  }

  private HBaseSaslRpcClient createSaslRpcClientSimple(String principal, String password) throws IOException {
    return new HBaseSaslRpcClient(AuthMethod.SIMPLE, createTokenMock(), principal);
  }

  @SuppressWarnings("unchecked")
  private Token<? extends TokenIdentifier> createTokenMock() {
    return mock(Token.class);
  }
}
