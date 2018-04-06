package com.islandturtlewatch.nest.reporter.frontend.reports;

/**
 * Created by ReverendCode on 4/19/17.
 */
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedColumnWithFetcher;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.ReportColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.ValueFetcher;
public class ColumnGenerator {

    public ReportColumn generateDefaultColumn(String name, String stringPath) {
        return new MappedColumnWithFetcher(name,
                new FetcherBuilder().addDisplayPath(stringPath).build());
    }

    public ReportColumn generateTimestampColumn(String name, String stringPath) {
        ValueFetcher fetcher = new FetcherBuilder()
                .addDisplayPath(stringPath)
                .asTimestamp("MM/dd/yyyy")
                .build();

        return new MappedColumnWithFetcher(name,fetcher);
    }

    public ReportColumn generate0AsBlankColumn(String name, String stringPath) {
        return new MappedColumnWithFetcher(name,
                new FetcherBuilder()
                        .addDisplayPath(stringPath)
                        .sub("0","")
                        .build());
    }

    public ReportColumn generateStaticColumn(String name, String value) {
        return new MappedColumnWithFetcher(name,
                new FetcherBuilder()
                        .setStaticValue(value)
                        .build());
    }

    public ReportColumn generateYesOrBlankColumn(String name, String stringPath) {
        return new MappedColumnWithFetcher(name,
                new FetcherBuilder()
                        .addDisplayPath(stringPath)
                        .sub("NO","")
                        .build());
    }

    public ReportColumn generateYNColumn(String name, String stringPath) {
        return new MappedColumnWithFetcher(name,
                new FetcherBuilder()
                        .addDisplayPath(stringPath)
                        .sub("YES","Y")
                        .sub("NO","N")
                        .build());
    }

    public ReportColumn generateSpeciesColumn(String name, String stringPath) {
        return new MappedColumnWithFetcher(name,
                new FetcherBuilder()
                        .addDisplayPath(stringPath)
                        .sub("LOGGERHEAD","Cc")
                        .sub("GREEN","Cm")
                        .build());
    }
}
