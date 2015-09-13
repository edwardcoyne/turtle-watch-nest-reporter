package com.islandturtlewatch.nest.reporter.web.servlets;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.java.Log;

import com.google.common.collect.ImmutableList;
import com.google.common.net.MediaType;
import com.islandturtlewatch.nest.reporter.backend.storage.ReportStore;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedDistanceColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedSectionColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedTimestampColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.ReportColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.RowFilter;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.StaticValueColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.ReportCsvGenerator;
import com.islandturtlewatch.nest.reporter.frontend.reports.ReportCsvGenerator.Column;
import com.islandturtlewatch.nest.reporter.frontend.reports.ReportCsvGenerator.Path;

@Log
public class StateFalseCrawlReportServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  // Keep this column separate so we can test against it.
  private static ReportColumn sectionColumn = new MappedSectionColumn("Beach Zone", "ref.owner_id");

  // This is the list of columns in the report, they will appear in this order.
  private static List<ReportColumn> reportColumns = ImmutableList.of(
      new MappedTimestampColumn("Date Crawl Recorded", "report.timestamp_found_ms"),
      new MappedColumn("Species","report.species"),
      new StaticValueColumn("Crawl within Project Area? ", "YES"),
      new MappedColumn("City","report.location.city"),
      new MappedColumn("Address","report.location.street_address"),
      new MappedColumn("Escarpment >= 18 Encountered", "report.location.escarpment_over_18_inches"),
      new OrderedReportWriter.FinalActivityColumn("Final Activity",
              "report.condition.abandoned_body_pit",
              "report.condition.abandoned_egg_cavity") ,
      new MappedDistanceColumn("Distance From Dune",
          "report.location.apex_to_barrier_ft", "report.location.apex_to_barrier_in"),
      new MappedDistanceColumn("Distance From MHW",
          "report.location.water_to_apex_ft", "report.location.water_to_apex_in"),
      new MappedColumn("ID/Label", "report.false_crawl_number"),
      sectionColumn,
      new MappedColumn("Additional Notes","report.additional_notes"),
      new MappedColumn("Latitude", "report.location.coordinates.lat"),
      new OrderedReportWriter.MappedColumnAbsoluteValueDouble("Longitude", "report.location.coordinates.long"));

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    log.info("Generating False Crawl report, no auth.");
    ReportStore store = new ReportStore();
    store.init();

    response.setContentType(MediaType.CSV_UTF_8.toString());
    ReportCsvGenerator generator = new ReportCsvGenerator(
        new OrderedReportWriter(reportColumns, new Filter()));
    generator.addAllRows(store.getActiveReports());

    response.setHeader("content-disposition",
        "inline; filename=\"nest_reporter_state_false_crawl_report_"+
            new Date().toString() + ".csv\"");
    response.setCharacterEncoding("UTF-8");
    ServletOutputStream outputStream = response.getOutputStream();
    OutputStreamWriter writer = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"));
    writer.write('\ufeff');
    writer.flush();
    generator.write(writer);
  }

  private class Filter implements RowFilter {
    @Override
    public boolean shouldWriteRow(Map<Path, Column> columnMap, int rowId) {
      // If there is no section number it is a junk report.
      boolean hasSection = !sectionColumn.getFetcher().fetch(columnMap, rowId).equals("");

      String falseCrawl = columnMap.get(new Path("report.false_crawl_number")).getValue(rowId);
      boolean isFalseCrawl = !falseCrawl.equals("") && !falseCrawl.equals("0");
      return hasSection && isFalseCrawl;
    }
  }
}