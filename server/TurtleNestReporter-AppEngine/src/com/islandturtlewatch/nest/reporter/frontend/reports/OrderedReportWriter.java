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

import com.google.appengine.repackaged.com.google.common.base.Flag;
import com.google.appengine.repackaged.com.google.storage.onestore.v3.proto2api.OnestoreEntity;
import com.google.common.base.Preconditions;
import com.islandturtlewatch.nest.data.ReportProto;
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
//Lets maybe build the fetcher somewhere else.
  public static class MappedColumnWithFetcher extends ReportColumn{
    public MappedColumnWithFetcher(String name, ValueFetcher fetcher) {
      super(name, fetcher);
    }
  }

  public static class MappedSpeciesColumn extends ReportColumn {
    public MappedSpeciesColumn(String name, final String stringPath) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
          Preconditions.checkNotNull(column, "Missing path: " + stringPath);
          if (column.getValue(rowId).equals("LOGGERHEAD") || column.getValue(rowId).equals("NO ENUM")) {
            return "Cc";
          } else if (column.getValue(rowId).equals("GREEN")) {
            return "Cm";
          } else return "";
        }

      });
    }
  }

  public static class MappedBlankIfUnsetColumn extends ReportColumn {
    public MappedBlankIfUnsetColumn(String name, final String stringPath) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
          Preconditions.checkNotNull(column, "Missing Path: " + stringPath);

          //Check for "UNSET_TYPE or UNSET_REASON"
          if (column.getValue(rowId).equals("UNSET_TYPE") ||
                  column.getValue(rowId).equals("UNSET_REASON") ||
                  column.getValue(rowId).equals("NO ENUM")) {
            return "";
          } else {
            String value = column.getValue(rowId);
            value = value.toLowerCase();
            value = value.replaceAll("_", " ");
            return value.substring(0,1).toUpperCase() + value.substring(1);
          }
        }
      });
    }
  }

  public static class MappedBlankIfUnsetWithOtherColumn extends ReportColumn {
    public MappedBlankIfUnsetWithOtherColumn(String name, final String stringPath, final String otherString) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        private final Path otherPath = new Path(otherString);
        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
          Preconditions.checkNotNull(column, "Missing Path: " + stringPath);
          Column other = columnMap.get(otherPath);
          Preconditions.checkNotNull(other, "Missing Path: " + otherString);

          //Check for "UNSET_TYPE or UNSET_REASON"
          if (column.getValue(rowId).equals("UNSET_TYPE") ||
                  column.getValue(rowId).equals("UNSET_REASON") ||
                  column.getValue(rowId).equals("NO ENUM")) {
            return "";
          } else if (column.getValue(rowId).equals("OTHER")) {
            return other.getValue(rowId);
          } else {
            String value = column.getValue(rowId);
            value = value.toLowerCase();
            value = value.replaceAll("_", " ");
            return value.substring(0,1).toUpperCase() + value.substring(1);

          }
        }
      });
    }
  }

  public static class MappedAnyMatchColumn extends ReportColumn {
    public MappedAnyMatchColumn(String name,
                                final String boolStringPath,
                                final String optionStringPathA,
                                final String optionStringPathB,
                                final ReportProto.NestCondition.WashoutTimeOption optionMatch) {
      super(name, new ValueFetcher() {
        private final Path boolPath = new Path(boolStringPath);
        private final Path optionPathA = new Path(optionStringPathA);
        private final Path optionPathB = new Path(optionStringPathB);
        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column boolColumn = Preconditions.checkNotNull(
                  columnMap.get(boolPath),
                  "Missing Path: " + boolStringPath);
          Column optionAColumn = Preconditions.checkNotNull(
                  columnMap.get(optionPathA),
                  "Missing Path: " + optionStringPathA);
          Column optionBColumn = Preconditions.checkNotNull(
                  columnMap.get(optionPathB),
                  "Missing Path: " + optionStringPathB);
          if (boolColumn.getValue(rowId).equals("YES") ||
                  optionAColumn.getValue(rowId).equals(optionMatch.toString()) ||
                  optionBColumn.getValue(rowId).equals(optionMatch.toString())) {
            return "Yes";
          }

          return "No";
        }
      });
    }
  }
  public static class MappedEitherOrColumn extends ReportColumn {
    public MappedEitherOrColumn(String name, final String stringPathA, final String stringPathB) {
      super(name, new ValueFetcher() {
        private final Path pathA = new Path(stringPathA);
        private final Path pathB = new Path(stringPathB);
        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column columnA = Preconditions.checkNotNull(columnMap.get(pathA), "Missing Path: " + stringPathA);
          Column columnB = Preconditions.checkNotNull(columnMap.get(pathB), "Missing Path: " + stringPathB);
          if (!columnA.getValue(rowId).equals("0") || !columnB.getValue(rowId).equals("0")) {
            return "Yes";
          }
          return "No";
        }
      });
    }
  }

  public static class MappedWashoutTimeOptionColumn extends ReportColumn {
    public MappedWashoutTimeOptionColumn(String name, final String stringPath,
                                         final ReportProto.NestCondition.WashoutTimeOption option,
                                         final String timestampPath) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        private final Path tPath = new Path(timestampPath);
        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column pathColumn = columnMap.get(path);
//          Column timeStampColumn = columnMap.get(tPath);
//          if (timeStampColumn == null) return "No";
//          if (timeStampColumn.getValue(rowId) == null) return "No";
//          if (timeStampColumn.getValue(rowId).equals("") || timeStampColumn.getValue(rowId).equals("0")) return "No";


          if (!pathColumn.hasValue(rowId)) return "No";
          if (pathColumn.getValue(rowId).equals(option.toString())) {
            return "Yes";
          }
          else return "No";
        }
      });
    }
  }

