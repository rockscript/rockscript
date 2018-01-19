/*
 * Copyright ©2017, RockScript.io. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.rockscript.service;

import io.rockscript.engine.impl.JsonObject;

import java.util.function.Function;

/** Special JsonObject used as import object that ensures automatic
 * capturing of the propertyName of ServiceFunction values and wraps the
 * activities so that the toString shows the property name. */
public class ImportObject extends JsonObject implements ImportProvider {

  protected String serviceName;

  public ImportObject(String serviceName) {
    this.serviceName = serviceName;
  }

  public ImportObject put(ServiceFunction serviceFunction) {
    put(serviceFunction.getFunctionName(), serviceFunction);
    if (serviceFunction instanceof AbstractServiceFunction) {
      ((AbstractServiceFunction) serviceFunction).setServiceName(serviceName);
    }
    return this;
  }


  public ImportObject put(String propertyName, Function<ServiceFunctionInput, ServiceFunctionOutput> function, final String... argNames) {
    put(new FunctionServiceFunction(propertyName, function, argNames));
    return this;
  }

  public String getServiceName() {
    return serviceName;
  }

  @Override
  public String toString() {
    return serviceName;
  }

  @Override
  public ImportObject getImportObject() {
    return this;
  }
}
