package com.islandturtlewatch.nest.reporter.util;

import com.islandturtlewatch.nest.data.ReportProto.Report;

public class ReportUtil {
  private ReportUtil() {} // Static only.

  public static String getShortName(Report report) {
    return "Nest " + report.getNestNumber();
  }
}
