/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teragrep.zep_01.rest;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.WebApplicationException;

import com.teragrep.zep_01.service.AuthenticationService;
import com.teragrep.zep_01.service.ServiceContext;
import com.teragrep.zep_01.service.SimpleServiceCallback;
import com.teragrep.zep_01.user.AuthenticationInfo;

public class AbstractRestApi {

  protected AuthenticationService authenticationService;

  protected AbstractRestApi(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  protected ServiceContext getServiceContext() {
    AuthenticationInfo authInfo = new AuthenticationInfo(authenticationService.getPrincipal());
    Set<String> userAndRoles = new HashSet<>();
    userAndRoles.add(authenticationService.getPrincipal());
    userAndRoles.addAll(authenticationService.getAssociatedRoles());
    return new ServiceContext(authInfo, userAndRoles);
  }

  public static class RestServiceCallback<T> extends SimpleServiceCallback<T> {

    @Override
    public void onFailure(Exception ex, ServiceContext context) throws IOException {
      super.onFailure(ex, context);
      if (ex instanceof WebApplicationException) {
        throw (WebApplicationException) ex;
      } else {
        throw new IOException(ex);
      }
    }
  }
}
