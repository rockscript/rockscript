package io.rockscript.http;

import org.apache.http.HttpEntity;

import java.util.function.BiFunction;

public interface EntityHandler extends BiFunction<HttpEntity,HttpResponse,Object>  {
}
