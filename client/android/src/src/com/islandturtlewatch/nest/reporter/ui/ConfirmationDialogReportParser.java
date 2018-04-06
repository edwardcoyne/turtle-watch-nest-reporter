package com.islandturtlewatch.nest.reporter.ui;

import com.islandturtlewatch.nest.data.ReportProto;
import com.islandturtlewatch.nest.reporter.util.DateUtil;

/**
 * Created by ReverendCode on 3/25/16.
 */
public class ConfirmationDialogReportParser {
    private static String message;
    private static String zone;
    private static String id;
    private static String dateRecorded;
    private static String address;
    private static String incubationDate;
    private static String actualHatchDate;

    public static String parseReport(ReportProto.Report report) {
        handleParseReport(report);
        message = "Beach Zone: " + zone + "\n" +
                id + "\n" +
                "Date Recorded: " + dateRecorded + "\n" +
                "Address: " + address + "\n" +
                "55 Day Incubation Date: " + incubationDate + "\n" +
                "Actual Hatch Date: " + actualHatchDate;
        return message;
    }

    private static void handleParseReport(ReportProto.Report report) {
        zone = String.valueOf(report.getLocation().getSection());
        if (report.hasFalseCrawlNumber()) {
            id = "False Crawl ID: " + String.valueOf(report.getFalseCrawlNumber());
            if (report.getPossibleFalseCrawl()) id += " (Possible False Crawl)";
        } else id = "Nest ID: " + String.valueOf(report.getNestNumber());
        if (report.getTimestampFoundMs() == 0) {
            dateRecorded = "Not Recorded";
        } else dateRecorded = DateUtil.getFormattedDate(report.getTimestampFoundMs());
        address = report.getLocation().getStreetAddress();
        if (report.getTimestampFoundMs() == 0) {
            incubationDate = "Not Recorded";
        } else incubationDate = DateUtil.getFormattedDate(DateUtil.plusDays(report.getTimestampFoundMs(), 55));
        if (report.getCondition().getHatchTimestampMs() == 0) {
            actualHatchDate = "Not Recorded";
        } else actualHatchDate = DateUtil.getFormattedDate(report.getCondition().getHatchTimestampMs());
    }
}
