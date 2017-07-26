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
package io.rockscript.action.http;

import java.util.List;
import java.util.stream.Collectors;

class ResponseHeader {

  final String name;
  final List<String> values;

  ResponseHeader(String name, List<String> values) {
    this.name = name;
    this.values = values;
  }

  @Override
  public String toString() {
    return values.stream().map(value -> String.format("%s: %s", name, value)).collect(Collectors.joining("\n"));
  }
}
