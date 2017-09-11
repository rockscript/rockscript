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
package io.rockscript.engine.impl;


import com.google.gson.reflect.TypeToken;
import io.rockscript.gson.PolymorphicTypeAdapterFactory;

public interface Event {

  static PolymorphicTypeAdapterFactory createEventJsonTypeAdapterFactory() {
    return new PolymorphicTypeAdapterFactory()
      .typeName(new TypeToken<Event>(){},                     "event") // abstract type 'event' should not be used, but is specified because required by PolymorphicTypeAdapterFactory
      .typeName(new TypeToken<ActivityEndedEvent>(){},        "activityEnd")
      .typeName(new TypeToken<ActivityStartedEvent>(){},      "activityStarted")
      .typeName(new TypeToken<ActivityWaitingEvent>(){},      "activityWaiting")
      .typeName(new TypeToken<ScriptEndedEvent>(){},          "scriptEnded")
      .typeName(new TypeToken<ScriptStartedEvent>(){},        "scriptStarted")
      .typeName(new TypeToken<VariableCreatedEvent>(){},      "variableCreated")
      .typeName(new TypeToken<ErrorExecutionEvent>(){},       "error")
      ;
  }

}
