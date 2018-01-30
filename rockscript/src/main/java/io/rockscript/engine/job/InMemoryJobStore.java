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
import io.rockscript.api.events.*;
import io.rockscript.util.Lists;

import java.time.Duration;
import java.util.*;

public class InMemoryJobStore implements JobStore {

  private static final int MAX_HISTORY = 10;

  protected Engine engine;
  protected Map<String, Job> jobs;
  protected Set<Job> jobsSortedByExecutionTime;
  /** latest MAX_HISTORY jobs that are executed ok */
  protected List<Job> history;
  /** jobs that are stuck */
  protected List<Job> deadJobs;

  public InMemoryJobStore(Engine engine) {
    this.engine = engine;
    reset();
  }

  public void reset() {
    this.jobs = new HashMap<>();
    this.jobsSortedByExecutionTime = new TreeSet<>((o1, o2) -> (int) Duration.between(o2.getExecutionTime(), o1.getExecutionTime()).toMillis());
    this.history = new ArrayList<>();
    this.deadJobs = new ArrayList<>();
  }

  @Override
  public void handle(JobEvent jobEvent) {
    if (jobEvent instanceof JobScheduledEvent) {
      JobScheduledEvent jobScheduledEvent = (JobScheduledEvent) jobEvent;
      Job job = jobScheduledEvent.getJob();
      jobs.put(job.getId(), job);
      jobsSortedByExecutionTime.add(job);

    } else if (jobEvent instanceof JobStartedEvent) {
      JobStartedEvent jobStartedEvent = (JobStartedEvent) jobEvent;
      String jobId = jobStartedEvent.getJobId();
      Job job = findJobById(jobId);
      JobRun jobRun = new JobRun();
      jobRun.setStart(jobStartedEvent.getJobStart());
      job.addJobRun(jobRun);

    } else if (jobEvent instanceof JobEndedEvent) {
      JobEndedEvent jobEndedEvent = (JobEndedEvent) jobEvent;
      Job job = findJobById(jobEndedEvent.getJobId());
      jobsSortedByExecutionTime.remove(job);
      history.add(0, job);
      if (history.size()>MAX_HISTORY) {
        Lists.removeLast(history);
      }
      JobRun lastJobRun = Lists.getLast(job.getJobRuns());
      lastJobRun.setEnd(jobEndedEvent.getJobEnd());

    } else if (jobEvent instanceof JobFailedEvent) {
      JobFailedEvent jobFailedEvent = (JobFailedEvent) jobEvent;
      Job job = findJobById(jobFailedEvent.getJobId());
      jobsSortedByExecutionTime.remove(job);
      JobRun lastJobRun = Lists.getLast(job.getJobRuns());
      if (jobFailedEvent.getNextRetryTime()!=null) {
        jobsSortedByExecutionTime.add(job);

      } else {
        jobs.remove(job.getId());
        deadJobs.add(job);
      }
    }
  }

  @Override
  public Job findJobById(String jobId) {
    return jobs.get(jobId);
  }

  /** returns the first job to be executed or null if there are no jobs scheduled */
  @Override
  public Job findNextJob() {
    return jobsSortedByExecutionTime
      .stream()
      .findFirst()
      .orElse(null);
  }

  public int getJobCount() {
    return jobs.size();
  }
}
