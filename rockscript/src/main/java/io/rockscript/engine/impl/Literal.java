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

import java.util.List;

public class Literal extends SingleExpression {

  private static class Infinity {
    private Infinity(){}
    @Override
    public String toString() {
      return "infinity";
    }
  }
  public static final Object INFINITY = new Infinity();

  private static class Undefined {
    private Undefined(){}
    @Override
    public String toString() {
      return "undefined";
    }
  }
  public static final Object UNDEFINED = new Undefined();

  private static class NaN {
    private NaN(){}
    @Override
    public String toString() {
      return "NaN";
    }
  }
  public static final Object NAN = new NaN();

  Object value;

  public Literal(Integer index, Location location) {
    super(index, location);
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  @Override
  public Execution createExecution(Execution parent) {
    return new LiteralExecution(this, parent);
  }

  @Override
  protected List<? extends ScriptElement> getChildren() {
    return null;
  }
}
