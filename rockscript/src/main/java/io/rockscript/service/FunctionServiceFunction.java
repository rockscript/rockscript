package io.rockscript.service;

import java.util.function.Function;

public class FunctionServiceFunction extends AbstractServiceFunction {

  Function<ServiceFunctionInput, ServiceFunctionOutput> function;

  public FunctionServiceFunction(String functionName, Function<ServiceFunctionInput, ServiceFunctionOutput> function, String... argNames) {
    super(functionName, argNames);
    this.function = function;
  }

  @Override
  public ServiceFunctionOutput invoke(ServiceFunctionInput input) {
    return function.apply(input);
  }
}
