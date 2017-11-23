/*
 * Copyright (c) 2017, RockScript.io. All rights reserved.
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

package io.rockscript.activity;

import io.rockscript.Engine;
import io.rockscript.engine.impl.RemoteActivityJsonObject;

import java.util.HashMap;
import java.util.Map;

public class ImportResolver {

  /** importProviders by name */
  Map<String,ImportProvider> importProviders = new HashMap<>();

  public ImportResolver(Engine engine) {
  }

  public ImportResolver add(ImportProvider importProvider) {
    importProviders.put(importProvider.getImportObject().getServiceName(), importProvider);
    return this;
  }

  public Object get(String url) {
    Object importObject;
    ImportProvider importProvider = importProviders.get(url);
    if (importProvider!=null) {
      importObject = importProvider.getImportObject();
    } else {
      importObject = new RemoteActivityJsonObject(url);
    }
    return importObject;
  }

  public ImportObject createImport(String url) {
    ImportObject importObject = new ImportObject(url);
    add(new StaticImportProvider(importObject));
    return importObject;
  }

}