//  optionColumn should take precedence
  public static class MappedExistsOrWashoutTimeColumn extends ReportColumn {
    public MappedExistsOrWashoutTimeColumn(String name, final String eventStringPath,
                                           final String optionStringPath,
                                         final ReportProto.NestCondition.WashoutTimeOption option,
                                           final String timestampPath) {
      super(name, new ValueFetcher() {
        private final Path optionPath = new Path(optionStringPath);
        private final Path eventPath = new Path(eventStringPath);
        private final Path timepath = new Path(timestampPath);
        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column optionColumn = columnMap.get(optionPath);
          Column eventColumn = columnMap.get(eventPath);
          Column timeStampColumn = columnMap.get(timepath);
//          if (timeStampColumn == null) return "No";
//          if (timeStampColumn.getValue(rowId) == null) return "No";
//          if (timeStampColumn.getValue(rowId).equals("") || timeStampColumn.getValue(rowId).equals("0")) return "No";
          String retVal = "No";
          if (eventColumn.hasValue(rowId) && !optionColumn.hasValue(rowId)) {
            retVal = "Yes";
          } else if (optionColumn.getValue(rowId).equals(option.toString())) {
            retVal = "Yes";
          }
        return retVal;
        }

      });
    }
  }

  public static class MappedYesOrBlankRadioColumn extends ReportColumn {
    public MappedYesOrBlankRadioColumn(String name, final String stringPath,
                                       final ReportProto.NestCondition.PreditationEvent.PredationTimeOption option) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
          if (column == null) return "";
          if (column.getValue(rowId) == null) return "";
          Preconditions.checkNotNull(column, "Missing path: " + stringPath);
          if (column.getValue(rowId) == option.toString()) {
            return "Yes";
          }
          else return "";
        }
      });
    }
  }

  public static class MappedYesOrBlankColumn extends ReportColumn {
    public MappedYesOrBlankColumn(String name, final String stringPath) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        @Override public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
//          if (column.getValue(rowId) == null) return "";
          Preconditions.checkNotNull(column, "Missing path: " + stringPath);
//          if (column.getValue(rowId) == "YES") {
//            return column.getValue(rowId);
//          } else return "";
          return column.getValue(rowId) == "YES" ? "Yes" : "";
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
          if (column.getValue(rowId) == null || column.getValue(rowId) == "NO ENUM") return "";
          if (column.getValue(rowId) == "YES") return "Yes";
          if (column.getValue(rowId) == "NO") return "No";
          return column.getValue(rowId);
        }
      });
    }
  }

  public static class MappedProportionColumn extends ReportColumn {
    public MappedProportionColumn(String name, final String stringPath) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        @Override public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
          Preconditions.checkNotNull(column, "Missing path: " + stringPath);
          if (column.getValue(rowId) == null) return "";
          if (column.getValue(rowId) == "NO ENUM") return "ALL";
          return column.getValue(rowId);
        }
      });
    }
  }

  public static class MappedPartialPredationColumn extends ReportColumn {
    public MappedPartialPredationColumn(
            String name,
            final String stringPathA,
            final String stringPathB) {
      super(name, new ValueFetcher() {
        private final Path pathA = new Path(stringPathA);
        private final Path pathB = new Path(stringPathB);
        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column completePredation = Preconditions.checkNotNull(columnMap.get(pathA),"Missing Path: " + stringPathA);

          Column predationTimestamp = columnMap.get(pathB); // I wish you were kotlin.
          boolean predated = false;

//          Take care not to look directly at a null object,
//          for when you gaze too long into the null,
//          The null also gazes into you?
          if ( predationTimestamp != null) predated = predationTimestamp.hasValue(rowId);
          if (predated && !(completePredation.getValue(rowId).equals("YES")) ) return "Yes";
          return "No";
        }
      });
    }
  }


  public static class MappedBlankIfZeroColumn extends ReportColumn {
    public MappedBlankIfZeroColumn(String name, final String stringPath) {
      super (name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
          Preconditions.checkNotNull(column, "Missing path: " + stringPath);
          if (column.getValue(rowId).equals("0")) return "";
          return column.getValue(rowId);
        }
      });
    }
  }

