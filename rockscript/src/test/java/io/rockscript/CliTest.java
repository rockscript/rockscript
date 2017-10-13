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

import io.rockscript.test.AbstractServerTest;
import io.rockscript.test.SimpleImportProvider;
import org.junit.Before;
import org.junit.Test;

public class CliTest extends AbstractServerTest {

  @Before
  @Override
  public void setUp() {
    super.setUp();
    SimpleImportProvider.setUp();
  }

  @Test
  public void testClientCommands() {
    new Ping()
      .parseArgs("ping", "-s", "http://localhost:"+server.getPort(), "-q")
      .execute();

    new Deploy()
      .parseArgs("deploy", "-s", "http://localhost:"+server.getPort(), "-r", "-n", ".*resources\\/testscripts.*", "..")
      .execute();

    new Start()
      .parseArgs("start", "-s", "http://localhost:"+server.getPort(), "-n", "short-script.rs")
      .execute();

    new End()
      .parseArgs("end", "-s", "http://localhost:"+server.getPort(), "-seid", "se1", "-eid", "e9")
      .execute();

    new io.rockscript.Test()
      .parseArgs("test", "-s", "http://localhost:"+server.getPort(), "-n", "short-test\\.rst")
      .execute();


    //    new Events()
//      .parseArgs("end", "-s", "http://localhost:3333")
//      .execute();
  }
}
