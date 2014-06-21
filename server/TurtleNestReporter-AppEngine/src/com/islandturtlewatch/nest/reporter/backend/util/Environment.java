package com.islandturtlewatch.nest.reporter.backend.util;

import com.google.appengine.api.utils.SystemProperty;

public class Environment {
  private Environment() {} // static only

  public boolean isProd() {
    return SystemProperty.environment.value() == SystemProperty.Environment.Value.Production;
  }
}