//WARNING: this function does string comparison.
  public static class MappedPredatorColumn extends ReportColumn {
    public MappedPredatorColumn(final String name, final String stringPath) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
          if (column == null) return "";
          if (column.getValue(rowId).equals(name)) {
            return column.getValue(rowId);
          } else return "";
        }
      });
    }
  }

  public static class MappedNullIfNotInventoriedColumn extends ReportColumn {
    public MappedNullIfNotInventoriedColumn(String name, final String countPath,
                                            final String inventoryPath) {
      super(name, new ValueFetcher() {
        private final Path count = new Path(countPath);
        private final Path inventory = new Path(inventoryPath);
        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column countColumn = columnMap.get(count);
          Column inventColumn = columnMap.get(inventory);
          Preconditions.checkNotNull(countColumn,"Missing Path: " + count);
          Preconditions.checkNotNull(inventColumn,"Missing Path: " + inventory);
          if (inventColumn.getValue(rowId) == "NO") {
            return "";
          } else return countColumn.getValue(rowId);
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

          return (!column.getValue(rowId).equals("0")) ? column.getValue(rowId) : defaultVal;
        }
      });
    }
  }
  public static class MappedShortTimestampColumn extends ReportColumn {
    private static DateFormat shortDate = new SimpleDateFormat("MM/dd");
    public MappedShortTimestampColumn(String name, final String stringPath, final String defaultVal) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
          Preconditions.checkNotNull(column, "Missing Path: " + stringPath);
          if (column.getValue(rowId).equals("0") || !column.hasValue(rowId)) {
            return defaultVal;
          } else {
            Long timestamp = Long.parseLong(column.getValue(rowId));
            return shortDate.format(new Date(timestamp));
          }
        }
      });
    }
  }


  // Converts timestamp at path to date.
  // Now with option to set default value to something other than blank
  public static class MappedTimestampColumn extends ReportColumn {
    private static DateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
    private static DateFormat shortDate = new SimpleDateFormat("MM/dd");
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
    public MappedTimestampColumn(String name, final String stringPath, final String defaultVal) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
          Preconditions.checkNotNull(column, "Missing Path: " + stringPath);
          if (column.getValue(rowId).equals("0") || !column.hasValue(rowId)) {
            return defaultVal;
          } else {
            Long timestamp = Long.parseLong(column.getValue(rowId));
            return DATE_FORMAT.format(new Date(timestamp));
          }
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
          if (!columnA.getValue(rowId).equals("")) {
            return columnA.getValue(rowId);
          } else if (!columnB.getValue(rowId).equals("")) {
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
          if (control.getValue(rowId).equals("YES")) {
            return value.getValue(rowId).equals("NO ENUM") ? "" : value.getValue(rowId);
          }
          return "";
        }
      });
    }
  }
  public static class MappedYNColumn extends ReportColumn {
    public MappedYNColumn(String name, final String stringPath) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
          if (column == null) return "No";
          Preconditions.checkNotNull(column, "Missing path: " + stringPath);
          if (column.getValue(rowId) == null) return "No";
          if (column.getValue(rowId) == "YES") {
            return "Yes";
          }else return "No";
        }
      });
    }
  }

  public static class MappedHasTimestampColumn extends ReportColumn {
    public MappedHasTimestampColumn(String name, final String stringPath) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
          if (column == null) return "No";
          if (column.getValue(rowId) == null) return "No";
          Preconditions.checkNotNull(column, "Missing Path: " + stringPath);
          if (column.getValue(rowId).equals("") || column.getValue(rowId).equals("0")) return "No";
          else return column.hasValue(rowId) ? "Yes" : "No";
        }
      });
    }
  }


  public static class MappedHasTimestampYOrBlankColumn extends ReportColumn {
    public MappedHasTimestampYOrBlankColumn(String name, final String stringPath) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
          if (column.getValue(rowId) == null) return "";
          Preconditions.checkNotNull(column, "Missing Path: " + stringPath);
          if (column.getValue(rowId).equals("") || column.getValue(rowId).equals("0")) return "";
          else return column.hasValue(rowId) ? "Yes" : "";
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
          return column.hasValue(rowId) ? "Yes" : "No";
        }
      });
    }
  }
