package com.islandturtlewatch.nest.reporter.net;

import java.io.IOException;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;

public class TimeoutWrappingRequestInitializer implements HttpRequestInitializer {
  private final HttpRequestInitializer wrapped;
  private final int connectTimeoutMs;
  private final int readTimeoutMs;

  public TimeoutWrappingRequestInitializer(HttpRequestInitializer wrapped,
                                           int connectTimeoutMs, int readTimeoutMs) {
    this.wrapped = wrapped;
    this.connectTimeoutMs = connectTimeoutMs;
    this.readTimeoutMs = readTimeoutMs;
  }

  @Override
  public void initialize(HttpRequest request) throws IOException {
    wrapped.initialize(request);
    request.setConnectTimeout(connectTimeoutMs);
    request.setReadTimeout(readTimeoutMs);
  }
}
