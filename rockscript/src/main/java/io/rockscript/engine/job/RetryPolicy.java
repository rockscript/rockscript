package io.rockscript.engine.job;

import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.LinkedList;

public class RetryPolicy extends LinkedList<TemporalAmount> {

  public static RetryPolicy createDefaultRetryPolicy() {
    RetryPolicy retryPolicy = new RetryPolicy();
    retryPolicy.add(Duration.ofSeconds(5));
    retryPolicy.add(Duration.ofMinutes(10));
    retryPolicy.add(Duration.ofHours(4));
    return retryPolicy;
  }


}
