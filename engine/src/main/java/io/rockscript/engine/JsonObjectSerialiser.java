package io.rockscript.engine;

import java.lang.reflect.Type;

import com.google.gson.*;

public class JsonObjectSerialiser implements JsonSerializer<JsonObject> {

  @Override
  public JsonElement serialize(JsonObject jsonObject, Type type, JsonSerializationContext context) {
    return context.serialize(jsonObject.properties);
  }
}
