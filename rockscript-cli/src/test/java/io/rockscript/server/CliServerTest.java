/*
 * Copyright (c) 2017 RockScript.io.
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.rockscript.server;

import io.rockscript.Deploy;
import io.rockscript.End;
import io.rockscript.Ping;
import io.rockscript.Start;
import io.rockscript.test.server.AbstractServerTest;
import org.junit.Before;
import org.junit.Test;

public class CliServerTest extends AbstractServerTest {

  @Test
  public void testClientCommands() {
    new Ping()
      .parseArgs("ping", "-s", "http://localhost:"+PORT, "-q")
      .execute();

    new Deploy()
      .parseArgs("deploy", "-s", "http://localhost:"+PORT, "-r", "-n", ".*resources\\/testscripts.*", "..")
      .execute();

    new Start()
      .parseArgs("start", "-s", "http://localhost:"+PORT, "-n", "short-script.rs")
      .execute();

    new End()
      .parseArgs("end", "-s", "http://localhost:"+PORT, "-seid", "se1", "-eid", "e9")
      .execute();

    new io.rockscript.Test()
      .parseArgs("test", "-s", "http://localhost:"+PORT, "-n", "short-test\\.rst")
      .execute();
  }
}
