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

import java.util.*;

import io.rockscript.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The Abstract Syntax Tree (AST) of a script. */
public class Script extends SourceElements {

  static Logger log = LoggerFactory.getLogger(Script.class);

  ServiceLocator serviceLocator;
  Map<String, Operation> executables;

  public Script(String id, Location location) {
    super(id, location);
  }

  @Override
  public Execution createExecution(Execution parent) {
    throw new RuntimeException("Use Script.start(...) instead");
  }

  public ServiceLocator getServiceLocator() {
    return serviceLocator;
  }

  public void setServiceLocator(ServiceLocator serviceLocator) {
    this.serviceLocator = serviceLocator;
  }

  public Operation findExecutable(String executableId) {
    return executables.get(executableId);
  }

  public void initializeExecutables(String scriptText) {
    executables = new HashMap<>();
    addExecutable(this, scriptText);
    initializeExecutables(getChildren(), scriptText);
  }

  void initializeExecutables(List<Operation> children, String scriptText) {
    if (children!=null) {
      for (Operation child: children) {
        addExecutable(child, scriptText);
        initializeExecutables(child.getChildren(), scriptText);
      }
    }
  }

  void addExecutable(Operation operation, String scriptText) {
    executables.put(operation.getId(), operation);
    Location location = operation.getLocation();
    int start = location.getStartIndex();
    int end = location.getEndIndex()+1;
    String scriptPiece = scriptText.substring(start, end).replaceAll("\\s", " ");
    log.debug(operation.getId()+" - "+operation.getClass().getSimpleName()+" - "+scriptPiece);
  }
}
