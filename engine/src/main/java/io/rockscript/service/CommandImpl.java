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
package io.rockscript.service;

import io.rockscript.Command;

public abstract class CommandImpl<R> implements Command<R> {

  /** transient because this field should not be serialized when using Gson */
  transient protected Configuration configuration;

  /** This constructor is used for json serialization.
   * When using this constructor, make sure that
   * {@link #setConfiguration(Configuration)} is called
   * before {@link #execute()} is invoked. */
  public CommandImpl() {
  }

  public CommandImpl(Configuration configuration) {
    this.configuration = configuration;
  }

  protected abstract R execute(Configuration configuration);

  @Override
  public R execute() {
    return execute(configuration);
  }

  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }
}
