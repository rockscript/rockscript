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
import io.rockscript.api.events.JobEndedEvent;
import io.rockscript.api.events.JobFailedEvent;
import io.rockscript.api.events.JobScheduledEvent;
import io.rockscript.api.events.JobStartedEvent;
import io.rockscript.engine.impl.Time;
import io.rockscript.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.TemporalAmount;

/** Used by the engine to schedule and manipulate jobs.
 * The JobService will dispatch events which will be picked
 * up by the JobStore and the JobExecutor. JobStore and the JobExecutor
 * take care of execution of jobs */
public class JobService {

  static Logger log = LoggerFactory.getLogger(JobService.class);

  protected Engine engine;
  RetryPolicy defaultJobRetryPolicy; // TODO initialize

  public JobService(Engine engine) {
    this.engine = engine;
  }

  /**
   * The job handler executed at the specified event, if an exception occurs,
   * the job is first retried according to the retry policy and if that doesn't
   * help, the job is put in the errorJobs.
   */
  public Job schedule(JobHandler jobHandler, Instant executionTime) {
    return schedule(jobHandler, executionTime, getDefaultJobRetryPolicy());
  }

  /** The job handler executed at the specified event, if an exception occurs,
   * the job is first retried according to the retry policy and if that doesn't
   * help, the job is put in the errorJobs. */
  public Job schedule(JobHandler jobHandler, Instant executionTime, RetryPolicy retryPolicy) {
    log.debug("Scheduling "+jobHandler.getClass().getSimpleName()+" for "+executionTime);
    String jobId = engine
      .getJobIdGenerator()
      .createId();
    Job job = new Job(jobId, jobHandler, executionTime, retryPolicy);
    engine
      .getEventDispatcher()
      .dispatch(new JobScheduledEvent(job));
    return job;
  }

  public void executeJob(Job job) {
    JobHandler jobHandler = job.getJobHandler();
    try {
      dispatchJobStartedEvent(job);
      jobHandler.execute(engine);
      log.debug("Job handler "+jobHandler.getClass().getSimpleName()+" execution ended ok");
      dispatchJobEndedEvent(job);
    } catch (Exception e) {
      String error = Exceptions.getStackTraceString(e);
      handleJobFailure(job, error);
    }
  }

  protected void dispatchJobStartedEvent(Job job) {
    try {
      engine
        .getEventDispatcher()
        .dispatch(new JobStartedEvent(job.getId(), Time.now()));
    } catch (Exception e) {
      log.debug("Job event dispatching failed: "+e.getMessage(), e);
    }
  }

  protected void dispatchJobEndedEvent(Job job) {
    try {
      engine
        .getEventDispatcher()
        .dispatch(new JobEndedEvent(job.getId(), Time.now()));
    } catch (Exception e) {
      log.debug("Job event dispatching failed: "+e.getMessage(), e);
    }
  }

  public void handleJobFailure(Job job, String error) {
    log.debug("Job handler "+job.getJobHandler().getClass().getSimpleName()+" error occurred: "+error);
    long errorCount = job.getErrorCount();
    TemporalAmount retryDuration = job.getNextRetryDuration();
    if (retryDuration!=null) {
      Instant nextRetryTime = Time.now().plus(retryDuration);
      job.setExecutionTime(nextRetryTime);
      engine.getEventDispatcher().dispatch(new JobFailedEvent(job.getId(), error, nextRetryTime));

    } else {
      engine.getEventDispatcher().dispatch(new JobFailedEvent(job.getId(), error));
    }
  }

  public RetryPolicy getDefaultJobRetryPolicy() {
    return defaultJobRetryPolicy;
  }
}
