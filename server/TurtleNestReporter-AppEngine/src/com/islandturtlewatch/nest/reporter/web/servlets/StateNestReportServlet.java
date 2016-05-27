package com.islandturtlewatch.nest.reporter.web.servlets;

import com.google.common.collect.ImmutableList;
import com.google.common.net.MediaType;
import com.islandturtlewatch.nest.reporter.backend.storage.ReportStore;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.ConditionallyMappedColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.FinalTreatmentColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.InitialTreatmentColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedHasTimestampColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedBlankIfUnsetColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedSpeciesColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedNullIfNotInventoriedColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedSectionColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedColumnAbsoluteValueDouble;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedColumnWithDefault;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedDistanceColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedIsPresentColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedPriorityColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedIfExistsColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedNotNullColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedNotNullTimestampColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedTimestampColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedYesNoColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.ReportColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.RowFilter;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.StaticValueColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.ReportCsvGenerator;
import com.islandturtlewatch.nest.reporter.frontend.reports.ReportCsvGenerator.Column;
import com.islandturtlewatch.nest.reporter.frontend.reports.ReportCsvGenerator.Path;

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

@Log
public class StateNestReportServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  // Keep this column separate so we can test against it.
  private static ReportColumn sectionColumn = new MappedSectionColumn("Beach Zone", "ref.owner_id");

