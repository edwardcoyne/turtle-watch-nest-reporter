package com.islandturtlewatch.nest.reporter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Build;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.islandturtlewatch.nest.reporter.transport.reportEndpoint.ReportEndpoint;

public class RunEnvironment {
  private enum Environment{
    LOCAL,
    DEVELOPMENT,
    PROD
  };
  private static final Environment environment = Environment.PROD;
  private static final String localAddress = "10.255.0.43";
  private static final String localAddressWPort = localAddress + ":8888";
  private static final int backendVersion = 2;


  public static String getRootBackendUrl() {
    switch(environment) {
      case LOCAL:
        return "http://" + localAddressWPort + "/_ah/api";
      case DEVELOPMENT:
      case PROD:
        return getVersionedRootUril(ReportEndpoint.DEFAULT_ROOT_URL);
      default:
        throw new UnsupportedOperationException("Don't support environment: " + environment);
    }
  }

  public static String rewriteUrlIfLocalHost(String url) {
    if (!environment.equals(Environment.LOCAL)) {
      // Bail early if prod.
      return url;
    }

    final Pattern pattern = Pattern.compile("http://([^:/]*).*");
    Matcher matcher = pattern.matcher(url);
    Preconditions.checkArgument(matcher.matches(), "Url:" + url
        + " does not match pattern: " +  pattern.toString());
    String domain = matcher.group(1);
    if (domain.equals("localhost") || domain.equals("127.0.0.1") || domain.equals("0.0.0.0")) {
      return url.replaceFirst(domain, localAddress);
    }
    return url;
  }

  private static String getVersionedRootUril(String rootUrl) {
    final Pattern pattern = Pattern.compile("([^/]*//)(.*)");
    Matcher matcher = pattern.matcher(rootUrl);
    Preconditions.checkArgument(matcher.matches(), "Url:" + rootUrl
        + " does not match pattern: " +  pattern.toString());

    return matcher.group(1) + backendVersion + "-dot-" + matcher.group(2);
  }

  public static boolean isEmulator() {
    return Build.PRODUCT.contains("sdk");
  }
}
