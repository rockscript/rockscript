package io.rockscript;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static io.rockscript.Server.createCommandsTypeAdapterFactory;
import static io.rockscript.engine.impl.Event.createEventJsonTypeAdapterFactory;

public class RockScriptGson {

  public static Gson createCommonGson() {
    return new GsonBuilder()
      .registerTypeAdapterFactory(createCommandsTypeAdapterFactory())
      .registerTypeAdapterFactory(createEventJsonTypeAdapterFactory())
      .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
      .create();
  }

  static class InstantTypeAdapter extends TypeAdapter<Instant> {
    static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME
      .withZone(ZoneId.of("UTC"));
    @Override
    public void write(JsonWriter out, Instant value) throws IOException {
      if (value!=null) {
        out.value(ISO_FORMATTER.format(value));
      } else {
        out.nullValue();
      }
    }
    @Override
    public Instant read(JsonReader in) throws IOException {
      String isoText = in.nextString();
      return OffsetDateTime
        .parse(isoText, ISO_FORMATTER)
        .toInstant();
    }
  }
}
