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
package io.rockscript.http.client;

import io.rockscript.activity.ActivityInput;
import io.rockscript.api.model.ScriptVersion;
import io.rockscript.api.model.ScriptExecution;
import io.rockscript.test.HttpTest;
import io.rockscript.test.HttpTestServer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class AsynchronousActivityHttpTest extends HttpTest {

  protected static Logger log = LoggerFactory.getLogger(AsynchronousActivityHttpTest.class);

  List<ActivityInput> activityInputs = new ArrayList<>();

  @Override
  protected void configure(HttpTestServer httpTestServer) {
    // The HttpTestServer is configured at the beginning of each test
  }

  @Test
  public void testHttpActivityNoBodyResponse() {
    httpTestServer
        .post("/approve", (request,response)-> {
          ActivityInput activityInput = gson.fromJson(request.body(), ActivityInput.class);
          activityInputs.add(activityInput);
          response
              .status(200)
              .send(); });

    executeApprovalScript();
  }

  @Test
  public void testHttpActivityFullBodyResponse() {
    httpTestServer
        .post("/approve", (request,response)-> {
          ActivityInput activityInput = gson.fromJson(request.body(), ActivityInput.class);
          activityInputs.add(activityInput);
          response
              .status(200)
              .headerContentTypeApplicationJson()
              .body("{ \"ended\": \"false\" }")
              .send(); });

    executeApprovalScript();
  }

  @Test
  public void testHttpActivityEmptyBodyResponse() {
    httpTestServer
        .post("/approve", (request,response)-> {
          ActivityInput activityInput = gson.fromJson(request.body(), ActivityInput.class);
          activityInputs.add(activityInput);
          response
              .status(200)
              .headerContentTypeApplicationJson()
              .body("{}")
              .send(); });

    executeApprovalScript();
  }

  private void executeApprovalScript() {
    ScriptVersion scriptVersion = deployScript(
        "var approvals = system.import('localhost:"+PORT+"'); \n" +
        "approvals.approve('oo',7); ");

    startScriptExecution(scriptVersion);

    ActivityInput activityInput = activityInputs.get(0);
    ScriptExecution scriptExecution = endActivity(activityInput.getContinuationReference());
    assertTrue(scriptExecution.isEnded());
  }
}
