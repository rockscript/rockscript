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
package io.rockscript.command;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.inject.Inject;
import io.rockscript.Engine;
import io.rockscript.netty.router.Request;
import io.rockscript.netty.router.Response;

public class StartScriptCommand implements Command {

  @Inject
  Engine engine;

  String scriptId;
  Object input;

  public StartScriptCommand scriptId(String scriptId) {
    this.scriptId = scriptId;
    return this;
  }

  public StartScriptCommand input(Object input) {
    this.input = input;
    return this;
  }

  public StartScriptCommand inputProperty(String propertyName, Object propertyValue) {
    if (input==null) {
      input = new LinkedHashMap<String,Object>();
    }
    if (!(input instanceof Map)) {
      throw new RuntimeException("inputProperty can only be used with maps / script objects");
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> objectMap = (Map<String, Object>) this.input;
    objectMap.put(propertyName, propertyValue);
    return this;
  }

  public static class ResponseJson {
    public String scriptExecutionId;
    public ResponseJson scriptExecutionId(String scriptExecutionId) {
      this.scriptExecutionId = scriptExecutionId;
      return this;
    }
  }

  @Override
  public void execute(Request request, Response response) {
    String scriptExecutionId = engine.startScriptExecution(scriptId);

    response.bodyJson(new ResponseJson()
      .scriptExecutionId(scriptExecutionId));
    response.statusOk();
    response.send();
  }
}
