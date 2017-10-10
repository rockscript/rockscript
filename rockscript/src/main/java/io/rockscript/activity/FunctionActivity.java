package io.rockscript.activity;

import java.util.function.Function;

public class FunctionActivity extends AbstractActivity {

  Function<ActivityInput, ActivityOutput> function;

  public FunctionActivity(String activityName, Function<ActivityInput, ActivityOutput> function, String... argNames) {
    super(activityName, argNames);
    this.function = function;
  }

  @Override
  public ActivityOutput invoke(ActivityInput input) {
    return function.apply(input);
  }
}
