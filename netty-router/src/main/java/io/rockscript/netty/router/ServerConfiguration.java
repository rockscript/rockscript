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

package io.rockscript.netty.router;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.netty.handler.codec.http.router.Router;

public class ServerConfiguration {

  protected Router<Class<?>> router = new Router<>();
  protected int port = 8888;
  protected List<Interceptor> interceptors;
  @Inject
  protected Injector services;

  public Server build() {
    return new Server(this);
  }

  public ServerConfiguration component(ServerComponent component) {
    component.configure(this);
    return this;
  }

  public Router<Class<?>> getRouter() {
    return router;
  }

  public int getPort() {
    return this.port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public ServerConfiguration port(int port) {
    this.port = port;
    return this;
  }

  public Injector getServices() {
    return services;
  }

  public void setServices(Injector services) {
    this.services = services;
  }

  public ServerConfiguration services(Injector services) {
    this.services = services;
    return this;
  }

  public List<Interceptor> getInterceptors() {
    return this.interceptors;
  }

  public void setInterceptors(List<Interceptor> interceptors) {
    this.interceptors = interceptors;
  }

  public ServerConfiguration interceptor(Interceptor interceptor) {
    if (interceptors==null) {
      interceptors = new ArrayList<>();
    }
    this.interceptors.add(interceptor);
    return this;
  }

  public ServerConfiguration scan(Class... classes) {
    if (classes!=null) {
      for (Class clazz : classes) {
        scan(clazz);
      }
    }
    return this;
  }

  public ServerConfiguration scan(Class<?> clazz) {
    Gets repeatableAnnotation = clazz.getDeclaredAnnotation(Gets.class);
    Get[] annotations = repeatableAnnotation!=null ? repeatableAnnotation.value() : null;
    for (Get annotation : list(clazz.getDeclaredAnnotation(Get.class), annotations)) {
      GET(annotation.value(), clazz);
    }

    Puts repeatablePuts = clazz.getDeclaredAnnotation(Puts.class);
    Put[] puts = repeatablePuts!=null ? repeatablePuts.value() : null;
    for (Put annotation : list(clazz.getDeclaredAnnotation(Put.class), puts)) {
      PUT(annotation.value(), clazz);
    }

    Posts repeatablePosts = clazz.getDeclaredAnnotation(Posts.class);
    Post[] posts = repeatablePosts!=null ? repeatablePosts.value() : null;
    for (Post annotation : list(clazz.getDeclaredAnnotation(Post.class), posts)) {
      POST(annotation.value(), clazz);
    }

    Deletes repeatableDeletes = clazz.getDeclaredAnnotation(Deletes.class);
    Delete[] deletes = repeatableDeletes!=null ? repeatableDeletes.value() : null;
    for (Delete annotation : list(clazz.getDeclaredAnnotation(Delete.class), deletes)) {
      DELETE(annotation.value(), clazz);
    }

    return this;
  }

  private <T> List<T> list(T annotation, T[] annotations) {
    List<T> list = new ArrayList<>();
    if (annotation!=null) {
      list.add(annotation);
    }
    if (annotations!=null) {
      for (T e : annotations) {
        list.add(e);
      }
    }
    return list;
  }

  public ServerConfiguration DELETE(String path, Class<?> target) {
    router.DELETE(path, target);
    return this;
  }

  public ServerConfiguration GET(String path, Class<?> target) {
    router.GET(path, target);
    return this;
  }

  public ServerConfiguration HEAD(String path, Class<?> target) {
    router.HEAD(path, target);
    return this;
  }

  public ServerConfiguration OPTIONS(String path, Class<?> target) {
    router.OPTIONS(path, target);
    return this;
  }

  public ServerConfiguration PATCH(String path, Class<?> target) {
    router.PATCH(path, target);
    return this;
  }

  public ServerConfiguration POST(String path, Class<?> target) {
    router.POST(path, target);
    return this;
  }

  public ServerConfiguration PUT(String path, Class<?> target) {
    router.PUT(path, target);
    return this;
  }

  public ServerConfiguration TRACE(String path, Class<?> target) {
    router.TRACE(path, target);
    return this;
  }

  public ServerConfiguration DELETE_FIRST(String path, Class<?> target) {
    router.DELETE_FIRST(path, target);
    return this;
  }

  public ServerConfiguration GET_FIRST(String path, Class<?> target) {
    router.GET_FIRST(path, target);
    return this;
  }

  public ServerConfiguration HEAD_FIRST(String path, Class<?> target) {
    router.HEAD_FIRST(path, target);
    return this;
  }

  public ServerConfiguration OPTIONS_FIRST(String path, Class<?> target) {
    router.OPTIONS_FIRST(path, target);
    return this;
  }

  public ServerConfiguration PATCH_FIRST(String path, Class<?> target) {
    router.PATCH_FIRST(path, target);
    return this;
  }

  public ServerConfiguration POST_FIRST(String path, Class<?> target) {
    router.POST_FIRST(path, target);
    return this;
  }

  public ServerConfiguration PUT_FIRST(String path, Class<?> target) {
    router.PUT_FIRST(path, target);
    return this;
  }

  public ServerConfiguration TRACE_FIRST(String path, Class<?> target) {
    router.TRACE_FIRST(path, target);
    return this;
  }

  public ServerConfiguration DELETE_LAST(String path, Class<?> target) {
    router.DELETE_LAST(path, target);
    return this;
  }

  public ServerConfiguration GET_LAST(String path, Class<?> target) {
    router.GET_LAST(path, target);
    return this;
  }

  public ServerConfiguration HEAD_LAST(String path, Class<?> target) {
    router.HEAD_LAST(path, target);
    return this;
  }

  public ServerConfiguration OPTIONS_LAST(String path, Class<?> target) {
    router.OPTIONS_LAST(path, target);
    return this;
  }

  public ServerConfiguration PATCH_LAST(String path, Class<?> target) {
    router.PATCH_LAST(path, target);
    return this;
  }

  public ServerConfiguration POST_LAST(String path, Class<?> target) {
    router.POST_LAST(path, target);
    return this;
  }

  public ServerConfiguration PUT_LAST(String path, Class<?> target) {
    router.PUT_LAST(path, target);
    return this;
  }

  public ServerConfiguration TRACE_LAST(String path, Class<?> target) {
    router.TRACE_LAST(path, target);
    return this;
  }

  public ServerConfiguration notFound(Class<?> target) {
    router.notFound(target);
    return this;
  }

  public ServerConfiguration defaultNotFoundHandler() {
    router.notFound(DefaultNotFoundHandler.class);
    return this;
  }
}
