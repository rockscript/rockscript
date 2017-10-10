package io.rockscript.engine.job.impl;

import java.time.Instant;

public class JobRun {

  protected Instant start;
  protected Instant end;
  protected String error;

  public Instant getStart() {
    return start;
  }

  public void setStart(Instant start) {
    this.start = start;
  }

  public Instant getEnd() {
    return end;
  }

  public void setEnd(Instant end) {
    this.end = end;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public boolean hasError() {
    return error!=null;
  }

  public void endOk() {
    this.end = Instant.now();
  }

  public void endError(String error) {
    this.end = Instant.now();
    this.error = error;
  }

}
