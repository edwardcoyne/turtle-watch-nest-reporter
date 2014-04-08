package com.islandturtlewatch.nest.reporter.util;

import java.util.Locale;

import com.islandturtlewatch.nest.data.ReportProto.Report;

public class ReportUtil {
  private static final int SHORT_NAME_LENGTH = 24;
  // Arguments for pattern: time_stamp, address
  private static final String SHORT_NAME_PATTERN =
      "%tm%1$td-%." + SHORT_NAME_LENGTH + "s";

  private ReportUtil() {} // Static only.

  public static String getShortName(Report report) {
    if (report.hasTimestampFoundMs()
        && report.getLocation().hasStreetAddress()) {
      return String.format(Locale.US, SHORT_NAME_PATTERN,
          report.getTimestampFoundMs(),
          report.getLocation().getStreetAddress());
    } else {
      return "New Report";
    }
  }

}
