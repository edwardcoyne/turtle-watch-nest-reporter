package com.islandturtlewatch.nest.reporter.net;

import java.io.IOException;

import lombok.experimental.Builder;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;

@Builder(fluent=false)
public class TimeoutWrappingRequestInitializer implements HttpRequestInitializer {
  private final HttpRequestInitializer wrapped;
  private final int connectTimeoutMs;
  private final int readTimeoutMs;

  @Override
  public void initialize(HttpRequest request) throws IOException {
    wrapped.initialize(request);
    request.setConnectTimeout(connectTimeoutMs);
    request.setReadTimeout(readTimeoutMs);
  }
}
