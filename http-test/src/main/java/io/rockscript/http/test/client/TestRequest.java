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
import java.text.SimpleDateFormat;
import java.util.*;

import io.rockscript.http.test.AbstractServerTest;
import javafx.util.Pair;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HttpContext;

import static io.rockscript.http.test.client.Executor.CLIENT;
import static java.net.URI.create;
import static java.util.Arrays.asList;
import static java.util.Locale.US;
import static java.util.TimeZone.getTimeZone;
import static org.apache.http.Consts.ISO_8859_1;
import static org.apache.http.client.config.RequestConfig.*;
import static org.apache.http.client.methods.HttpGet.METHOD_NAME;
import static org.apache.http.client.utils.URLEncodedUtils.CONTENT_TYPE;
import static org.apache.http.client.utils.URLEncodedUtils.format;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

public class TestRequest {

  public static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
  public static final Locale DATE_LOCALE = US;
  public static final TimeZone TIME_ZONE = getTimeZone("GMT");

  protected String method;
  protected String uriString;
  protected List<Pair<String, String>> queryParameters;
  protected List<Pair<String, String>> headers;
  protected HttpEntity entity;
  protected Boolean useExpectContinue;
  protected Integer socketTmeout;
  protected Integer connectTimeout;
  protected HttpHost proxy;
  protected SimpleDateFormat dateFormatter;
  protected AbstractServerTest test;

  public static TestRequest Get(final String uri) {
    return new TestRequest(METHOD_NAME, uri);
  }

  public static TestRequest Head(final String uri) {
    return new TestRequest(HttpHead.METHOD_NAME, uri);
  }

  private boolean methodSupportsBody() {
    return HttpPost.METHOD_NAME.equals(method) || HttpPatch.METHOD_NAME.equals(method) || HttpPut.METHOD_NAME.equals(method);
  }

  public static TestRequest Post(final String uri) {
    return new TestRequest(HttpPost.METHOD_NAME, uri);
  }

  public static TestRequest Patch(final String uri) {
    return new TestRequest(HttpPatch.METHOD_NAME, uri);
  }

  public static TestRequest Put(final String uri) {
    return new TestRequest(HttpPut.METHOD_NAME, uri);
  }

  public static TestRequest Trace(final String uri) {
    return new TestRequest(HttpTrace.METHOD_NAME, uri);
  }

  public static TestRequest Delete(final String uri) {
    return new TestRequest(HttpDelete.METHOD_NAME, uri);
  }

  public static TestRequest Options(final String uri) {
    return new TestRequest(HttpOptions.METHOD_NAME, uri);
  }

  TestRequest(String method, String uriString) {
    super();
    this.method = method;
    this.uriString = uriString;
  }

  HttpResponse internalExecute(final HttpClient client, final HttpContext localContext) {
    final InternalHttpRequest request = buildRequest();
    final RequestConfig config = buildRequestConfig(client);
    request.setConfig(config);
    try {
      return client.execute(request, localContext);
    } catch (IOException e) {
      Throwable serverException = AbstractServerTest.serverException;
      throw new RuntimeException(serverException!=null ? serverException : e);
    }
  }

  private InternalHttpRequest buildRequest() {
    InternalHttpRequest request = null;
    if (methodSupportsBody()) {
      request = new InternalEntityEnclosingHttpRequest(method, create(buildUriWithQueryParameters()));
    } else {
      request = new InternalHttpRequest(method, create(buildUriWithQueryParameters()));
    }

    if (headers!=null) {
      for (Pair<String, String> header : headers) {
        request.addHeader(header.getKey(), header.getValue());
      }
    }

    if (entity!=null) {
      if (request instanceof HttpEntityEnclosingRequest) {
        ((InternalEntityEnclosingHttpRequest) request).setEntity(entity);
      } else {
        throw new IllegalStateException(method+" request does not support an entity");
      }
    }
    return request;
  }

