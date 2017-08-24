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
package io.rockscript.test;

import com.google.gson.Gson;
import io.rockscript.ScriptService;
import io.rockscript.TestScriptService;
import org.junit.After;
import org.junit.Before;

public abstract class HttpTest {

  protected final int PORT = 4000;
  protected HttpTestServer httpTestServer = new HttpTestServer(PORT);
  protected ScriptService scriptService = new TestScriptService();
  protected Gson gson = scriptService.getEngineConfiguration().getGson();

  @Before
  public void setUpApprovalActivityWorker() {
    configure(httpTestServer);
    httpTestServer.start();
  }

  protected abstract void configure(HttpTestServer httpTestServer);

  @After
  public void tearDownApprovalActivityWorker() {
    httpTestServer.stop();
  }

}
