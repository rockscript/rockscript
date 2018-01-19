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

package io.rockscript.service;

import io.rockscript.Engine;
import io.rockscript.engine.impl.RemoteServiceFunctionJsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportResolver {

  /** importProviders by name */
  Map<String,ImportProvider> importProviders = new HashMap<>();

  public ImportResolver(Engine engine, List<ImportProvider> importProviders) {
    importProviders.forEach(importProvider -> add(importProvider));
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
      importObject = new RemoteServiceFunctionJsonObject(url);
    }
    return importObject;
  }

  public ImportObject createImport(String url) {
    ImportObject importObject = new ImportObject(url);
    add(importObject);
    return importObject;
  }

}
