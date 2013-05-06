/*
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
package org.apache.hadoop.hbase.ipc;

import java.io.IOException;

import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;

import org.apache.hadoop.hbase.security.SaslUtil;

public final class SaslClients {

  public interface SaslClientProvider {

    SaslClient createDigestSaslClient(String[] mechanismNames, String saslDefaultRealm, 
        CallbackHandler saslClientCallbackHandler) throws IOException;

    SaslClient createKerberosSaslClient(String[] mechanismNames,
        String userFirstPart, String userSecondPart) throws IOException;
  }

  private static SaslClientProvider provider = new DefaultSaslClientProvider();

  public static void setSaslRpcClientProvider(SaslClientProvider saslClientProvider) {
    provider = saslClientProvider;
  }

  private static final class DefaultSaslClientProvider implements SaslClientProvider {
    @Override
    public SaslClient createDigestSaslClient(String[] mechanismNames, String saslDefaultRealm, 
        CallbackHandler saslClientCallbackHandler) throws IOException {
      return Sasl.createSaslClient(mechanismNames, null, null, saslDefaultRealm,
          SaslUtil.SASL_PROPS, saslClientCallbackHandler);
    }

    @Override
    public SaslClient createKerberosSaslClient(String[] mechanismNames,
        String userFirstPart, String userSecondPart) throws IOException {
      return Sasl.createSaslClient(mechanismNames, null, userFirstPart, userSecondPart,
          SaslUtil.SASL_PROPS, null);
    }
  }

  public static SaslClient getDigestSaslClient(String[] mechanismNames, 
      String saslDefaultRealm, CallbackHandler saslClientCallbackHandler) throws IOException {
    return provider.createDigestSaslClient(mechanismNames, saslDefaultRealm, saslClientCallbackHandler);
  }

  public static SaslClient getKerberosSaslClients(String[] mechanismNames,
      String userFirstPart, String userSecondPart) throws IOException {
    return provider.createKerberosSaslClient(mechanismNames, userFirstPart, 
        userSecondPart);
  }
}
