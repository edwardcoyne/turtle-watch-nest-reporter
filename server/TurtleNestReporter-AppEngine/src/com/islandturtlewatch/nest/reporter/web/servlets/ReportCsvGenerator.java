package com.islandturtlewatch.nest.reporter.web.servlets;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.islandturtlewatch.nest.data.ReportProto.Image;
import com.islandturtlewatch.nest.data.ReportProto.ReportWrapper;
import com.islandturtlewatch.nest.data.ReportProto.ReportWrapper.Builder;

@Log
class ReportCsvGenerator {
  public static Joiner csvJoiner = Joiner.on(";");
  private static Joiner pathJoiner = Joiner.on(".");
  private static Splitter pathSplitter = Splitter.on(".");

  private int currentRow = 0;
  private final Map<Path, Column> columnMap = new TreeMap<>();
  private final ReportWriter reportWriter;

  public ReportCsvGenerator() {
    this.reportWriter = new ReportWriterAll();
  }

  public ReportCsvGenerator(ReportWriter reportWriter) {
    this.reportWriter = reportWriter;
  }

  public void addAllRows(Iterable<ReportWrapper> wrappers) {
    int ct = 0;
    for (ReportWrapper wrapper : wrappers) {
      addRow(wrapper);
      ct++;
    }

    log.info("Added " + ct + " reports.");
  }

  public void addRow(ReportWrapper  wrapper) {
    //TODO(edcoyne): why are these not active?
    //Preconditions.checkArgument(wrapper.getActive());
    addImages(wrapper.getReport().getImageList(), wrapper.getRef().getReportId());
    Builder updatedWrapper = wrapper.toBuilder();
    updatedWrapper.getReportBuilder().clearImage();
    processValues(new Path(), updatedWrapper.build(), wrapper.getDescriptorForType());
    currentRow++;
  }

  private void addImages(Iterable<Image> images, long reportId) {
    int ordinal = 0;
    for (Image image : images) {
      Path path = new Path().add("report").add("image").add(++ordinal);
      String value = "http://1-dot-static-sentinel-567.appspot.com/web/image/"
          + reportId + "/" + image.getFileName();

      if (!columnMap.containsKey(path)) {
        columnMap.put(path, new Column());
      }
      columnMap.get(path).addRowData(currentRow, value);
    }
  }

  private void processValues(Path path, Message message, Descriptor messageDescriptor) {
    for (FieldDescriptor field : messageDescriptor.getFields()) {
      processValues(path.add(field), message, field);
    }
  }

  private void processValues(Path path, Message message, FieldDescriptor field) {
    if (field.getType() == FieldDescriptor.Type.MESSAGE) {
      if (field.isRepeated()) {
        int ordinal = 0;
        @SuppressWarnings("unchecked")
        Iterable<Message> repeatedMessages = (List<Message>)message.getField(field);
        for (Message repeatedMessage : repeatedMessages) {
          processValues(path.add(ordinal++), repeatedMessage, field.getMessageType());
        }
        return;
      }
      processValues(path, (Message)message.getField(field), field.getMessageType());
      return;
    }

    if (!columnMap.containsKey(path)) {
      columnMap.put(path, new Column());
    }
    String value = "";
    if (field.getType() == FieldDescriptor.Type.ENUM) {
      EnumValueDescriptor enumValue = (EnumValueDescriptor)message.getField(field);
      value = enumValue.getName();
    } else {
      value = sanatize(message.getField(field));
    }
    columnMap.get(path).addRowData(currentRow, value);
  }

  private String sanatize(Object value) {
    String result = "";
    if (value instanceof String) {
      result = value.toString().replace('\n', ' ').replace(';', ' ');
    } else {
      result = value.toString();
    }
    return result;
  }

  public void write(Writer writer) throws IOException {
    writer.append("SEP=;\n");
    reportWriter.writeHeader(writer, columnMap.keySet());

    int ct = 0;
    for (ct = 0; ct <= currentRow; ct++) {
      reportWriter.writeRow(writer, columnMap, ct);
    }

    log.info("Wrote " + ct + " reports.");
  }

  public static class Column {
    private final Map<Integer, String> rowValues = new HashMap<>();

    private Column() {}

    private void addRowData(int rowId, String value) {
      rowValues.put(rowId, value);
    }

    public String getValue(int rowId) {
      if (rowValues.containsKey(rowId)) {
        return rowValues.get(rowId);
      }
      return "";
    }

    public boolean hasValue(int rowId) {
      return rowValues.containsKey(rowId);
    }
  }

  @EqualsAndHashCode
  public static class Path implements Comparable<Path>{
    private final List<String> path = new ArrayList<>();

    public Path() { }

    public Path(String stringPath) {
      for (String element : pathSplitter.split(stringPath)) {
        this.path.add(element);
      }
    }

    private Path(Path path, String entry) {
      this.path.addAll(path.path);
      this.path.add(entry);
    }

    public Path add(int repeatedOrdinal) {
      return new Path(this, Integer.toString(repeatedOrdinal));
    }

    public Path add(String element) {
      return new Path(this, element);
    }

    public Path add(FieldDescriptor field) {
      return new Path(this, field.getName());
    }

    @Override
    public String toString() {
      return pathJoiner.join(path);
    }

    @Override
    public int compareTo(Path o) {
      for (int i = 0; i < path.size(); i++) {
        if (o.path.size() <= i) {
          return -1;
        }
        int result = path.get(i).compareTo(o.path.get(i));
        if (result != 0) {
          return result;
        }
      }
      return 0;
    }
  }

  public interface ReportWriter {
    public void writeHeader(Writer writer, Iterable<Path> columns) throws IOException;
    // TODO(edcoyne): columnmap is ugly here, should just pass data we need.
    public void writeRow(Writer writer, Map<Path, Column> columnMap, int rowId) throws IOException;
  }

  public class ReportWriterAll implements ReportWriter {
    @Override
    public void writeHeader(Writer writer, Iterable<Path> columns) throws IOException {
      List<String> cells = new ArrayList<>();
      for (Path entry : columns) {
        cells.add(entry.toString());
      }
      writer.append(csvJoiner.join(cells)).append('\n');
    }

    @Override
    public void writeRow(Writer writer, Map<Path, Column> columnMap, int rowId) throws IOException {
      List<String> cells = new ArrayList<>();
      for (Entry<Path, Column> entry : columnMap.entrySet()) {
        cells.add(entry.getValue().getValue(rowId));
      }
      writer.append(csvJoiner.join(cells)).append('\n');
    }
  }
}