//checks to see if the given path exists
  //TODO: (DWenzel) find a less hacky way of doing this.
  public static class MappedNotNullColumn extends ReportColumn {
    public MappedNotNullColumn(String name, final String stringPath) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
          if (column == null) return "No";
          else return column.hasValue(rowId) ? "Yes" : "No";
        }
      });
    }
  }

  public static class MappedNotNullTimestampColumn extends ReportColumn {
    private static DateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
    public MappedNotNullTimestampColumn(String name, final String stringPath) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
         Column column = columnMap.get(path);
          if (column == null) return "";
          else if (column.getValue(rowId).equals("0") || column.getValue(rowId).equals("")) {
            return "";
          } else {
            Long timestamp = Long.parseLong(column.getValue(rowId));


            return DATE_FORMAT.format(new Date(timestamp));
          }



        }
      });
    }
  }

  public static class MappedIfExistsColumn extends ReportColumn {
    public MappedIfExistsColumn(String name, final String stringPath) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        @Override public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
          if (column == null) return "";
          else return column.getValue(rowId);
        }
      });
    }

  }


  public static class MappedIsPresentYNColumn extends ReportColumn {
    public MappedIsPresentYNColumn(String name, final String stringPath) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        @Override public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
          Preconditions.checkNotNull(column, "Missing path: " + stringPath);
          return column.hasValue(rowId) ? "Yes" : "No";
        }
      });
    }
  }


  public static class MappedIsPresentYesOrBlankColumn extends ReportColumn {
    public MappedIsPresentYesOrBlankColumn(String name, final String stringPath) {
      super(name, new ValueFetcher() {
        private final Path path = new Path(stringPath);
        @Override public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column column = columnMap.get(path);
          if (column.getValue(rowId) == null) return "";
          Preconditions.checkNotNull(column, "Missing path: " + stringPath);
          return column.hasValue(rowId) ? "Yes" : "";
        }
      });
    }
  }


  public static class MappedComboColumn extends ReportColumn {
    private static Pattern USER_PATTERN = Pattern.compile("section([0-9]+)@islandturtlewatch.com");
    public MappedComboColumn(final String name, final String sPathA, final String sPathB) {
      super(name, new ValueFetcher() {
        private final Path pathA = new Path(sPathA);
        private final Path pathB = new Path(sPathB);
        private final MappedSectionColumn foo = new MappedSectionColumn(name,sPathA);

        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column columnA = columnMap.get(pathA);
          Column columnB = columnMap.get(pathB);
          Preconditions.checkNotNull(columnA, "Missing path: " + pathA );
          Preconditions.checkNotNull(columnB, "Missing path: " + pathB );
          String stringB = columnB.getValue(rowId);
          return foo.getFetcher().fetch(columnMap, rowId) + " / " + stringB + ".";
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
//          boolean hasAbandonedBodyPit = Boolean.valueOf(abandonedBodyPit.getValue(rowId));

          Column abandonedEggCavities =
              Preconditions.checkNotNull(columnMap.get(abandonedEggCavitiesPath),
                      "Missing path: " + abandonedEggCavitiesPathStr);

          //Please Note: this DOES NOT work, these values are YES/NO not true/false
//          boolean hasAbandonedEggCavities = Boolean.valueOf(abandonedEggCavities.getValue(rowId));


          if (abandonedEggCavities.getValue(rowId).equals("YES")) {
            return "Egg Chamber";
          } else if (abandonedBodyPit.getValue(rowId).equals("YES")) {
            return "Body Pit";
          } else
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

          Column relocated = columnMap.get(relocatedPath);
                  Preconditions.checkNotNull(columnMap.get(relocatedPath),
              "Missing path: " + relocatedPathStr);
          boolean wasRelocated = (relocated.getValue(rowId) == "YES");

          Column dateFound = Preconditions.checkNotNull(columnMap.get(dateFoundPath),
              "Missing path: " + dateFoundPathStr);
          Column dateProtected = Preconditions.checkNotNull(columnMap.get(dateProtectedPath),
              "Missing path: " + dateProtectedPathStr);
          Column protectionEvent = columnMap.get(protectionEventPath);
                  Preconditions.checkNotNull(protectionEvent,
              "Missing path: " + protectionEventPathStr);

          // If dates are not within one day of each other this doesn't apply.
          Long dateDiff = Long.parseLong(dateProtected.getValue(rowId)) -
              Long.parseLong(dateFound.getValue(rowId));
          String protectionType = protectionEvent.getValue(rowId);
          if (dateDiff <= dayInMs) {
            if (protectionType.equals("UNSET_TYPE")) {
              return wasRelocated ? "E" : "A";
            }
             if (protectionType.equals("SELF_RELEASING_FLAT")) {
              return  wasRelocated ? "F" : "B";
            }
             if (protectionType.equals("SELF_RELEASING_CAGE")) {
              return wasRelocated ? "G" : "C";
            }
           if (protectionType.equals("RESTRAINING_CAGE")) {
              return wasRelocated ? "H" : "D";
            }
          }

          return wasRelocated ? "E" : "A";
        }
      });
    }
  }

  public static class FinalTreatmentColumn extends ReportColumn {
    public FinalTreatmentColumn(
            final String name,
            final String protectionChangeStr,
            final String relocatedPathStr,
            final String protectionEventStr) {

      super(name, new ValueFetcher() {
        private final Path relocatedPath = new Path(relocatedPathStr);
        private final Path protectionChangePath = new Path(protectionChangeStr);
        private final Path protectionEventPath = new Path(protectionEventStr);
        @Override
        public String fetch(Map<Path, Column> columnMap, int rowId) {
          Column protectionChangeColumn = Preconditions.checkNotNull(columnMap.get(protectionChangePath));
          Column protectionType = Preconditions.checkNotNull(columnMap.get(protectionEventPath));
          Column relocatedColumn = Preconditions.checkNotNull(columnMap.get(relocatedPath));

          boolean wasRelocated = relocatedColumn.getValue(rowId) == "YES";
          boolean protectionChanged = protectionChangeColumn.hasValue(rowId) &&
                  !protectionChangeColumn.getValue(rowId).equals("0");

          if (protectionChanged) {
            String protectionEvent = protectionType.getValue(rowId);
            if (protectionEvent.equals("UNSET_TYPE")) {
              return wasRelocated ? "E" : "A";
            }
            if (protectionEvent.equals("SELF_RELEASING_FLAT")) {
              return  wasRelocated ? "F" : "B";
            }
            if (protectionEvent.equals("SELF_RELEASING_CAGE")) {
              return wasRelocated ? "G" : "C";
            }
            if (protectionEvent.equals("RESTRAINING_CAGE")) {
              return wasRelocated ? "H" : "D";
            }
          }
          return "";
        }
      });
    }
  }

  // Determines final treatment.
