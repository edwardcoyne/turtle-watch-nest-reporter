package com.islandturtlewatch.nest.reporter;

import android.os.Build;

import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.ReportEndpoint;

public class RunEnvironment {
  private enum Environment{
    LOCAL,
    DEVELOPMENT,
    PROD
  };
  private static final Environment environment = Environment.DEVELOPMENT;
  private static final String localAddress = "10.255.0.43:8888";

  public static String getRootBackendUrl() {
    switch(environment) {
      case LOCAL:
        return "http://" + localAddress + "/_ah/api";
      case DEVELOPMENT:
      case PROD:
        return ReportEndpoint.DEFAULT_ROOT_URL;
      default:
        throw new UnsupportedOperationException("Don't support environment: " + environment);
    }
  }

  public static boolean isEmulator() {
    return Build.PRODUCT.contains("sdk");
  }
}
