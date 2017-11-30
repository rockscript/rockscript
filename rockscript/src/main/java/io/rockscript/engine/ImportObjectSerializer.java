package io.rockscript.engine;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.rockscript.service.ImportObject;

import java.lang.reflect.Type;

public class ImportObjectSerializer implements JsonSerializer<ImportObject> {

  @Override
  public JsonElement serialize(ImportObject src, Type typeOfSrc, JsonSerializationContext context) {
    if (src!=null) {
      return new JsonPrimitive("import('" + src.getServiceName() + "')");
    }
    return null;
  }

}
