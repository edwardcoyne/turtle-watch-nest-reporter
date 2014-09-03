package com.islandturtlewatch.nest.reporter.frontend.reports;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;

import com.google.common.base.Preconditions;
import com.islandturtlewatch.nest.reporter.frontend.reports.ReportCsvGenerator.Column;
import com.islandturtlewatch.nest.reporter.frontend.reports.ReportCsvGenerator.Path;

public class OrderedReportWriter implements ReportCsvGenerator.ReportWriter {
  private final Iterable<ReportColumn> reportColumns;
  private final RowFilter rowFilter;

  // Section column should be part of reportColumns too.
  public OrderedReportWriter(Iterable<ReportColumn> reportColumns, RowFilter rowFilter) {
    this.reportColumns = reportColumns;
    this.rowFilter = rowFilter;
  }

  @Override
  public void writeHeader(Writer writer, Iterable<Path> columns) throws IOException {
    List<String> cells = new ArrayList<>();
    for (ReportColumn column : reportColumns) {
      cells.add(column.getName());
    }
    writer.append(ReportCsvGenerator.csvJoiner.join(cells)).append('\n');
  }

  @Override
  public void writeRow(Writer writer, Map<Path, Column> columnMap, int rowId) throws IOException {
    if (rowFilter.shouldWriteRow(columnMap, rowId)) {
      return;
    }
    //TODO(edcoyne): split nests and false crawls.

    List<String> cells = new ArrayList<>();
    for (ReportColumn column : reportColumns) {
      cells.add(column.getFetcher().fetch(columnMap, rowId));
    }
    writer.append(ReportCsvGenerator.csvJoiner.join(cells)).append('\n');
  }

  public interface ValueFetcher {
    public String fetch(Map<Path, Column> columnMap, int rowId);
  }

  public interface RowFilter {
    public boolean shouldWriteRow(Map<Path, Column> columnMap, int rowId);
  }

  public static class ReportColumn {
    @Getter
    private final String name;
    @Getter
    private final ValueFetcher fetcher;
    private ReportColumn(String name, ValueFetcher fetcher) {
      this.name = name;
      this.fetcher = fetcher;
    }
  }

  // Returns a default value for column.
  public static class StaticValueColumn extends ReportColumn{
    public StaticValueColumn(String name, final String value) {
      super(name, new ValueFetcher() {
        @Override public String fetch(Map<Path, Column> columnMap, int rowId) {
          return value;
        }
      });
    }
  }

  public static class MappedColumn extends ReportColumn {
    public MappedColumn(String name, final String stringPath) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        @Override public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
          Preconditions.checkNotNull(column, "Missing path: " + stringPath);
          return column.getValue(rowId);
        }
      });
    }
  }


  // Converts timestamp at path to date.
  public static class MappedTimestampColumn extends ReportColumn {
    private static DateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
    public MappedTimestampColumn(String name, final String stringPath) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        @Override public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
          Preconditions.checkNotNull(column, "Missing path: " + stringPath);
          if (!column.hasValue(rowId) || column.getValue(rowId).equals("0")) {
            return "";
          }
          Long timestamp = Long.parseLong(column.getValue(rowId));
          return DATE_FORMAT.format(new Date(timestamp));
        }
      });
    }
  }

  // Value will be YES if there is a value at the path.
  public static class MappedIsPresentColumn extends ReportColumn {
    public MappedIsPresentColumn(String name, final String stringPath) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        @Override public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
          Preconditions.checkNotNull(column, "Missing path: " + stringPath);
          return column.hasValue(rowId) ? "YES" : "NO";
        }
      });
    }
  }

  // Column will read section number out of submitting user.
  public static class MappedSectionColumn extends ReportColumn {
    private static Pattern USER_PATTERN = Pattern.compile("section([0-9]+)@islandturtlewatch.com");
    public MappedSectionColumn(String name, final String stringPath) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);

        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
          Preconditions.checkNotNull(column, "Missing path: " + stringPath);
          String user = column.getValue(rowId);
          Matcher matcher = USER_PATTERN.matcher(user);
          if (!matcher.matches()) {
            return "";
          }
          return matcher.group(1);
        }
      });
    }
  }

  // Converts values at stringPathFt and stirngPathIn to Ft.In like 2.01.
  public static class MappedDistanceColumn extends ReportColumn {
    public MappedDistanceColumn(
        String name, final String stringPathFt, final String stringPathIn) {
      super(name, new ValueFetcher() {
        private final Path pathFt = new Path(stringPathFt);
        private final Path pathIn = new Path(stringPathIn);
        @Override public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column columnFt = Preconditions.checkNotNull(columnMap.get(pathFt),
              "Missing path: " + stringPathFt);
          Column columnIn = Preconditions.checkNotNull(columnMap.get(pathIn),
              "Missing path: " + stringPathIn);
          return String.format("%s.%02d", columnFt.getValue(rowId),
              columnIn.hasValue(rowId) ? Integer.parseInt(columnIn.getValue(rowId)) : 0);
        }
      });
    }
  }
}