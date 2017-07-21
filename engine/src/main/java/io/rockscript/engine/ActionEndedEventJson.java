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
package io.rockscript.engine;

public class ActionEndedEventJson extends RecoverableEventJson<ActionEndedEvent> {

  // TODO Is there a better way to access this from HttpActionWorkQueueTest than public or getter access?
  public Object result;

  public ActionEndedEventJson(ActionEndedEvent externalFunctionEndedEvent) {
    super(externalFunctionEndedEvent);
    result = externalFunctionEndedEvent.result;
  }

  @Override
  public ActionEndedEvent toEvent(Execution execution) {
    // TODO deserialize the result:
    //   Map --> JsonObject
    //   List --> JsonArray
    // and possibly other deserialization stuff
    return new ActionEndedEvent((ArgumentsExpressionExecution) execution, result);
  }
}
