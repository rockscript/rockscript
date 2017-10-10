package io.rockscript.engine.job.impl;

import io.rockscript.engine.job.RetryPolicy;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;

/** JobService data structure to maintain jobs and when
 * they should be executed. */
public class Job {

  String id;
  JobHandler jobHandler;
  Instant executionTime;
  RetryPolicy retryPolicy;
  List<JobRun> jobRuns;

  public Job() {
  }

  public Job(String id, JobHandler jobHandler, Instant executionTime, RetryPolicy retryPolicy) {
    this.id = id;
    this.executionTime = executionTime;
    this.jobHandler = jobHandler;
    this.retryPolicy = retryPolicy;
  }

  public Instant getExecutionTime() {
    return executionTime;
  }

  public void setExecutionTime(Instant executionTime) {
    this.executionTime = executionTime;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public JobHandler getJobHandler() {
    return jobHandler;
  }

  public List<JobRun> getJobRuns() {
    return jobRuns;
  }

  public void setJobRuns(List<JobRun> jobRuns) {
    this.jobRuns = jobRuns;
  }

  public long getErrorCount() {
    return jobRuns!=null ? jobRuns.stream().filter(j->j.hasError()).count() : 0;
  }

  public void addJobRun(JobRun jobRun) {
    if (jobRuns==null) {
      jobRuns = new ArrayList<>();
    }
    jobRuns.add(jobRun);
  }

  public TemporalAmount getNextRetryDuration() {
    return retryPolicy!=null && !retryPolicy.isEmpty()? retryPolicy.removeFirst() : null;
  }
}
