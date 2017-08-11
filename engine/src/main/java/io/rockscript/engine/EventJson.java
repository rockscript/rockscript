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

import com.google.gson.reflect.TypeToken;
import io.rockscript.gson.PolymorphicTypeAdapterFactory;

public class EventJson {

  public static PolymorphicTypeAdapterFactory createEventJsonTypeAdapterFactory() {
    return new PolymorphicTypeAdapterFactory()
      .typeName(new TypeToken<EventJson>(){},                     "event") // abstract type 'event' should not be used, but is specified because required by PolymorphicTypeAdapterFactory
      .typeName(new TypeToken<ActionEndedEventJson>(){},          "actionEnd")
      .typeName(new TypeToken<ActionStartedEventJson>(){},        "actionStarted")
      .typeName(new TypeToken<ActionWaitingEventJson>(){},        "actionWaiting")
      .typeName(new TypeToken<IdentifierResolvedEventJson>(){},   "identifierResolved")
      .typeName(new TypeToken<ObjectImportedEventJson>(){},       "objectImported")
      .typeName(new TypeToken<PropertyDereferencedEventJson>(){}, "propertyDereferenced")
      .typeName(new TypeToken<ScriptDeployedEventJson>(){},       "scriptDeployed")
      .typeName(new TypeToken<ScriptEndedEventJson>(){},          "scriptEnded")
      .typeName(new TypeToken<ScriptStartedEventJson>(){},        "scriptStarted")
      .typeName(new TypeToken<VariableCreatedEventJson>(){},      "variableCreated")
      ;
  }
}
