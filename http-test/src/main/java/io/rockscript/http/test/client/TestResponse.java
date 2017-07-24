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

import java.io.*;
import java.nio.charset.Charset;

import io.rockscript.netty.router.JsonHandler;
import org.apache.http.*;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;

import static io.rockscript.http.test.AbstractServerTest.serverException;
import static java.nio.charset.Charset.forName;
import static org.apache.http.HttpStatus.*;
import static org.apache.http.entity.ContentType.getOrDefault;
import static org.apache.http.util.EntityUtils.toByteArray;

public class TestResponse {

    private static final Charset UTF8 = forName("UTF-8");
    protected TestRequest testRequest;
    protected final HttpResponse response;
    protected boolean consumed;

    TestResponse(final HttpResponse response, TestRequest testRequest) {
        super();
        this.testRequest = testRequest;
        this.response = response;
    }

    private void assertNotConsumed() {
        if (this.consumed) {
            throw new IllegalStateException("Response content has been already consumed");
        }
    }

    private void dispose() {
        if (this.consumed) {
            return;
        }
        try {
            final HttpEntity entity = this.response.getEntity();
            final InputStream content = entity.getContent();
            if (content!=null) {
                content.close();
            }
        } catch (final Exception ignore) {
        } finally {
            this.consumed = true;
        }
    }

    /**
     * Discards response content and deallocates all resources associated with it.
     */
    public void discardContent() {
        dispose();
    }

    /**
     * Handles the response using the specified {@link ResponseHandler}
     */
    public <T> T handleResponse(final ResponseHandler<T> handler) {
        assertNotConsumed();
        try {
            return handler.handleResponse(this.response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            dispose();
        }
    }

    public Content returnContent() {
        return handleResponse(new ContentResponseHandler());
    }

    public HttpResponse returnResponse() throws IOException {
        assertNotConsumed();
        try {
            final HttpEntity entity = this.response.getEntity();
            if (entity!=null) {
                final ByteArrayEntity byteArrayEntity = new ByteArrayEntity(toByteArray(entity));
                final ContentType contentType = getOrDefault(entity);
                byteArrayEntity.setContentType(contentType.toString());
                this.response.setEntity(byteArrayEntity);
            }
            return this.response;
        } finally {
            this.consumed = true;
        }
    }

    public String bodyStringUtf8() {
        return returnContent().asString(UTF8);
    }

    public <T> T body(Class<T> type) {
        String jsonBodyString = bodyStringUtf8();
        JsonHandler jsonHandler = testRequest.test.getNettyServer().getJsonHandler();
        return (T) jsonHandler.fromJsonString(jsonBodyString, type);
    }

    public TestResponse assertStatusCreated() {
        return assertStatus(SC_CREATED);
    }

    public TestResponse assertStatusOk() {
        return assertStatus(SC_OK);
    }

    public TestResponse assertStatusNoContent() {
        return assertStatus(SC_NO_CONTENT);
    }

    public TestResponse assertStatus(int expectedStatusCode) {
        int responseStatusCode = response.getStatusLine().getStatusCode();
        if (responseStatusCode!=expectedStatusCode) {
            Throwable serverCause = serverException;
            throw new BadStatusException("Expected "+expectedStatusCode+", but was "+responseStatusCode, serverCause);
        }
        return this;
    }

    public void saveContent(final File file) throws IOException {
        assertNotConsumed();
        final StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode()>=300) {
            throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }
        final FileOutputStream out = new FileOutputStream(file);
        try {
            final HttpEntity entity = this.response.getEntity();
            if (entity!=null) {
                entity.writeTo(out);
            }
        } finally {
            this.consumed = true;
            out.close();
        }
    }
}
