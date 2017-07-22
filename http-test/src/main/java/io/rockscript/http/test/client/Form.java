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
package io.rockscript.http.test.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class Form {

  private final List<NameValuePair> params;

  public static Form form() {
    return new Form();
  }

  Form() {
    super();
    this.params = new ArrayList<NameValuePair>();
  }

  public Form add(final String name, final String value) {
    this.params.add(new BasicNameValuePair(name, value));
    return this;
  }

  public List<NameValuePair> build() {
    return new ArrayList<NameValuePair>(this.params);
  }

}
