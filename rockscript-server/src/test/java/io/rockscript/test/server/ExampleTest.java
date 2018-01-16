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
package io.rockscript.test.server;

import io.rockscript.Configuration;
import io.rockscript.Engine;
import io.rockscript.test.engine.TestEngineProvider;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ExampleTest extends AbstractServerTest {

  protected static Logger log = LoggerFactory.getLogger(ExampleTest.class);

  @Override
  protected TestEngineProvider getEngineProvider() {
    return new TestEngineProvider() {
      @Override
      public Engine createEngine() {
        return new Configuration()
          .configureTest()
          .configureExamples()
          .build()
          .start();
      }
    };
  }

  @Test
  public void testApproval() throws Exception {
    newGet("/examples/lucky")
      .execute()
      .assertStatusInternalServerException();
    newGet("/examples/lucky")
      .execute()
      .assertStatusOk();
  }
}
