package com.islandturtlewatch.nest.reporter.util;

import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.Report.NestStatus;

public class ReportUtil {
  private ReportUtil() {} // Static only.

  public static String getShortName(Report report) {
    return ((report.getStatus() == NestStatus.FALSE_CRAWL) ? "False Crawl " : "Nest ")
        + report.getNestNumber();
  }
}
