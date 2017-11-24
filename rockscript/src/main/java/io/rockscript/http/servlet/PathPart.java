/*
 * Copyright (c) 2017 RockScript.io.
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.rockscript.http.servlet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class PathPart {

  public static List<PathPart> parse(String pathTemplate) {
    List<PathPart> pathParts = new ArrayList<>();
    for (String partText: pathTemplate.split("/")) {
      if (partText.startsWith("{")
          && partText.endsWith("}")
          && partText.length()>2) {
        pathParts.add(new DynamicPart(partText.substring(1, partText.length()-1)));
      } else {
        pathParts.add(new FixedPart(partText));
      }
    }
    return pathParts;
  }

  public abstract boolean matches(String actualPart, Map<String, String> pathParameters);

  public static class FixedPart extends PathPart {
    String text;
    public FixedPart(String text) {
      this.text = text;
    }
    @Override
    public boolean matches(String actualPart, Map<String, String> pathParameters) {
      return text.equals(actualPart);
    }
  }

  public static class DynamicPart extends PathPart {
    String parameterName;
    public DynamicPart(String parameterName) {
      this.parameterName = parameterName;
    }
    @Override
    public boolean matches(String actualPart, Map<String, String> pathParameters) {
      pathParameters.put(parameterName, actualPart);
      return true;
    }
  }
}
