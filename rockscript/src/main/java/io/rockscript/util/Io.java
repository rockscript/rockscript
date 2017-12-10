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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class Io {

  public static final String DEFAULT_CHARSET = "UTF-8";

  public static String getString(InputStream inputStream) {
    return getString(inputStream, DEFAULT_CHARSET);
  }

  public static String getString(InputStream inputStream, String charset) {
    if (inputStream==null) {
      return null;
    }
    Scanner scanner = new Scanner(inputStream, charset)
      .useDelimiter("\\A");
    if (scanner.hasNext()) {
      return scanner.next();
    } else {
      return "";
    }
  }

  public static String getResourceAsString(String resource) {
    return getResourceAsString(resource, DEFAULT_CHARSET);
  }

  public static String getResourceAsString(String resource, String charset) {
    InputStream resourceAsStream = getResourceAsStream(resource);
    return getString(resourceAsStream, charset);
  }

  public static boolean hasResource(String resource) {
    return Io.class.getClassLoader().getResource(resource)!=null;
  }

  public static byte[] getResourceAsBytes(String resource) {
    InputStream resourceStream = getResourceAsStream(resource);
    return getBytes(resourceStream);
  }

  public static byte[] getBytes(InputStream inputStream) {
    if (inputStream==null) {
      return null;
    }
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();

      int nRead;
      byte[] data = new byte[16384];

      while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, nRead);
      }

      buffer.flush();

      return buffer.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("Couldn't read bytes from stream: "+e.getMessage(), e);
    }
  }

  public static InputStream getResourceAsStream(String resource) {
    return Io.class.getClassLoader().getResourceAsStream(resource);
  }
}
