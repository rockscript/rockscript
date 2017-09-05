/*
 * Copyright ©2017, RockScript.io. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.rockscript.test;

import io.rockscript.netty.router.Interceptor;
import io.rockscript.netty.router.InterceptorContext;

import static io.rockscript.test.AbstractServerTest.serverException;

public class ServerExceptionInterceptor implements Interceptor {

  @Override
  public void intercept(InterceptorContext interceptorContext) {
    try {
      interceptorContext.next();
    } catch (Throwable e) {
      serverException = e;
      if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      } else {
        throw new RuntimeException("Exception in request handler: "+e.getMessage(), e);
      }
    }
  }
}
