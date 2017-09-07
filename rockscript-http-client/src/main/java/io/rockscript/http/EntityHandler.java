package io.rockscript.http;

import org.apache.http.HttpEntity;

import java.util.function.BiFunction;

/** An EntityHandler extracts the data from the response {@link HttpEntity}
 * when the request is executed and the result is then made accessible through
 * the {@link HttpResponse#getBody()} property.
 *
 * The default entity handler is to read the HttpEntity as a String:
 * {@link StringEntityHandler}.
 *
 * To customize the EntityHandler, use
 * {@link HttpRequest#entityHandler(EntityHandler)}
 *
 * Use an EntityHandler when you want to handle the response entity in a streaming way,
 * rather then as a String. */
public interface EntityHandler extends BiFunction<HttpEntity,HttpResponse,Object>  {
}
