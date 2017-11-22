package io.rockscript.engine.job;

import io.rockscript.Engine;
import io.rockscript.engine.impl.IdGenerator;
import io.rockscript.engine.job.impl.Job;
import io.rockscript.engine.job.impl.JobContext;
import io.rockscript.engine.job.impl.JobHandler;
import io.rockscript.engine.job.impl.JobRun;
import io.rockscript.util.Exceptions;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class JobService {

  Engine engine;
  Timer timer;
  List<Job> jobs = new ArrayList<>();
  IdGenerator idGenerator;
  /** jobs that are stuck */
  List<Job> errorJobs = new ArrayList<>();

  public JobService(Engine engine) {
    this.engine = engine;
    timer = new Timer();
    idGenerator = engine.getJobIdGenerator();
  }

  public void startup() {
  }

  public void shutdown() {
    timer.cancel();
  }

//  /** The job handler is immediately executed like a command in a queue */
//  public Job schedule(JobHandler jobHandler) {
//  }
//
//  /** The job handler executed at the specified event, if an exception occurs,
//   * the job moves to the errorJobs. */
//  public Job schedule(JobHandler jobHandler, Instant executionTime) {
//  }
//
  /** The job handler executed at the specified event, if an exception occurs,
   * the job is first retried according to the retry policy and if that doesn't
   * help, the job is put in the errorJobs. */
  public Job schedule(JobHandler jobHandler, Instant executionTime, RetryPolicy retryPolicy) {
    Job job = new Job(idGenerator.createId(), jobHandler, executionTime, retryPolicy);
    schedule(job);
    jobs.add(job);
    return job;
  }

  public void schedule(Job job) {
    long millisFromNow = Duration.between(Instant.now(), job.getExecutionTime()).toMillis();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        executeJob(job);
      }
    }, millisFromNow);
  }

  private void executeJob(Job job) {
    JobHandler jobHandler = job.getJobHandler();
    JobRun jobRun = new JobRun();
    job.addJobRun(jobRun);
    try {
      jobHandler.execute(new JobContext(engine, job, jobRun, this));
      jobRun.endOk();
    } catch (Exception e) {
      String error = Exceptions.getStackTraceString(e);
      handleError(job, jobRun, error);
    }
  }

  public void handleError(Job job, JobRun jobRun, String error) {
    jobRun.endError(error);
    long errorCount = job.getErrorCount();
    TemporalAmount retryDuration = job.getNextRetryDuration();
    if (retryDuration!=null) {
      Instant nextRetryTime = Instant.now().plus(retryDuration);
      job.setExecutionTime(nextRetryTime);
      schedule(job);

    } else {
      jobs.remove(job);
      errorJobs.add(job);
    }
  }
}
