package com.islandturtlewatch.nest.reporter.util;

import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.Report.NestStatus;

public class ReportUtil {
  private ReportUtil() {} // Static only.

  public static String getShortName(Report report) {
    if (report.getStatus() == NestStatus.FALSE_CRAWL) {
      return "False Crawl " + report.getFalseCrawlNumber();
    } else {
      return "Nest " + report.getNestNumber();
    }
  }
}
