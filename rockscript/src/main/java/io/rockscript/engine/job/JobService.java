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
import io.rockscript.engine.impl.IdGenerator;
import io.rockscript.engine.impl.Time;
import io.rockscript.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class JobService {

  static Logger log = LoggerFactory.getLogger(JobService.class);

  protected Engine engine;
  protected Timer timer;
  protected List<Job> jobs = new ArrayList<>();
  protected IdGenerator idGenerator;
  /** jobs that are stuck */
  protected List<Job> errorJobs = new ArrayList<>();
  protected RetryPolicy defaultJobRetryPolicy; // TODO initialize this

  public JobService(Engine engine) {
    this.engine = engine;
    idGenerator = engine.getJobIdGenerator();
  }

  public void startup() {
    timer = new Timer();
  }

  public void shutdown() {
    timer.cancel();
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
    Job job = new Job(idGenerator.createId(), jobHandler, executionTime, retryPolicy);
    schedule(job);
    jobs.add(job);
    return job;
  }

  protected void schedule(Job job) {
    long millisFromNow = Duration.between(Time.now(), job.getExecutionTime()).toMillis();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        executeJob(job);
      }
    }, millisFromNow);
  }

  protected void executeJob(Job job) {
    JobHandler jobHandler = job.getJobHandler();
    JobRun jobRun = new JobRun();
    job.addJobRun(jobRun);
    try {
      log.debug("Job handler "+jobHandler.getClass().getSimpleName()+" execution starts");
      jobHandler.execute(engine);
      log.debug("Job handler "+jobHandler.getClass().getSimpleName()+" execution ended ok");
      jobRun.endOk();
    } catch (Exception e) {
      String error = Exceptions.getStackTraceString(e);
      handleError(job, jobRun, error);
    }
  }

  public void handleError(Job job, JobRun jobRun, String error) {
    log.debug("Job handler "+job.getJobHandler().getClass().getSimpleName()+" error occurred: "+error);
    jobRun.endError(error);
    long errorCount = job.getErrorCount();
    TemporalAmount retryDuration = job.getNextRetryDuration();
    if (retryDuration!=null) {
      Instant nextRetryTime = Time.now().plus(retryDuration);
      job.setExecutionTime(nextRetryTime);
      schedule(job);

    } else {
      jobs.remove(job);
      errorJobs.add(job);
    }
  }

  public RetryPolicy getDefaultJobRetryPolicy() {
    return defaultJobRetryPolicy;
  }
}
