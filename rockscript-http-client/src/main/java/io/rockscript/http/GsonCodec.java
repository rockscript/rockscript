package io.rockscript.http;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public class GsonCodec implements Codec {

  Gson gson;

  public GsonCodec(Gson gson) {
    this.gson = gson;
  }

  @Override
  public String serialize(Object bodyObject) {
    return gson.toJson(bodyObject);
  }

  @Override
  public <T> T deserialize(String serializedForm, Type type) {
    return gson.fromJson(serializedForm, type);
  }
}
