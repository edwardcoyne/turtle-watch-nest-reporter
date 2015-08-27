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
import com.islandturtlewatch.nest.data.ReportProto.Intervention.ProtectionEvent;
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
    if (!rowFilter.shouldWriteRow(columnMap, rowId)) {
      return;
    }

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

  public static class MappedColumnAbsoluteValueDouble extends ReportColumn {
    public MappedColumnAbsoluteValueDouble(String name, final String stringPath) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        @Override public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
          Preconditions.checkNotNull(column, "Missing path: " + stringPath);

          double value = Double.parseDouble(column.getValue(rowId));
          return Double.toString(Math.abs(value));
        }
      });
    }
  }

  public static class MappedColumnWithDefault extends ReportColumn {
    public MappedColumnWithDefault(String name, final String stringPath, final String defaultVal) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        @Override public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
          Preconditions.checkNotNull(column, "Missing path: " + stringPath);

          return column.hasValue(rowId) ? column.getValue(rowId) : defaultVal;
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

  public static class MappedPriorityColumn extends ReportColumn {
    public MappedPriorityColumn (String name, final String aStringPath,
                                 final String bStringPath, final String defaultString) {
      super(name, new ValueFetcher() {
        private final Path aPath = new Path(aStringPath);
        private final Path bPath = new Path(bStringPath);
        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column columnA = columnMap.get(aPath);
          Column columnB = columnMap.get(bPath);
          Preconditions.checkNotNull(columnA, "Missing path: " + aStringPath);
          Preconditions.checkNotNull(columnB, "Missing path: " + bStringPath);
          if (columnA.hasValue(rowId)) {
            return columnA.getValue(rowId);
          } else if (columnB.hasValue(rowId)) {
            return columnB.getValue(rowId);
          } else {
            return defaultString;
          }
        }
      });

    }
  }

  // Will output the value at valuePath if the value at controlPath is true.
  public static class ConditionallyMappedColumn extends ReportColumn {
    public ConditionallyMappedColumn(String name,
                                     final String controlPathStr, final String valuePathStr) {
      super(name, new ValueFetcher() {
        private final Path controlPath = new Path(controlPathStr);
        private final Path valuePath = new Path(valuePathStr);
        @Override public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column control = Preconditions.checkNotNull(columnMap.get(controlPath),
              "Missing path: " + controlPathStr);
          Column value = Preconditions.checkNotNull(columnMap.get(valuePath),
              "Missing path: " + controlPathStr);
          if (Boolean.parseBoolean(control.getValue(rowId))) {
            return value.getValue(rowId);
          }
          return "";
        }
      });
    }
  }
    //converts Yes and No to Y and N
  public static class MappedYesNoColumn extends ReportColumn {
    public MappedYesNoColumn(String name, final String stringPath) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
          Preconditions.checkNotNull(column, "Missing path: " + stringPath);
          if (column.getValue(rowId) == "Yes") {
            return "Y";
          }else return "N";
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
          //changed from YES : NO to coincide with FWC Reporting requirements
          return column.hasValue(rowId) ? "Y" : "N";
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

  // Determines final Activity.
  public static class FinalActivityColumn extends ReportColumn {
    public FinalActivityColumn(
        String name,
        final String abandonedBodyPitPathStr,
        final String abandonedEggCavitiesPathStr) {
      super(name, new ValueFetcher() {
        private final Path abandonedBodyPitPath = new Path(abandonedBodyPitPathStr);
        private final Path abandonedEggCavitiesPath = new Path(abandonedEggCavitiesPathStr);
        @Override public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column abandonedBodyPit = Preconditions.checkNotNull(columnMap.get(abandonedBodyPitPath),
              "Missing path: " + abandonedBodyPitPathStr);
          boolean hasAbandonedBodyPit = Boolean.valueOf(abandonedBodyPit.getValue(rowId));

          Column abandonedEggCavities =
              Preconditions.checkNotNull(columnMap.get(abandonedEggCavitiesPath),
              "Missing path: " + abandonedEggCavitiesPathStr);
          boolean hasAbandonedEggCavities = Boolean.valueOf(abandonedEggCavities.getValue(rowId));

          if (hasAbandonedBodyPit) {
            return "Abandoned Body Pit";
          }
          if (hasAbandonedEggCavities) {
            return "Abandoned Egg Chamber";
          }

          return "No Digging";
        }
      });
    }
  }

  // Determines initial treatment.
  public static class InitialTreatmentColumn extends ReportColumn {
    private static final long dayInMs = 86400000l;
    public InitialTreatmentColumn(
        String name,
        final String relocatedPathStr,
        final String dateFoundPathStr,
        final String dateProtectedPathStr,
        final String protectionEventPathStr) {
      super(name, new ValueFetcher() {
        private final Path relocatedPath = new Path(relocatedPathStr);
        private final Path dateFoundPath = new Path(dateFoundPathStr);
        private final Path dateProtectedPath = new Path(dateProtectedPathStr);
        private final Path protectionEventPath = new Path(protectionEventPathStr);
        @Override public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column relocated = Preconditions.checkNotNull(columnMap.get(relocatedPath),
              "Missing path: " + relocatedPathStr);
          boolean wasRelocated = Boolean.valueOf(relocated.getValue(rowId));

          Column dateFound = Preconditions.checkNotNull(columnMap.get(dateFoundPath),
              "Missing path: " + dateFoundPathStr);
          Column dateProtected = Preconditions.checkNotNull(columnMap.get(dateProtectedPath),
              "Missing path: " + dateProtectedPathStr);
          Column protectionEvent = Preconditions.checkNotNull(columnMap.get(protectionEventPath),
              "Missing path: " + protectionEventPathStr);

          // If dates are not within one day of each other this doesn't apply.
          Long dateDiff = Long.parseLong(dateProtected.getValue(rowId)) -
              Long.parseLong(dateFound.getValue(rowId));
          if (dateDiff < dayInMs) {
            if (protectionEvent.equals(ProtectionEvent.Type.UNSET_TYPE)) {
              return wasRelocated ? "E" : "A";
            }
            if (protectionEvent.equals(ProtectionEvent.Type.SELF_RELEASING_FLAT)) {
              return wasRelocated ? "F" : "B";
            }
            if (protectionEvent.equals(ProtectionEvent.Type.SELF_RELEASING_CAGE)) {
              return wasRelocated ? "G" : "C";
            }
            if (protectionEvent.equals(ProtectionEvent.Type.RESTRAINING_CAGE)) {
              return wasRelocated ? "H" : "D";
            }
          }
          return "";
        }
      });
    }
  }

  // Determines final treatment.
  public static class FinalTreatmentColumn extends ReportColumn {
    private static final long dayInMs = 86400000l;
    public FinalTreatmentColumn(
        final String name,
        final String relocatedPathStr,
        final String dateFoundPathStr,
        final String dateProtectedPathStr,
        final String protectionEventPathStr) {
      super(name, new ValueFetcher() {
        private final Path relocatedPath = new Path(relocatedPathStr);
        private final Path dateFoundPath = new Path(dateFoundPathStr);
        private final Path dateProtectedPath = new Path(dateProtectedPathStr);
        private final Path protectionEventPath = new Path(protectionEventPathStr);
        private final InitialTreatmentColumn initialColumn = new InitialTreatmentColumn(name,
            relocatedPathStr, dateFoundPathStr, dateProtectedPathStr, protectionEventPathStr);
        @Override public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column relocated = Preconditions.checkNotNull(columnMap.get(relocatedPath),
              "Missing path: " + relocatedPathStr);
          boolean wasRelocated = Boolean.valueOf(relocated.getValue(rowId));

          Column dateFound = Preconditions.checkNotNull(columnMap.get(dateFoundPath),
              "Missing path: " + dateFoundPathStr);
          Column dateProtected = Preconditions.checkNotNull(columnMap.get(dateProtectedPath),
              "Missing path: " + dateProtectedPathStr);
          Column protectionEvent = Preconditions.checkNotNull(columnMap.get(protectionEventPath),
              "Missing path: " + protectionEventPathStr);

          // If dates are within one day of each other this doesn't apply.
          Long dateDiff = Long.parseLong(dateProtected.getValue(rowId)) -
              Long.parseLong(dateFound.getValue(rowId));
          if (dateDiff > dayInMs) {
            if (protectionEvent.equals(ProtectionEvent.Type.UNSET_TYPE)) {
              return wasRelocated ? "E" : "A";
            }
            if (protectionEvent.equals(ProtectionEvent.Type.SELF_RELEASING_FLAT)) {
              return wasRelocated ? "F" : "B";
            }
            if (protectionEvent.equals(ProtectionEvent.Type.SELF_RELEASING_CAGE)) {
              return wasRelocated ? "G" : "C";
            }
            if (protectionEvent.equals(ProtectionEvent.Type.RESTRAINING_CAGE)) {
              return wasRelocated ? "H" : "D";
            }
          }
          return initialColumn.getFetcher().fetch(columnMap, rowId);
        }
      });
    }
  }
}