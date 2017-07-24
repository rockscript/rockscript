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
package io.rockscript;

import io.rockscript.http.test.AbstractServerTest;
import io.rockscript.netty.router.NettyServer;
import org.apache.http.entity.ContentType;
import org.junit.*;

import static org.junit.Assert.assertEquals;

public class ServerTest extends AbstractServerTest {

  Server server;

  @Before
  public void setUp() {
    server = new TestServer();
    server.startup();
  }

  @After
  public void tearDown() {
    server.shutdown();
  }

  @Test
  public void testServer() {
    String responseBody = POST("scripts")
      .bodyString("var a = 'msg';", ContentType.create("application/rockscript"))
      .execute()
      .assertStatusOk()
      .bodyStringUtf8();

    assertEquals("goodby", responseBody);
  }

  @Override
  public NettyServer getNettyServer() {
    return server.nettyServer;
  }
}
