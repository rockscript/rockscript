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

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.AbstractResponseHandler;

import static io.rockscript.http.test.client.Content.NO_CONTENT;
import static org.apache.http.entity.ContentType.getOrDefault;
import static org.apache.http.util.EntityUtils.toByteArray;

/**
 * {@link ResponseHandler} implementation that converts
 * {@link HttpResponse} messages to {@link Content}
 * instances.
 *
 * @see Content
 * @since 4.4
 */
public class ContentResponseHandler extends AbstractResponseHandler<Content> {

    @Override
    public Content handleEntity(final HttpEntity entity) throws IOException {
        return entity!=null ? new Content(toByteArray(entity), getOrDefault(entity)) : NO_CONTENT;
    }

}
