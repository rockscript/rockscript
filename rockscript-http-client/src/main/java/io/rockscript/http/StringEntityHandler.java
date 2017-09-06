package io.rockscript.http;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class StringEntityHandler implements EntityHandler {

  @Override
  public Object apply(HttpEntity httpEntity, HttpResponse httpResponse) {
    try {
      return EntityUtils.toString(httpEntity, "UTF-8");
    } catch (IOException e) {
      throw new RuntimeException("Couldn't ready body/entity from http request "+httpResponse.toString());
    }
  }
}
