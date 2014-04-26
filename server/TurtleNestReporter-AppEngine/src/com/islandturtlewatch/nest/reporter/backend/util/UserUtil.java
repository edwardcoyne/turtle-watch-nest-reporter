package com.islandturtlewatch.nest.reporter.backend.util;

import com.google.appengine.api.users.User;

public class UserUtil {
  private UserUtil() {} // No instantiation.

  public static String getUserId(User user) {
    // user.getUserId() is broken! returns 'null', this is a known issue, using email as id, it
    // is unique for all authenticated users.
    // Tracked: https://code.google.com/p/googleappengine/issues/detail?id=8848
    return user.getEmail();
  }
}
