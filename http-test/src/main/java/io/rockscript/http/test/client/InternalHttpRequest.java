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

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.concurrent.Cancellable;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicRequestLine;

import static org.apache.http.HttpVersion.HTTP_1_1;
import static org.apache.http.util.Args.notBlank;
import static org.apache.http.util.Args.notNull;

@NotThreadSafe
class InternalHttpRequest extends AbstractHttpMessage implements HttpUriRequest, HttpExecutionAware, Configurable {

    private final String method;
    private ProtocolVersion version;
    private URI uri;
    private RequestConfig config;

    private final AtomicBoolean aborted;
    private final AtomicReference<Cancellable> cancellableRef;

    InternalHttpRequest(final String method, final URI requestURI) {
        notBlank(method, "Method");
        notNull(requestURI, "Request URI");
        this.method = method;
        this.uri = requestURI;
        this.aborted = new AtomicBoolean(false);
        this.cancellableRef = new AtomicReference<Cancellable>(null);
    }

    public void setProtocolVersion(final ProtocolVersion version) {
        this.version = version;
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return version!=null ? version : HTTP_1_1;
    }

    @Override
    public String getMethod() {
        return this.method;
    }

    @Override
    public URI getURI() {
        return this.uri;
    }

    @Override
    public void abort() throws UnsupportedOperationException {
        if (this.aborted.compareAndSet(false, true)) {
            final Cancellable cancellable = this.cancellableRef.getAndSet(null);
            if (cancellable!=null) {
                cancellable.cancel();
            }
        }
    }

    @Override
    public boolean isAborted() {
        return this.aborted.get();
    }

    @Override
    public void setCancellable(final Cancellable cancellable) {
        if (!this.aborted.get()) {
            this.cancellableRef.set(cancellable);
        }
    }

    @Override
    public RequestLine getRequestLine() {
        final ProtocolVersion ver = getProtocolVersion();
        final URI uriCopy = getURI();
        String uritext = null;
        if (uriCopy!=null) {
            uritext = uriCopy.toASCIIString();
        }
        if (uritext==null || uritext.isEmpty()) {
            uritext = "/";
        }
        return new BasicRequestLine(getMethod(), uritext, ver);
    }

    @Override
    public RequestConfig getConfig() {
        return config;
    }

    public void setConfig(final RequestConfig config) {
        this.config = config;
    }

    public void setURI(final URI uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        return getMethod()+" "+getURI()+" "+getProtocolVersion();
    }

}
