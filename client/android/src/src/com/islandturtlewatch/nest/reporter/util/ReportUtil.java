package com.islandturtlewatch.nest.reporter.util;

import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.Report.NestStatus;

public class ReportUtil {
  private ReportUtil() {} // Static only.

  public static String getShortName(Report report) {
    if (report.getStatus() == NestStatus.FALSE_CRAWL) {
      if (!report.getPossibleFalseCrawl()) {
        return "False Crawl " + report.getFalseCrawlNumber();
      }
      return "False Crawl " + report.getFalseCrawlNumber() + " (PFC)";
    } else {
      return "Nest " + report.getNestNumber();
    }
  }
}
