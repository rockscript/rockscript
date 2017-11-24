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
package io.rockscript.test.engine;

import io.rockscript.activity.ActivityInput;
import io.rockscript.api.model.ScriptExecution;
import io.rockscript.api.model.ScriptVersion;
import io.rockscript.http.servlet.PathRequestHandler;
import io.rockscript.http.servlet.RouterServlet;
import io.rockscript.http.servlet.ServerRequest;
import io.rockscript.http.servlet.ServerResponse;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static io.rockscript.http.servlet.PathRequestHandler.POST;
import static org.junit.Assert.assertTrue;

public class AsynchronousActivityHttpTest extends AbstractHttpTest {

  protected static Logger log = LoggerFactory.getLogger(AsynchronousActivityHttpTest.class);

  List<ActivityInput> activityInputs = new ArrayList<>();

  @Override
  protected void configure(RouterServlet routerServlet) {
    // The routerServlet is configured at the beginning of each test
  }

  @Test
  public void testHttpActivityNoBodyResponse() {
    routerServlet
      .requestHandler(new PathRequestHandler(POST, "/approve") {
        @Override
        public void handle(ServerRequest request, ServerResponse response) {
          ActivityInput activityInput = parseBodyAs(request, ActivityInput.class);
          activityInputs.add(activityInput);
          response.status(200);
        }
      });
    executeApprovalScript();
  }

  @Test
  public void testHttpActivityFullBodyResponse() {
    routerServlet
      .requestHandler(new PathRequestHandler(POST, "/approve") {
        @Override
        public void handle(ServerRequest request, ServerResponse response) {
          ActivityInput activityInput = parseBodyAs(request, ActivityInput.class);
          activityInputs.add(activityInput);
          response
            .status(200)
            .headerContentTypeApplicationJson()
            .bodyString("{ \"ended\": \"false\" }");
        }
      });
    executeApprovalScript();
  }

  @Test
  public void testHttpActivityEmptyBodyResponse() {
    routerServlet
      .requestHandler(new PathRequestHandler(POST, "/approve") {
        @Override
        public void handle(ServerRequest request, ServerResponse response) {
          ActivityInput activityInput = parseBodyAs(request, ActivityInput.class);
          activityInputs.add(activityInput);
          response
            .status(200)
            .headerContentTypeApplicationJson()
            .bodyString("{}");
        }
      });
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
