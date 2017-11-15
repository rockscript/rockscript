package io.rockscript.activity.test;

import io.rockscript.api.model.ScriptExecution;

public class TestScriptExecution extends ScriptExecution {

  String scriptText;
  String scriptName;

  public String getScriptText() {
    return scriptText;
  }

  public void setScriptText(String scriptText) {
    this.scriptText = scriptText;
  }

  public String getScriptName() {
    return scriptName;
  }

  public void setScriptName(String scriptName) {
    this.scriptName = scriptName;
  }
}
