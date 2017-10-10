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
package io.rockscript.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Exceptions {

  public static String getRecursiveMessage(Throwable t) {
    List<Throwable> exceptionStack = new ArrayList<>();
    addRecursive(t, exceptionStack);
    return exceptionStack.stream()
      .map(e -> e.getMessage())
      .collect(Collectors.joining("\n -> "));
  }

  private static void addRecursive(Throwable t, List<Throwable> exceptionStack) {
    if (t!=null) {
      exceptionStack.add(t);
      addRecursive(t.getCause(), exceptionStack);
    }
  }

  public static void assertContains(String expected, String text) {
    if (text==null || !text.contains(expected)) {
      throw new AssertionError("Expected '"+expected+"' to be present, but text was "+(text!=null ? "'"+text+"'" : "null"));
    }
  }

  public static String getStackTraceString(Throwable t) {
    StringWriter out = new StringWriter();
    t.printStackTrace(new PrintWriter(out));
    return out.toString();
  }

  public static void rethrowRuntimeException(Throwable e) {
    if (e !=null) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      throw new RuntimeException(e);
    }
  }
}
