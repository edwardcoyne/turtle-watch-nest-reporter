package com.islandturtlewatch.nest.reporter.util;

import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.Report.NestStatus;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore;

public class ReportUtil {
  private ReportUtil() {} // Static only.

  public static String getShortName(LocalDataStore.CachedReportWrapper wrapper) {
    Report report = wrapper.getReport();
    if (wrapper.getPossibleFalseCrawlDuplicate()) {
      return "Possible False Crawl " + report.getPossibleFalseCrawlNumber() +
              " (False Crawl " + report.getFalseCrawlNumber() + ")";
    }
    if (report.getStatus() == NestStatus.FALSE_CRAWL) {
      //add logic to check for duplicate duplicate
      if (!report.getPossibleFalseCrawl()) {
        return "False Crawl " + report.getFalseCrawlNumber();
      }
      return "False Crawl " + report.getFalseCrawlNumber() + " (PFC " +
              report.getPossibleFalseCrawlNumber() + ")";
    } else {
      return "Nest " + report.getNestNumber();
    }
  }
}
