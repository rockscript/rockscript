package io.rockscript.db;

import java.util.HashMap;
import java.util.Map;

public class ParameterMap {

  Map<String, Integer> indexes = new HashMap<>();

  public void add(String parameterName) {
    indexes.put(parameterName, indexes.size() + 1);
  }

  public int getIndex(String parameterName) {
    return indexes.get(parameterName);
  }
}
