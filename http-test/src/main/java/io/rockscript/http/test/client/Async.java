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

import java.util.concurrent.Future;

import org.apache.http.client.ResponseHandler;
import org.apache.http.concurrent.BasicFuture;
import org.apache.http.concurrent.FutureCallback;

public class Async {

    private Executor executor;
    private java.util.concurrent.Executor concurrentExec;

    public static Async newInstance() {
        return new Async();
    }

    Async() {
        super();
    }

    public Async use(final Executor executor) {
        this.executor = executor;
        return this;
    }

    public Async use(final java.util.concurrent.Executor concurrentExec) {
        this.concurrentExec = concurrentExec;
        return this;
    }

    static class ExecRunnable<T> implements Runnable {

        private final BasicFuture<T> future;
        private final TestRequest testRequest;
        private final Executor executor;
        private final ResponseHandler<T> handler;

        ExecRunnable(final BasicFuture<T> future, final TestRequest testRequest, final Executor executor, final ResponseHandler<T> handler) {
            super();
            this.future = future;
            this.testRequest = testRequest;
            this.executor = executor;
            this.handler = handler;
        }

        @Override
        public void run() {
            try {
                final TestResponse testResponse = this.executor.execute(this.testRequest);
                final T result = testResponse.handleResponse(this.handler);
                this.future.completed(result);
            } catch (final Exception ex) {
                this.future.failed(ex);
            }
        }

    }

    public <T> Future<T> execute(final TestRequest testRequest, final ResponseHandler<T> handler, final FutureCallback<T> callback) {
        final BasicFuture<T> future = new BasicFuture<T>(callback);
        final ExecRunnable<T> runnable = new ExecRunnable<T>(future, testRequest, this.executor!=null ? this.executor : Executor.newInstance(), handler);
        if (this.concurrentExec!=null) {
            this.concurrentExec.execute(runnable);
        } else {
            final Thread t = new Thread(runnable);
            t.setDaemon(true);
            t.start();
        }
        return future;
    }

    public <T> Future<T> execute(final TestRequest testRequest, final ResponseHandler<T> handler) {
        return execute(testRequest, handler, null);
    }

    public Future<Content> execute(final TestRequest testRequest, final FutureCallback<Content> callback) {
        return execute(testRequest, new ContentResponseHandler(), callback);
    }

    public Future<Content> execute(final TestRequest testRequest) {
        return execute(testRequest, new ContentResponseHandler(), null);
    }

}
