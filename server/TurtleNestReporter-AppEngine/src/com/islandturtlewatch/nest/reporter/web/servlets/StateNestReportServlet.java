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
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedColumnAbsoluteValueDouble;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedColumnWithDefault;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedDistanceColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedIsPresentColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedSectionColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedTimestampColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.ReportColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.FinalActivityColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.RowFilter;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.StaticValueColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.InitialTreatmentColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.FinalTreatmentColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.ConditionallyMappedColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.ReportCsvGenerator;
import com.islandturtlewatch.nest.reporter.frontend.reports.ReportCsvGenerator.Column;
import com.islandturtlewatch.nest.reporter.frontend.reports.ReportCsvGenerator.Path;

@Log
public class StateNestReportServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  // Keep this column separate so we can test against it.
  private static ReportColumn sectionColumn = new MappedSectionColumn("Beach Zone", "ref.owner_id");

  // This is the list of columns in the report, they will appear in this order.
  private static List<ReportColumn> reportColumns = ImmutableList.of(
      new MappedTimestampColumn("Date Nest Recorded", "report.timestamp_found_ms"),
      new StaticValueColumn("Escarpment >= 18 Encountered", "NO"),
      new MappedColumn("ID/Label", "report.nest_number"),
      sectionColumn,
      new InitialTreatmentColumn("Initial Treatment ",
          "report.intervention.relocation.was_relocated",
          "report.timestamp_found_ms",
          "report.intervention.protection_event.timestamp_ms",
          "report.intervention.protection_event.type"),
      new FinalTreatmentColumn("Final Treatment ",
          "report.intervention.relocation.was_relocated",
          "report.timestamp_found_ms",
          "report.intervention.protection_event.timestamp_ms",
          "report.intervention.protection_event.type"),
      new MappedDistanceColumn("Distance From Dune",
          "report.location.apex_to_barrier_ft", "report.location.apex_to_barrier_in"),
      new MappedDistanceColumn("Distance From MHW",
          "report.location.water_to_apex_ft", "report.location.water_to_apex_in"),
      new MappedColumn("Nest Relocated", "report.intervention.relocation.was_relocated"),
      new FinalActivityColumn("Final Activity",
          "report.condition.abandoned_body_pits", "report.condition.abandoned_egg_cavities"),
      new MappedIsPresentColumn("Nest Washed Over", "report.condition.wash_over.0.timestamp_ms"),
      new MappedIsPresentColumn("Nest Completely or Partially Washed Out",
          "report.condition.wash_out.timestamp_ms"),
      new MappedIsPresentColumn("Predation", "report.condition.preditation.0.timestamp_ms"),
      new MappedTimestampColumn("Eggs Damaged by Another Turtle Date",
          "report.condition.eggs_scattered_by_another_timestamp_ms"),
      new ConditionallyMappedColumn("Type of Vandalism",
          "report.condition.vandalized", "report.condition.vandalism_type"),
      new ConditionallyMappedColumn("Eggs Removed",
          "report.condition.poached", "report.condition.poached_eggs_removed"),
      new MappedTimestampColumn("First Hatchling Emergence Date",
          "report.condition.hatch_timestamp_ms"),
      new MappedColumn("Hatchlings Disoriented", "report.condition.disorientation"),
      new MappedColumn("Nest Inventoried", "report.intervention.excavation.excavated"),
      new MappedTimestampColumn("Date Nest Inventoried",
          "report.intervention.excavation.timestamp_ms"),
      new MappedColumn("# of Dead Hatchlings", "report.intervention.excavation.dead_in_nest"),
      new MappedColumn("# of Live Hatchlings", "report.intervention.excavation.live_in_nest"),
      new MappedColumn("# of Empty Shells", "report.intervention.excavation.hatched_shells"),
      new MappedColumn("# of Dead Pipped", "report.intervention.excavation.dead_pipped"),
      new MappedColumn("# of Live Pipped", "report.intervention.excavation.live_pipped"),
      new MappedColumn("# of Whole Eggs", "report.intervention.excavation.whole_unhatched"),
      new MappedColumn("# of Damaged Eggs", "report.intervention.excavation.eggs_destroyed"),
      new MappedColumnWithDefault("Initial Clutch Size",
          "report.intervention.relocation.eggs_relocated",
          "UNK"),
      new MappedColumnAbsoluteValueDouble("Latitude", "report.location.coordinates.lat"),
      new MappedColumnAbsoluteValueDouble("Longitude", "report.location.coordinates.long"));

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    log.info("Generating Nest Report report, no auth.");
    ReportStore store = new ReportStore();
    store.init();

    response.setContentType(MediaType.CSV_UTF_8.toString());
    ReportCsvGenerator generator = new ReportCsvGenerator(
        new OrderedReportWriter(reportColumns, new Filter()));
    generator.addAllRows(store.getActiveReports());

    response.setHeader("content-disposition",
        "inline; filename=\"nest_reporter_state_nest_report_" + new Date().toString() + ".csv\"");
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

      String nest = columnMap.get(new Path("report.nest_number")).getValue(rowId);
      boolean isNest = !nest.equals("0") && !nest.equals("");
      return hasSection && isNest;
    }
  }
}