//  public static class nFinalTreatmentColumn extends ReportColumn {
//    private static final long dayInMs = 86400000L;
//    public nFinalTreatmentColumn(
//        final String name,
//        final String relocatedPathStr,
//        final String dateFoundPathStr,
//        final String dateProtectedPathStr,
//        final String protectionEventPathStr) {
//
//      super(name, new ValueFetcher() {
//        private final Path relocatedPath = new Path(relocatedPathStr);
//        private final Path dateFoundPath = new Path(dateFoundPathStr);
//        private final Path dateProtectedPath = new Path(dateProtectedPathStr);
//        private final Path protectionEventPath = new Path(protectionEventPathStr);
//        private final InitialTreatmentColumn initialColumn = new InitialTreatmentColumn(name,
//            relocatedPathStr, dateFoundPathStr, dateProtectedPathStr, protectionEventPathStr);
//
//        @Override
//        public String fetch(Map<Path, Column> columnMap, int rowId) {
//          Column relocated = Preconditions.checkNotNull(columnMap.get(relocatedPath),
//              "Missing path: " + relocatedPathStr);
//          boolean wasRelocated = (relocated.getValue(rowId) == "YES");
//
//
//          Column dateFound = Preconditions.checkNotNull(columnMap.get(dateFoundPath),
//              "Missing path: " + dateFoundPathStr);
//          Column dateProtected = Preconditions.checkNotNull(columnMap.get(dateProtectedPath),
//              "Missing path: " + dateProtectedPathStr);
//          Column protectionEvent = Preconditions.checkNotNull(columnMap.get(protectionEventPath),
//              "Missing path: " + protectionEventPathStr);
//
//          // If dates are within one day of each other this doesn't apply.
//          Long dateDiff = Long.parseLong(dateProtected.getValue(rowId)) -
//              Long.parseLong(dateFound.getValue(rowId));
//          String protectionType = protectionEvent.getValue(rowId);
//          if (dateDiff > dayInMs) {
//            if (protectionType.equals("UNSET_TYPE")) {
//              return wasRelocated ? "E" : "A";
//            }
//            if (protectionType.equals("SELF_RELEASING_FLAT")) {
//              return  wasRelocated ? "F" : "B";
//            }
//            if (protectionType.equals("SELF_RELEASING_CAGE")) {
//              return wasRelocated ? "G" : "C";
//            }
//            if (protectionType.equals("RESTRAINING_CAGE")) {
//              return wasRelocated ? "H" : "D";
//            }
//          }
//          return "";
////          return initialColumn.getFetcher().fetch(columnMap, rowId);
//        }
//      });
//    }
//  }
}