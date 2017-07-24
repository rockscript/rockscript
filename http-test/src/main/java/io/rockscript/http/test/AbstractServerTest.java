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
package io.rockscript.http.test;

import io.rockscript.http.test.client.TestRequest;
import io.rockscript.netty.router.*;
import org.junit.Before;
import org.junit.Rule;

public abstract class AbstractServerTest {

  /**
   * if you add interceptor {@link ServerExceptionInterceptor}
   * with {@link AsyncHttpServerConfiguration#interceptor(Interceptor)}
   * to your test asyncHttpServer, this member field will contain the latest
   * asyncHttpServer side exception.  If a test request fails or if it does
   * not get the expected response status, this asyncHttpServer exception is
   * used as the cause.
   */
  public static Throwable serverException;

  public abstract AsyncHttpServer getNettyServer();

  @Rule
  public TestLogger testLogger = new TestLogger();

  @Before
  public void setUp() {
    serverException = null;
  }

  public String createUri(final String path) {
    return "http://localhost:"+getNettyServer().getPort()+"/"+path;
  }

  public TestRequest GET(final String path) {
    return configure(TestRequest.Get(createUri(path)));
  }

  public TestRequest POST(final String path) {
    return configure(TestRequest.Post(createUri(path)));
  }

  public TestRequest PUT(final String path) {
    return configure(TestRequest.Put(createUri(path)));
  }

  public TestRequest DELETE(final String path) {
    return configure(TestRequest.Delete(createUri(path)));
  }

  /**
   * configurations applied to all requests
   */
  private TestRequest configure(TestRequest testRequest) {
    // request.socketTimeout(2000);  // default is -1
    // request.connectTimeout(2000); // default is -1
    testRequest.setTest(this);
    return testRequest;
  }

  protected void throwIfNull(Object o, String message, String... messageArgs) {
    if (o==null) {
      throw new RuntimeException(String.format(message, (String[])messageArgs));
    }
  }

  public String getMethodName() {
    return testLogger.getMethodName();
  }
}
