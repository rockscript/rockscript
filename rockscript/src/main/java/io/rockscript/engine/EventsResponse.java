package io.rockscript.engine;

import io.rockscript.engine.impl.Event;

import java.util.List;

public class EventsResponse implements CommandResponse {

  String error;
  List<? extends Event> events;

  /** for gson deserialization */
  EventsResponse() {
  }

  public EventsResponse(List<? extends Event> events) {
    this.events = events;
  }

  public EventsResponse(String error) {
    this.error = error;
  }

  public List<? extends Event> getEvents() {
    return events;
  }

  public String getError() {
    return error;
  }

  @Override
  public int getStatus() {
    return error==null ? 200 : 400;
  }
}
