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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.UserProvider;
import org.apache.hadoop.hbase.security.HBaseSaslRpcServer.AuthMethod;
import org.apache.hadoop.hbase.security.User;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.security.UserGroupInformation;

/**
 * The IPC connection header sent by the client to the server
 * on connection establishment.  Part of the {@link SecureRpcEngine}
 * implementation.
 */
class SecureConnectionHeader extends ConnectionHeader {
  private User user = null;
  private AuthMethod authMethod;

  public SecureConnectionHeader() {}

  /**
   * Create a new {@link org.apache.hadoop.hbase.ipc.SecureConnectionHeader} with the given <code>protocol</code>
   * and {@link org.apache.hadoop.security.UserGroupInformation}.
   * @param protocol protocol used for communication between the IPC client
   *                 and the server
   * @param ugi {@link org.apache.hadoop.security.UserGroupInformation} of the client communicating with
   *            the server
   */
  public SecureConnectionHeader(String protocol, User user, AuthMethod authMethod) {
    this.protocol = protocol;
    this.user = user;
    this.authMethod = authMethod;
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    protocol = Text.readString(in);
    if (protocol.isEmpty()) {
      protocol = null;
    }
    boolean ugiUsernamePresent = in.readBoolean();
    if (ugiUsernamePresent) {
      String username = in.readUTF();
      boolean realUserNamePresent = in.readBoolean();
      Configuration conf = HBaseConfiguration.create();
      UserProvider provider = UserProvider.instantiate(conf);
      if (realUserNamePresent) {
        String realUserName = in.readUTF();
        UserGroupInformation realUserUgi =
            UserGroupInformation.createRemoteUser(realUserName);
        user = provider.create(
            UserGroupInformation.createProxyUser(username, realUserUgi));
      } else {
        user = provider.create(UserGroupInformation.createRemoteUser(username));
      }
    } else {
      user = null;
    }
  }

  @Override
  public void write(DataOutput out) throws IOException {
    Text.writeString(out, (protocol == null) ? "" : protocol);
    if (user != null) {
      UserGroupInformation ugi = user.getUGI();
      if (authMethod == AuthMethod.KERBEROS) {
        // Send effective user for Kerberos auth
        out.writeBoolean(true);
        out.writeUTF(ugi.getUserName());
        out.writeBoolean(false);
      } else if (authMethod == AuthMethod.DIGEST) {
        // Don't send user for token auth
        out.writeBoolean(false);
      } else {
        //Send both effective user and real user for simple auth
        out.writeBoolean(true);
        out.writeUTF(ugi.getUserName());
        if (ugi.getRealUser() != null) {
          out.writeBoolean(true);
          out.writeUTF(ugi.getRealUser().getUserName());
        } else {
          out.writeBoolean(false);
        }
      }
    } else {
      out.writeBoolean(false);
    }
  }

  public String getProtocol() {
    return protocol;
  }

  public User getUser() {
    return user;
  }

  public String toString() {
    return protocol + "-" + user;
  }
}
