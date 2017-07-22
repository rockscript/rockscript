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
package io.rockscript.netty.router;

import static java.lang.String.format;

public class InternalServerException extends HttpException {

  private static final long serialVersionUID = 1L;

  public InternalServerException() {
    super();
  }

  public InternalServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public static void checkNotNull(Object o, String message, Object... args) {
    checkTrue(o==null, message, args);
  }

  public static void checkTrue(boolean condition, String message, Object... args) {
    if (condition) {
      throw new InternalServerException(format(message, args));
    }
  }

  public InternalServerException(String message, Throwable cause) {
    super(message, cause);
  }

  public InternalServerException(String message) {
    super(message);
  }

  public InternalServerException(Throwable cause) {
    super(cause);
  }

  @Override
  public int getStatusCode() {
    return 500;
  }
}
