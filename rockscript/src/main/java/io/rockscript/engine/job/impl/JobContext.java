package io.rockscript.engine.job.impl;

import io.rockscript.Engine;
import io.rockscript.engine.job.JobService;

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