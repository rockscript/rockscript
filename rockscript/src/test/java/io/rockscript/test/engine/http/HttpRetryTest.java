/*
 * Copyright (c) 2018 RockScript.io.
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
import io.rockscript.api.events.ExecutionEvent;
import io.rockscript.api.model.ScriptVersion;
import io.rockscript.engine.impl.EngineScriptExecution;
import io.rockscript.engine.impl.Time;
import io.rockscript.engine.job.InMemoryJobStore;
import io.rockscript.engine.job.Job;
import io.rockscript.engine.job.JobService;
import io.rockscript.engine.job.RetryServiceFunctionJobHandler;
import io.rockscript.examples.ExamplesHandler;
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
import static org.junit.Assert.*;

public class HttpRetryTest extends AbstractHttpTest {

  protected static Logger log = LoggerFactory.getLogger(HttpRetryTest.class);

  @Override
  protected void configure(RouterServlet serviceServlet) {
    serviceServlet.requestHandler(new ExamplesHandler(engine));
  }

  @Test
  public void testHttpGetFailOnce() {
    ScriptVersion scriptVersion = deployScript(
      "var http = system.import('rockscript.io/http'); \n" +
      "var response = http.get({ \n" +
      "  url:'http://localhost:" + SERVICE_PORT + "/examples/lucky', \n" +
      "  expectedStatus: 200 \n" +
      "}); ");

    EngineScriptExecution scriptExecution = new StartScriptExecutionCommand()
      .scriptVersionId(scriptVersion.getId())
      .execute(engine)
      .getEngineScriptExecution();

    // The first time the examples lucky request always fails

    // Assert there is a job to retry this failed
    Job nextJob = engine.getJobStore().findNextJob();
    assertNotNull(nextJob);
    // Execute the job which will retry the http invocation
    // The second time the examples lucky request will succeed
    engine
      .getJobService()
      .executeJob(nextJob);

    // Assert that the script is ended
    scriptExecution = engine.getScriptExecutionStore()
      .findScriptExecutionById(scriptExecution.getId());
    assertTrue(scriptExecution.isEnded());

    // Assert that there are no more jobs to be executed
    assertNull(engine.getJobStore().findNextJob());
  }

  @Test
  public void testHttpKeepFailing() {
    ScriptVersion scriptVersion = deployScript(
      "var http = system.import('rockscript.io/http'); \n" +
      "var response = http.get({ \n" +
      "  url:'http://unexistinghost/'" +
      "}); ");

    EngineScriptExecution scriptExecution = new StartScriptExecutionCommand()
      .scriptVersionId(scriptVersion.getId())
      .execute(engine)
      .getEngineScriptExecution();

    JobService jobService = engine.getJobService();

    // First 3 times a retry job is scheduled
    Job nextJob = engine.getJobStore().findNextJob();
    assertNotNull(nextJob);
    jobService.executeJob(nextJob);

    nextJob = engine.getJobStore().findNextJob();
    assertNotNull(nextJob);
    jobService.executeJob(nextJob);

    nextJob = engine.getJobStore().findNextJob();
    assertNotNull(nextJob);
    jobService.executeJob(nextJob);

    // Forth time, the retry is given up
    assertNull(engine.getJobStore().findNextJob());

    scriptExecution = engine.getScriptExecutionStore()
      .findScriptExecutionById(scriptExecution.getId());
    assertFalse(scriptExecution.isEnded());
  }

}
