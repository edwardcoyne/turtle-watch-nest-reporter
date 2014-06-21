package com.islandturtlewatch.nest.reporter.web.servlets;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.EqualsAndHashCode;
import lombok.extern.java.Log;

import com.google.common.base.Joiner;
import com.google.common.net.MediaType;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.islandturtlewatch.nest.data.ReportProto.Image;
import com.islandturtlewatch.nest.data.ReportProto.ReportWrapper;
import com.islandturtlewatch.nest.data.ReportProto.ReportWrapper.Builder;
import com.islandturtlewatch.nest.reporter.backend.storage.ReportStore;

/**
 * Will generate csv of all active reports.
 */
@Log
public class CsvServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private static Joiner csvJoiner = Joiner.on(";");
  private static Joiner pathJoiner = Joiner.on(".");


  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    log.info("Generating Csv report, no auth.");
    ReportStore store = new ReportStore();
    store.init();

    response.setContentType(MediaType.CSV_UTF_8.toString());
    //response.setContentType(MediaType.PLAIN_TEXT_UTF_8.toString());
    ReportCsvGenerator generator = new ReportCsvGenerator();
    generator.addAllRows(store.getActiveReports());

    response.setHeader("content-disposition",
        "inline; filename=\"nest_reporter_reports_" + new Date().toString() + ".csv\"");
    response.setCharacterEncoding("UTF-8");
    ServletOutputStream outputStream = response.getOutputStream();
    OutputStreamWriter writer = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"));
    writer.write('\ufeff');
    writer.flush();
    generator.write(writer);
  }

  private static class ReportCsvGenerator {
    private int currentRow = 0;
    private final Map<Path, Column> columnMap = new TreeMap<>();

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
      writeHeader(writer);

      int ct = 0;
      for (ct = 0; ct <= currentRow; ct++) {
        writeRow(writer, ct);
      }

      log.info("Wrote " + ct + " reports.");
    }

    private void writeHeader(Writer writer) throws IOException {

      List<String> cells = new ArrayList<>();
      for (Entry<Path, Column> entry : columnMap.entrySet()) {
        cells.add(entry.getKey().toString());
      }
      writer.append(csvJoiner.join(cells)).append('\n');
    }

    private void writeRow(Writer writer, int rowId) throws IOException {
      List<String> cells = new ArrayList<>();
      for (Entry<Path, Column> entry : columnMap.entrySet()) {
        cells.add(entry.getValue().getValue(rowId));
      }
      writer.append(csvJoiner.join(cells)).append('\n');
    }

    private class Column {
      private final Map<Integer, String> rowValues = new HashMap<>();

      private Column() {}

      private void addRowData(int rowId, String value) {
        rowValues.put(rowId, value);
      }

      private String getValue(int rowId) {
        if (rowValues.containsKey(rowId)) {
          return rowValues.get(rowId);
        }
        return "";
      }
    }

    @EqualsAndHashCode
    private class Path implements Comparable<Path>{
      private final List<String> path = new ArrayList<>();

      public Path() { }

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
  }
}
