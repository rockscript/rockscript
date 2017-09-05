package io.rockscript.http;

import java.lang.reflect.Type; /** transforms between objects and serialization format used in
 * the body of http requests or responses. */
public interface Codec {

  String serialize(Object bodyObject);


  <T> T deserialize(String serializedForm, Type type);
}
