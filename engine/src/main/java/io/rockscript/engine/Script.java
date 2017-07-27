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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The Abstract Syntax Tree (AST) of a script. */
public class Script extends SourceElements {

  static Logger log = LoggerFactory.getLogger(Script.class);

  String id;
  ServiceLocator serviceLocator;
  List<ScriptElement> elements;

  public Script(Integer index, Location location) {
    super(index, location);
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

  public ScriptElement findScriptElement(int executableIndex) {
    return elements.get(executableIndex);
  }

  public void initializeScriptElements(String scriptText) {
    elements = new ArrayList<>();
    addScriptElement(this, scriptText);
    initializeScriptElements(getChildren(), scriptText);
  }

  void initializeScriptElements(List<? extends ScriptElement> children, String scriptText) {
    if (children!=null) {
      for (ScriptElement child: children) {
        addScriptElement(child, scriptText);
        initializeScriptElements(child.getChildren(), scriptText);
      }
    }
  }

  void addScriptElement(ScriptElement scriptElement, String scriptText) {
    int executableIndex = elements.size();
    scriptElement.setIndex(executableIndex);
    elements.add(scriptElement);

    Location location = scriptElement.getLocation();
    int start = location.getStart();
    int end = location.getEnd()+1;
    String scriptPiece = scriptText.substring(start, end).replaceAll("\\s", " ");
    log.debug(scriptElement.getIndex()+" - "+scriptElement.getClass().getSimpleName()+" - "+scriptPiece);
    scriptElement.setText(scriptPiece);
  }

  public List<ScriptElement> getElements() {
    return elements;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
