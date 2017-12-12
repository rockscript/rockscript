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
package io.rockscript.test.engine.http;

import io.rockscript.api.commands.StartScriptExecutionCommand;
import io.rockscript.api.model.ScriptVersion;
import io.rockscript.engine.impl.EngineScriptExecution;
import io.rockscript.engine.impl.ScriptExecutionErrorEvent;
import io.rockscript.engine.impl.Time;
import io.rockscript.engine.job.Job;
import io.rockscript.engine.job.RetryServiceFunctionJobHandler;
import io.rockscript.http.servlet.PathRequestHandler;
import io.rockscript.http.servlet.RouterServlet;
import io.rockscript.http.servlet.ServerRequest;
import io.rockscript.http.servlet.ServerResponse;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static io.rockscript.http.servlet.PathRequestHandler.GET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HttpServiceTest extends AbstractHttpTest {

  protected static Logger log = LoggerFactory.getLogger(HttpServiceTest.class);

  public static interface TestRequestHandler {
    void handle(ServerRequest request, ServerResponse response);
  }

  /** to be initialized by the tests */
  TestRequestHandler testRequestHandler = null;

  @Override
  protected void configure(RouterServlet serviceServlet) {
    // The routerServlet is configured at the beginning of each test
    serviceServlet
      .requestHandler(new PathRequestHandler(GET, "/") {
        @Override
        public void handle(ServerRequest request, ServerResponse response) {
          testRequestHandler.handle(request, response);
        }
      });
  }

  @Test
  public void testHttpGet() {
    testRequestHandler = new TestRequestHandler() {
      public void handle(ServerRequest request, ServerResponse response) {
        assertEquals("singlevalue", request.getHeader("Single"));
        response.headerContentTypeTextPlain();
        response.status(200);
        response.bodyString("thread was here");
      }
    };

    ScriptVersion scriptVersion = deployScript(
      "var http = system.import('rockscript.io/http'); \n" +
      "var response = http.get({" +
      "  url: 'http://localhost:" + SERVICE_PORT + "?p1=v1&p2=v2'," +
      "  headers: {" +
      "    Single: 'singlevalue'" +
//      "    Double: ['double', 'value']" +
      "  }" +
      "}); ");

    EngineScriptExecution engineScriptExecution = new StartScriptExecutionCommand()
      .scriptVersionId(scriptVersion.getId())
      .execute(engine)
      .getEngineScriptExecution();

    Map<String,Object> response = (Map<String, Object>) engineScriptExecution.getVariable("response").getValue();
    assertEquals("thread was here", response.get("body"));

    Map<String,List<String>> headers = (Map<String, List<String>>) response.get("headers");
    assertEquals("text/plain", ((List)headers.get("Content-Type")).get(0));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testHttpGetError() {
    setNow(Time.now()); // fix time
    int wrongPort = 7236;
    ScriptVersion scriptVersion = deployScript(
      "var http = system.import('rockscript.io/http'); \n" +
      "var response = http.get({" +
      "  url: 'http://localhost:" + wrongPort+ "'," +
      "}); ");

    EngineScriptExecution engineScriptExecution = new StartScriptExecutionCommand()
      .scriptVersionId(scriptVersion.getId())
      .execute(engine)
      .getEngineScriptExecution();

    ScriptExecutionErrorEvent errorEvent = engineScriptExecution.getErrorEvent();

    List<Job> jobs = engine.getJobService().getjobs();
    Job job = jobs.get(0);
    RetryServiceFunctionJobHandler retryServiceFunctionJobHandler = (RetryServiceFunctionJobHandler) job.getJobHandler();
    assertEquals(engineScriptExecution.getId(), retryServiceFunctionJobHandler.getContinuationReference().getScriptExecutionId());
    assertNotNull(retryServiceFunctionJobHandler.getContinuationReference().getExecutionId());
    assertEquals(Time.now().plusSeconds(5), job.getExecutionTime());
    assertEquals(1, jobs.size());

    engine.getJobService().executeJob(job);

    assertEquals(1, job.getJobRuns().size());
    assertEquals(2, jobs.size());
  }
}
