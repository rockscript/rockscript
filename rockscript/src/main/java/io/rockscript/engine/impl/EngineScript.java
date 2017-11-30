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

import io.rockscript.api.model.ScriptVersion;
import io.rockscript.Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/** The Abstract Syntax Tree (AST) of a script version. */
public class EngineScript extends SourceElements {

  static Logger log = LoggerFactory.getLogger(EngineScript.class);

  ScriptVersion scriptVersion;
  Engine engine;
  List<ScriptElement> elements;

  public EngineScript(Integer index, Location location) {
    super(index, location);
  }

  @Override
  public Execution createExecution(Execution parent) {
    throw new RuntimeException("Use EngineScript.start(...) instead");
  }

  public Engine getEngine() {
    return engine;
  }

  public void setEngine(Engine engine) {
    this.engine = engine;
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
        if (child!=null) {
          addScriptElement(child, scriptText);
          initializeScriptElements(child.getChildren(), scriptText);
        }
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
    // log.debug(scriptElement.getIndex()+" - "+scriptElement.getClass().getSimpleName()+" - "+scriptPiece);
    scriptElement.setText(scriptPiece);
  }

  public List<ScriptElement> getElements() {
    return elements;
  }

  public ScriptVersion getScriptVersion() {
    return scriptVersion;
  }

  public void setScriptVersion(ScriptVersion scriptVersion) {
    this.scriptVersion = scriptVersion;
  }
}
