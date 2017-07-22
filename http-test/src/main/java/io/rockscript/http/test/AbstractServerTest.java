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

import com.google.inject.Injector;
import io.rockscript.http.test.client.TestRequest;
import io.rockscript.netty.router.*;
import org.junit.Before;
import org.junit.Rule;

public abstract class AbstractServerTest {

  /**
   * if you add interceptor {@link ServerExceptionInterceptor}
   * with {@link io.rockscript.netty.router.ServerConfiguration#interceptor(Interceptor)}
   * to your test server, this member field will contain the latest
   * server side exception.  If a test request fails or if it does
   * not get the expected response status, this server exception is
   * used as the cause.
   */
  public static Throwable serverException;

  @Rule
  public TestLogger testLogger = new TestLogger();

  /**
   * lazy initialized during first test {@link #setUp()}
   * and then cached for all subsequent test executions
   */
  protected static Injector cachedServices = null;

  /**
   * service locator for application level objects
   */
  public Injector services;

  /**
   * the http server used during test
   */
  public Server server = null;

  @Before
  public void setUp() {
    this.services = initializeServices();
    this.server = services.getInstance(Server.class);

    serverException = null;
  }

  /**
   * the services used in this test.
   * <p>
   * This default implementation will {@link #cachedServices cache}
   * the services object after it has been
   * {@link #createServices() created} the first time.
   * <p>
   * Individual tests can overwrite this method if they need
   * customized services.
   */
  protected Injector initializeServices() {
    if (cachedServices==null) {
      cachedServices = createServices();
      startServices(cachedServices);
    }
    return cachedServices;
  }

  /**
   * creates the services object and is typically
   * just called once in the setUp of the first test
   * and then cached so that subsequent tests can
   * leverage the same services.
   */
  protected abstract Injector createServices();

  /**
   * only invoked once during lazy initialization of
   * the services.
   *
   * @see #initializeServices()
   */
  protected void startServices(Injector services) {
    services.getInstance(Server.class).startup();
  }

  public String createUri(final String path) {
    return "http://localhost:"+server.getPort()+"/"+path;
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

  public JsonHandler getJson() {
    throwIfNull(services, "Services are not configured");
    JsonHandler jsonHandler = server.getJsonHandler();
    throwIfNull(jsonHandler, "Services doesn't contain Json");
    return jsonHandler;
  }

  protected void throwIfNull(Object o, String message, String... messageArgs) {
    if (o==null) {
      throw new RuntimeException(String.format(message, messageArgs));
    }
  }

  public String getMethodName() {
    return testLogger.getMethodName();
  }
}
