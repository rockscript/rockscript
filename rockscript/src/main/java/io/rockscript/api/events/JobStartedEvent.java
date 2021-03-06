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
package io.rockscript.api.events;

import io.rockscript.engine.job.Job;

import java.time.Instant;

public class JobStartedEvent extends JobEvent {

  String jobId;
  Instant jobStart;

  JobStartedEvent() {
  }

  public JobStartedEvent(String jobId, Instant jobStart) {
    this.jobId = jobId;
    this.jobStart = jobStart;
  }

  public String getJobId() {
    return jobId;
  }

  public Instant getJobStart() {
    return jobStart;
  }
}