//    private static ReportColumn sectionColumn = new StaticValueColumn("**TEST**", "1");



  // This is the list of columns in the report, they will appear in this order.
  private static List<ReportColumn> reportColumns = ImmutableList.of(
      new MappedTimestampColumn("Date Nest Recorded", "report.timestamp_found_ms"),
      new MappedColumn("Escarpment >= 18 Encountered", "report.location.escarpment_over_18_inches"),
      new MappedSpeciesColumn("Species","report.species"),
      new MappedColumn("ID/Label", "report.nest_number"),
      sectionColumn,
      new OrderedReportWriter.MappedComboColumn("Nest Label","ref.owner_id","report.nest_number"),
      new StaticValueColumn("Nest within Project Area","YES"),
      new MappedColumn("City","report.location.city"),
      new MappedYesNoColumn("Within Cortez Groin Replacement Area",
              "report.location.in_cortez_groin_replacement_area"),
      new MappedColumn("Address","report.location.street_address"),
      new MappedColumnWithDefault("Clutch Size Counted When Nest Was Made",
              "report.intervention.relocation.eggs_relocated",
              "UNK"),
      new InitialTreatmentColumn("Initial Treatment ",
          "report.intervention.relocation.was_relocated",
          "report.timestamp_found_ms",
          "report.intervention.protection_event.timestamp_ms",
          "report.intervention.protection_event.type"),
          // ROW 10
      new FinalTreatmentColumn("Final Treatment ",
          "report.intervention.relocation.was_relocated",
          "report.timestamp_found_ms",
          "report.intervention.protection_event.timestamp_ms",
          "report.intervention.protection_event.type"),
      new MappedBlankIfUnsetColumn("Protection Event", "report.intervention.protection_event.type"),
      new MappedTimestampColumn("Date Protected","report.intervention.protection_event.timestamp_ms"),
      new MappedDistanceColumn("Distance From Dune",
          "report.location.apex_to_barrier_ft", "report.location.apex_to_barrier_in"),
      new MappedDistanceColumn("Distance From MHW",
          "report.location.water_to_apex_ft", "report.location.water_to_apex_in"),
      new MappedColumn("Nest Relocated", "report.intervention.relocation.was_relocated"),
      new MappedTimestampColumn("Date Relocated","report.intervention.relocation.timestamp_ms"),
          //not required for Nest report, only for False Crawl Report
//      new FinalActivityColumn("Final Activity",
//          "report.condition.abandoned_body_pits", "report.condition.abandoned_egg_cavities"),

      new MappedNotNullColumn("Nest Washed Over", "report.condition.wash_over.0.timestamp_ms"),
      new MappedNotNullColumn("Inundated","report.condition.inundated_event.0.timestamp_ms"),

          //This column deprecated, has been split into distinct complete/partial columns
//      new MappedIsPresentColumn("Nest Completely or Partially Washed Out",
//          "report.condition.wash_out.timestamp_ms"),

      new MappedHasTimestampColumn("Complete Wash out","report.condition.wash_out.timestamp_ms"),
      new MappedHasTimestampColumn("Partial Wash out","report.condition.partial_washout.timestamp_ms"),
      new OrderedReportWriter.MappedEitherOrColumn("Nest Completely or Partially Washed Out",
              "report.condition.wash_out.timestamp_ms",
              "report.condition.partial_washout.timestamp_ms"),
      new MappedYesNoColumn("Did Washout Occur Post-Hatch but Pre-Inventory",
              "report.condition.post_hatch_washout"),
          //ROW 20
      new MappedPriorityColumn("If Washed Out By A Major Storm Give Name",
              "report.condition.wash_out.storm_name",
              "report.condition.partial_washout.storm_name",
              ""),

      new MappedColumn("Nest Completely Depredated",
              "report.condition.nest_depredated"),
      new MappedNotNullColumn("Predation", "report.condition.preditation.0.timestamp_ms"),

          new MappedNotNullTimestampColumn("Date(s) Predation Occurred",
              "report.condition.preditation.0.timestamp_ms"),
    new MappedIfExistsColumn("If Predated by What Predator(s)","report.condition.preditation.0.predator"),
      new MappedYesNoColumn("Roots Invade Eggshells",
              "report.condition.roots_invaded_eggshells"),
      new MappedYesNoColumn("Eggs Damaged by Another Turtle",
          "report.condition.eggs_damaged_by_another_turtle"),
      new MappedYesNoColumn("Poached?",
              "report.condition.poached"),
          new MappedYesNoColumn("Poached (Eggs Removed)",
                  "report.condition.poached_eggs_removed"),
      new MappedYesNoColumn("Nest Dug Into","report.condition.nest_dug_into"),
      new MappedYesNoColumn("Vandalized","report.condition.vandalized"),
      new ConditionallyMappedColumn("Type of Vandalism",
          "report.condition.vandalized",
              "report.condition.vandalism_type"),
          //ROW 30
      new MappedYesNoColumn("Adopted","report.intervention.adopted"),
      new MappedIfExistsColumn("Adoptee","report.intervention.adoptee"),
      new MappedTimestampColumn("First Hatchling Emergence Date",
          "report.condition.hatch_timestamp_ms",
              "UNK"),
      new OrderedReportWriter.MappedShortTimestampColumn("Subsequent Emergence Date(s)",
              "report.condition.additional_hatch_timestamp_ms",
              "UNK"),
      new MappedColumn("Hatchlings Disoriented", "report.condition.disorientation"),
      new MappedColumn("Nest Inventoried", "report.intervention.excavation.excavated"),
      new OrderedReportWriter.MappedBlankIfUnsetWithOtherColumn("If Nest Not Inventoried Why Not?",
              "report.intervention.excavation.failure_reason",
              "report.intervention.excavation.failure_other"),
      new MappedTimestampColumn("Date Nest Inventoried",
          "report.intervention.excavation.timestamp_ms"),
      new MappedNullIfNotInventoriedColumn("# of Dead Hatchlings",
              "report.intervention.excavation.dead_in_nest","report.intervention.excavation.excavated"),
      new MappedNullIfNotInventoriedColumn("# of Live Hatchlings", "report.intervention.excavation.live_in_nest"
              ,"report.intervention.excavation.excavated"),
          //ROW 40
      new MappedNullIfNotInventoriedColumn("# of Empty Shells", "report.intervention.excavation.hatched_shells"
              ,"report.intervention.excavation.excavated"),
      new MappedNullIfNotInventoriedColumn("# of Dead Pipped", "report.intervention.excavation.dead_pipped"
              ,"report.intervention.excavation.excavated"),
      new MappedNullIfNotInventoriedColumn("# of Live Pipped", "report.intervention.excavation.live_pipped"
              ,"report.intervention.excavation.excavated"),
      new MappedNullIfNotInventoriedColumn("# of Whole Eggs", "report.intervention.excavation.whole_unhatched"
              ,"report.intervention.excavation.excavated"),
      new MappedNullIfNotInventoriedColumn("# of Damaged Eggs", "report.intervention.excavation.eggs_destroyed"
              ,"report.intervention.excavation.excavated"),
      new MappedColumnWithDefault("Initial Clutch Size",
          "report.intervention.relocation.eggs_relocated",
          "UNK"),
          new MappedColumn("Additional Notes","report.additional_notes"),
          new MappedNotNullColumn("Photo","report.image"),

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
        "inline; filename=\"nest_reporter_state_nest_report_" +
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
//       If there is no section number it is a junk report.
      boolean hasSection = !sectionColumn.getFetcher().fetch(columnMap, rowId).equals("");
      String nest = columnMap.get(new Path("report.nest_number")).getValue(rowId);
      boolean isNest = !nest.equals("0") && !nest.equals("");
      return hasSection && isNest;
    }
  }
}