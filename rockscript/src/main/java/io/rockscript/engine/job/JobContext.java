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
package io.rockscript.engine.job;

import io.rockscript.Engine;

import java.time.Instant;
import java.time.temporal.TemporalAmount;

public class JobContext extends Engine {

  Engine engine;
  Job job;
  JobRun jobRun;
  JobService jobService;

  public JobContext(Engine engine, Job job, JobRun jobRun, JobService jobService) {
    this.engine = engine;
    this.job = job;
    this.jobRun = jobRun;
    this.jobService = jobService;
  }

  public void handleError(String error) {
    jobService.handleError(job, jobRun, error);
  }

  public void reschedule(TemporalAmount retryDuration) {
    job.setExecutionTime(Instant.now().plus(retryDuration));
    jobService.schedule(job);
  }

}