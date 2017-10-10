package io.rockscript.engine.job.impl;

import io.rockscript.engine.Configuration;
import io.rockscript.engine.job.JobService;

import java.time.Instant;
import java.time.temporal.TemporalAmount;

public class JobContext extends Configuration {

  Configuration configuration;
  Job job;
  JobRun jobRun;
  JobService jobService;

  public JobContext(Configuration configuration, Job job, JobRun jobRun, JobService jobService) {
    this.configuration = configuration;
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