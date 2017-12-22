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
import io.rockscript.EngineListener;
import io.rockscript.api.events.JobScheduledEvent;
import io.rockscript.api.events.JobEvent;
import io.rockscript.api.events.JobFailedEvent;
import io.rockscript.engine.impl.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

public class InMemoryJobExecutor extends AbstractJobExecutor implements EngineListener {

  static Logger log = LoggerFactory.getLogger(InMemoryJobExecutor.class);

  protected Timer timer;
  protected RetryPolicy defaultJobRetryPolicy; // TODO initialize this

  public InMemoryJobExecutor(Engine engine) {
    super(engine);
  }

  @Override
  public void engineStarts(Engine engine) {
    timer = new Timer();
  }

  @Override
  public void engineStops(Engine engine) {
    timer.cancel();
  }

  public void handle(JobEvent jobEvent) {
    if (jobEvent instanceof JobScheduledEvent) {
      JobScheduledEvent jobScheduledEvent = (JobScheduledEvent) jobEvent;
      scheduleJob(jobScheduledEvent.getJob());

    } else if (jobEvent instanceof JobFailedEvent) {
      JobFailedEvent jobFailedEvent = (JobFailedEvent) jobEvent;
      if (jobFailedEvent.getNextRetryTime()!=null) {
        Job job = engine
          .getJobStore()
          .findJobById(jobFailedEvent.getJobId());
        scheduleJob(job);
      }
    }
  }

  public void scheduleJob(Job job) {
    long millisFromNow = Duration.between(Time.now(), job.getExecutionTime()).toMillis();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        engine
          .getJobService()
          .executeJob(job);
      }
    }, millisFromNow);
  }
}