  private RequestConfig buildRequestConfig(HttpClient client) {
    final Builder builder;
    if (client instanceof Configurable) {
      builder = copy(((Configurable) client).getConfig());
    } else {
      builder = custom();
    }
    if (this.useExpectContinue!=null) {
      builder.setExpectContinueEnabled(this.useExpectContinue);
    }
    if (this.socketTmeout!=null) {
      builder.setSocketTimeout(this.socketTmeout);
    }
    if (this.connectTimeout!=null) {
      builder.setConnectTimeout(this.connectTimeout);
    }
    if (this.proxy!=null) {
      builder.setProxy(this.proxy);
    }
    return builder.build();
  }

  public String buildUriWithQueryParameters() {
    if (queryParameters!=null) {
      for (Pair<String, String> queryParameter : queryParameters) {
        uriString += uriString.contains("?") ? "&" : "&"+queryParameter.getKey()+"="+queryParameter.getValue();
      }
      queryParameters = null;
    }
    return uriString;
  }

  public TestResponse execute() {
    return new TestResponse(internalExecute(CLIENT, null), this);
  }

  public TestRequest queryParameter(final String name, final String value) {
    if (queryParameters==null) {
      queryParameters = new ArrayList<>();
    }
    queryParameters.add(new Pair<>(name, value));
    return this;
  }

  public TestRequest header(final String name, final String value) {
    if (headers==null) {
      headers = new ArrayList<>();
    }
    headers.add(new Pair<>(name, value));
    return this;
  }

  // // HTTP entity operations

  public TestRequest body(final HttpEntity entity) {
    this.entity = entity;
    return this;
  }

  public TestRequest bodyForm(final Iterable<? extends NameValuePair> formParams, final Charset charset) {
    final List<NameValuePair> paramList = new ArrayList<NameValuePair>();
    for (NameValuePair param : formParams) {
      paramList.add(param);
    }
    final ContentType contentType = ContentType.create(CONTENT_TYPE, charset);
    final String s = format(paramList, charset!=null ? charset.name() : null);
    return bodyString(s, contentType);
  }

  public TestRequest bodyForm(final Iterable<? extends NameValuePair> formParams) {
    return bodyForm(formParams, ISO_8859_1);
  }

  public TestRequest bodyForm(final NameValuePair... formParams) {
    return bodyForm(asList(formParams), ISO_8859_1);
  }

  public TestRequest bodyString(final String s, final ContentType contentType) {
    final Charset charset = contentType!=null ? contentType.getCharset() : null;
    final byte[] raw = charset!=null ? s.getBytes(charset) : s.getBytes();
    return body(new InternalByteArrayEntity(raw, contentType));
  }

  public TestRequest bodyJsonString(String jsonString) {
    return bodyString(jsonString, APPLICATION_JSON);
  }

  public TestRequest bodyJson(Object bodyObject) {
    String jsonString = test.getJson().toJsonString(bodyObject);
    bodyString(jsonString, APPLICATION_JSON);
    return this;
  }

  public TestRequest bodyFile(final File file, final ContentType contentType) {
    return body(new InternalFileEntity(file, contentType));
  }

  public TestRequest bodyByteArray(final byte[] b) {
    return body(new InternalByteArrayEntity(b));
  }

  /**
   * @since 4.4
   */
  public TestRequest bodyByteArray(final byte[] b, final ContentType contentType) {
    return body(new InternalByteArrayEntity(b, contentType));
  }

  public TestRequest bodyByteArray(final byte[] b, final int off, final int len) {
    return body(new InternalByteArrayEntity(b, off, len));
  }

  /**
   * @since 4.4
   */
  public TestRequest bodyByteArray(final byte[] b, final int off, final int len, final ContentType contentType) {
    return body(new InternalByteArrayEntity(b, off, len, contentType));
  }

  public TestRequest bodyStream(final InputStream instream) {
    return body(new InternalInputStreamEntity(instream, -1, null));
  }

  public TestRequest bodyStream(final InputStream instream, final ContentType contentType) {
    return body(new InternalInputStreamEntity(instream, -1, contentType));
  }

  @Override
  public String toString() {
    return buildUriWithQueryParameters();
  }

  public AbstractServerTest getTest() {
    return test;
  }

  public void setTest(AbstractServerTest test) {
    this.test = test;
  }
}
