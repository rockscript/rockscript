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
package io.rockscript.action.http;

import io.rockscript.action.ImportJsonObject;
import io.rockscript.action.ImportProvider;
import io.rockscript.engine.ImportResolver;

public class HttpImportProvider implements ImportProvider {

  public static final String HTTP_ACTION_URL = "rockscript.io/http";

  public void provideImport(ImportResolver importResolver) {
    ImportJsonObject http = new ImportJsonObject();
    http.put("request", new HttpAction());
    importResolver.add(HTTP_ACTION_URL, http);
  }
}
