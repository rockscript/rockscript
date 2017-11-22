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
package io.rockscript.http.client;

import org.apache.http.HttpEntity;

/** An ClientResponseBodyHandler extracts the data from the response {@link HttpEntity}
 * when the request is executed and the result is then made accessible through
 * the {@link ClientResponse#getBody()} property.
 *
 * The default entity handler is to read the HttpEntity as a String:
 * {@link StringClientResponseBodyHandler}.
 *
 * To customize the ClientResponseBodyHandler, use
 * {@link ClientRequest#execute(ClientResponseBodyHandler)}
 *
 * Use an ClientResponseBodyHandler eg when you want to handle the response entity
 * in a streaming way, rather then as a String. */
public interface ClientResponseBodyHandler {

  Object getBody(HttpEntity entity, ClientResponse response) throws Exception;

}
