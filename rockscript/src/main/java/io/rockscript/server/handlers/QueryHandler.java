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
package io.rockscript.server.handlers;

import io.rockscript.netty.router.Post;

/** Handles POST requests for /query
 *
 * Queries are done with a HTTP POST because they have
 * query details in the body
 * https://stackoverflow.com/questions/978061/http-get-with-request-body
 *
 * Implementation wise we don't see a difference yet between commands and
 * queries so we leverage the command infrastructure for queries.  This
 * could be revisited later.
 */
@Post("/query")
public class QueryHandler extends CommandHandler {
}
