package com.islandturtlewatch.nest.reporter.web.servlets;

import com.google.common.collect.ImmutableList;
import com.google.common.net.MediaType;
import com.islandturtlewatch.nest.data.ReportProto;
import com.islandturtlewatch.nest.reporter.backend.storage.ReportStore;
import com.islandturtlewatch.nest.reporter.frontend.reports.ColumnGenerator;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.ConditionallyMappedColumn;
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
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedPriorityColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedIfExistsColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedNotNullColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedNotNullTimestampColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedTimestampColumn;
import com.islandturtlewatch.nest.reporter.frontend.reports.OrderedReportWriter.MappedYNColumn;
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

  static ColumnGenerator columnGenerator = new ColumnGenerator();

  // This is the list of columns in the report, they will appear in this order.
  private static List<ReportColumn> reportColumns = ImmutableList.of(
//          columnGenerator.generateTimestampColumn("Date Nest Recorded", "report.timestamp_found_ms"),
      new MappedTimestampColumn("Date Nest Recorded", "report.timestamp_found_ms"),
//          columnGenerator.generateDefaultColumn("Escarpment >= 18 Encountered", "report.location.escarpment_over_18_inches"),
          new MappedColumn("ID/Label", "report.nest_number"),
          sectionColumn,
      new MappedColumn("Escarpment >= 18 Encountered", "report.location.escarpment_over_18_inches"),
//          columnGenerator.generateYNColumn("Nest seaward of armoring structure","report.nest_seaward_of_armoring_structure"),
      new MappedYNColumn("Nest seaward of armoring structure","report.nest_seaward_of_armoring_structure"),
//          columnGenerator.generateYNColumn("Nest within 3 feet of armoring structure","report.within_3_feet_of_structure"),
      new OrderedReportWriter.MappedYNColumn("Nest within 3 feet of armoring structure","report.within_3_feet_of_structure"),
//          columnGenerator.generateDefaultColumn("Type of Structure","report.type_of_structure"),
      new MappedColumn("Type of Structure","report.type_of_structure"),
//          columnGenerator.generateSpeciesColumn("Species","report.species"),
      new MappedSpeciesColumn("Species","report.species"),
//          columnGenerator.generateDefaultColumn("ID/Label", "report.nest_number"),

      new OrderedReportWriter.MappedComboColumn("Nest Label","ref.owner_id","report.nest_number"),
      new MappedColumn("Nest within Project Area","report.location.nest_within_project_area"),

//      columnGenerator.generateDefaultColumn("Nest within Project Area","report.location.nest_wihin_project_area"),

      new MappedColumn("City","report.location.city"),
//          columnGenerator.generateYNColumn("Within Cortez Groin Replacement Area",
//            "report.location.in_cortez_groin_replacement_area"),
      new MappedYNColumn("Within Cortez Groin Replacement Area",
              "report.location.in_cortez_groin_replacement_area"),
//          columnGenerator.generateDefaultColumn("Address","report.location.street_address"),
      new MappedColumn("Address","report.location.street_address"),
//            columnGenerator.generateYesOrBlankColumn("Body Pits",
//                    "report.condition.abandoned_body_pits"),
      new OrderedReportWriter.MappedYesOrBlankColumn("Body Pits",
              "report.condition.abandoned_body_pits"),
//          columnGenerator.generateYesOrBlankColumn("Egg Chambers",
//                  "report.condition.abandoned_egg_cavities"),
      new OrderedReportWriter.MappedYesOrBlankColumn("Egg Chambers",
              "report.condition.abandoned_egg_cavities"),
//          columnGenerator.generate0AsBlankColumn("Clutch Size Counted When Nest Was Made",
//                  "report.intervention.relocation.eggs_relocated"),
      new MappedColumnWithDefault("Clutch Size Counted When Nest Was Made",
              "report.intervention.relocation.eggs_relocated",
              ""),
      new InitialTreatmentColumn("Initial Treatment ",
          "report.intervention.relocation.was_relocated",
          "report.timestamp_found_ms",
          "report.intervention.protection_event.timestamp_ms",
          "report.intervention.protection_event.type"),

      new OrderedReportWriter.FinalTreatmentColumn(
              "Final Treatment",
              "report.intervention.protection_changed_event.timestamp_ms",
              "report.intervention.relocation.was_relocated",
              "report.intervention.protection_changed_event.type"),
      new MappedBlankIfUnsetColumn("Protection Event", "report.intervention.protection_event.type"),
      new MappedTimestampColumn("Initial Date Protected","report.intervention.protection_event.timestamp_ms"),
      new OrderedReportWriter.MappedBlankIfUnsetWithOtherColumn("Reason for Protection",
              "report.intervention.protection_event.reason","report.intervention.protection_event.reason_other"),
      new OrderedReportWriter.MappedHasTimestampYOrBlankColumn("Nest Protection Change",
              "report.intervention.protection_changed_event.timestamp_ms"),
      new MappedTimestampColumn("Date Nest Protection Changed","report.intervention.protection_changed_event.timestamp_ms"),
      new MappedBlankIfUnsetColumn("New Protection Event","report.intervention.protection_changed_event.type"),
      new MappedColumn("Reason for Nest Treatment Change","report.intervention.protection_changed_reason"),
          new MappedColumn("If predator control methods other than screening/caging were employed, " +
                  "please describe","report.condition.describe_control_methods"),
      new MappedDistanceColumn("Distance From Dune",
          "report.location.apex_to_barrier_ft", "report.location.apex_to_barrier_in"),
      new MappedDistanceColumn("Distance From MHW",
          "report.location.water_to_apex_ft", "report.location.water_to_apex_in"),
      new MappedColumn("Nest Relocated", "report.intervention.relocation.was_relocated"),
      new MappedTimestampColumn("Date Relocated","report.intervention.relocation.timestamp_ms"),
      new MappedBlankIfUnsetColumn("Reason for Relocation","report.intervention.relocation.reason"),
      new MappedNotNullColumn("Nest Washed Over", "report.condition.wash_over.0.timestamp_ms"),
      new MappedNotNullColumn("Inundated","report.condition.inundated_event.0.timestamp_ms"),
      new OrderedReportWriter.MappedYNColumn("Did Inundation Occur Prior to Hatching?",
              "report.condition.inundated_event.0.event_prior_to_hatching"),
      new MappedHasTimestampColumn("Complete Wash out","report.condition.wash_out.timestamp_ms"),
      new OrderedReportWriter.MappedWashoutTimeOptionColumn(
              "Did Complete Washout Occur Prior to Hatching?",
              "report.condition.complete_washout_timing",
              ReportProto.NestCondition.WashoutTimeOption.PRE_HATCH,
              "report.condition.wash_out.timestamp_ms"),
      new OrderedReportWriter.MappedWashoutTimeOptionColumn(
                  "Did Complete Washout Occur Post-Hatch but Pre-Inventory",
                  "report.condition.complete_washout_timing",
                  ReportProto.NestCondition.WashoutTimeOption.POST_HATCH,
              "report.condition.wash_out.timestamp_ms"),
      new MappedHasTimestampColumn("Partial Wash out","report.condition.partial_washout.timestamp_ms"),

//          TODO: This should be removed after this years reports are completed (Jul2017)
      new OrderedReportWriter.MappedExistsOrWashoutTimeColumn("Did Partial Washout Occur Prior to Hatching?",
              "report.condition.partial_washout.event_prior_to_hatching",
              "report.condition.partial_washout_timing",
              ReportProto.NestCondition.WashoutTimeOption.PRE_HATCH,
              "report.condition.partial_washout.timestamp_ms"),

//      new MappedIfExistsColumn("Did Partial Washout Occur Prior to Hatching?",
//              "report.condition.partial_washout.event_prior_to_hatching"),
      new OrderedReportWriter.MappedWashoutTimeOptionColumn(
              "Did Partial Washout Occur Post-Hatch, but Pre-Inventory",
              "report.condition.partial_washout_timing",
              ReportProto.NestCondition.WashoutTimeOption.POST_HATCH,
              "report.condition.partial_washout.timestamp_ms"),

//          TODO: This should be altered at the end of the season (Jul2017)
      new OrderedReportWriter.MappedAnyMatchColumn(
              "Did Washout Occur Post-Hatch but Pre-Inventory",
              "report.condition.partial_washout.event_prior_to_hatching",
              "report.condition.partial_washout_timing",
              "report.condition.complete_washout_timing",
              ReportProto.NestCondition.WashoutTimeOption.POST_HATCH),

      new OrderedReportWriter.MappedEitherOrColumn("Nest Completely or Partially Washed Out",
              "report.condition.wash_out.timestamp_ms",
              "report.condition.partial_washout.timestamp_ms"),
      new MappedPriorityColumn("If Washed Out By A Major Storm Give Name",
              "report.condition.wash_out.storm_name",
              "report.condition.partial_washout.storm_name",
              ""),
      new MappedNotNullColumn("Accretion","report.condition.accretion.0.timestamp_ms"),
      new OrderedReportWriter.MappedYNColumn("Did Accretion Occur Prior to Hatching?",
              "report.condition.accretion.0.event_prior_to_hatching"),
      new MappedNotNullTimestampColumn("Accretion Date","report.condition.accretion.0.timestamp_ms"),
      new MappedIfExistsColumn("Accretion Storm Name","report.condition.accretion.0.storm_name"),
      new MappedNotNullColumn("Erosion","report.condition.erosion.0.timestamp_ms"),

          new MappedNotNullColumn("Did Erosion Occur Prior to Hatching?",
                  "report.condition.erosion.0.event_prior_to_hatching"),
      new MappedNotNullTimestampColumn("Erosion Date", "report.condition.erosion.0.timestamp_ms"),
      new MappedIfExistsColumn("Erosion Storm Name", "report.condition.erosion.0.storm_name"),
      new MappedTimestampColumn("Other Storm impact Date","report.condition.storm_impact.timestamp_ms"),
      new MappedColumn("Other storm impact storm name","report.condition.storm_impact.storm_name"),
      new MappedColumn("Details","report.condition.storm_impact.other_impact"),
      new MappedColumn("Nest Completely Depredated",
              "report.condition.nest_depredated"),

      new OrderedReportWriter.MappedPartialPredationColumn("Partial Predation",
              "report.condition.nest_depredated",
              "report.condition.preditation.0.timestamp_ms"),
      new MappedColumn("Do you actively look for and record predation events?",
              "report.condition.actively_record_events"),
      new OrderedReportWriter.MappedProportionColumn("Regarding mammalian predation events, what proportion " +
              "of the events do you likely record?","report.condition.prop_events_recorded"),

      new MappedNotNullColumn("Predation", "report.condition.preditation.0.timestamp_ms"),

          new MappedNotNullTimestampColumn("Date(s) Predation Occurred",
              "report.condition.preditation.0.timestamp_ms"),
      new OrderedReportWriter.MappedYesOrBlankRadioColumn("Predated Prior to Hatching",
              "report.condition.preditation.0.predated_prior",
              ReportProto.NestCondition.PreditationEvent.PredationTimeOption.PRIOR_TO_HATCH),
      new OrderedReportWriter.MappedYesOrBlankRadioColumn("Predation Occurred Post Hatch but Prior to Inventory",
              "report.condition.preditation.0.predated_prior",
              ReportProto.NestCondition.PreditationEvent.PredationTimeOption.PRIOR_TO_INV),
      new MappedIfExistsColumn("If Predated by What Predator(s)","report.condition.preditation.0.predator_spinner_text"),
          //These names are being compared against the values in arrays.xml under predator_array
          new OrderedReportWriter.MappedPredatorColumn("Raccoon Only","report.condition.preditation.0.predator_spinner_text"),
          new OrderedReportWriter.MappedPredatorColumn("Fox Only","report.condition.preditation.0.predator_spinner_text"),
          new OrderedReportWriter.MappedPredatorColumn("Coyote Only","report.condition.preditation.0.predator_spinner_text"),
          new OrderedReportWriter.MappedPredatorColumn("Dog Only","report.condition.preditation.0.predator_spinner_text"),
          new OrderedReportWriter.MappedPredatorColumn("Canine (Unsure if Coyote or Dog)","report.condition.preditation.0.predator_spinner_text"),
          new OrderedReportWriter.MappedPredatorColumn("Feral Hog Only","report.condition.preditation.0.predator_spinner_text"),
          new OrderedReportWriter.MappedPredatorColumn("Armadillo Only","report.condition.preditation.0.predator_spinner_text"),
          new OrderedReportWriter.MappedPredatorColumn("Mammal - Unk","report.condition.preditation.0.predator_spinner_text"),
          new OrderedReportWriter.MappedPredatorColumn("Ants Only","report.condition.preditation.0.predator_spinner_text"),
          new OrderedReportWriter.MappedPredatorColumn("Ghost Crab Only","report.condition.preditation.0.predator_spinner_text"),
          new OrderedReportWriter.MappedPredatorColumn("Raccoon and Ghost Crab","report.condition.preditation.0.predator_spinner_text"),
          new OrderedReportWriter.MappedPredatorColumn("Coyote and Ghost Crab","report.condition.preditation.0.predator_spinner_text"),
          new OrderedReportWriter.MappedPredatorColumn("Other","report.condition.preditation.0.predator_spinner_text"),
      new MappedYNColumn("Did ghost crab(s) damage fewer than 10 eggs?","report.condition.ghost_damage_10_or_less"),
      new MappedYNColumn("Roots Invade Eggshells",
              "report.condition.roots_invaded_eggshells"),
      new MappedYNColumn("Eggs Damaged by Another Turtle",
          "report.condition.eggs_damaged_by_another_turtle"),
      new MappedYNColumn("Poached?",
              "report.condition.poached"),
          new OrderedReportWriter.MappedYNColumn("Poached (Eggs Removed)",
                  "report.condition.poached_eggs_removed"),
      new OrderedReportWriter.MappedYNColumn("Nest Dug Into","report.condition.nest_dug_into"),
      new OrderedReportWriter.MappedYNColumn("Vandalized","report.condition.vandalized"),
      new ConditionallyMappedColumn("Type of Vandalism",
          "report.condition.vandalized",
              "report.condition.vandalism_type"),
      new OrderedReportWriter.MappedYNColumn("Adopted","report.intervention.adopted"),
      new MappedIfExistsColumn("Adoptee","report.intervention.adoptee"),
      new MappedTimestampColumn("First Hatchling Emergence Date",
          "report.condition.hatch_timestamp_ms",
              "UNK"),
      new OrderedReportWriter.MappedShortTimestampColumn("Subsequent Emergence Date(s)",
              "report.condition.additional_hatch_timestamp_ms",
              "UNK"),
      new MappedColumn("Hatchlings Disoriented", "report.condition.disorientation"),

      new MappedYNColumn("Nest Inventoried", "report.intervention.excavation.excavated"),
      new OrderedReportWriter.MappedBlankIfUnsetWithOtherColumn("If Nest Not Inventoried Why Not?",
              "report.intervention.excavation.failure_reason",
              "report.intervention.excavation.failure_other"),
      new MappedTimestampColumn("Date Nest Inventoried",
          "report.intervention.excavation.timestamp_ms"),
      new MappedNullIfNotInventoriedColumn("# of Dead Hatchlings",
              "report.intervention.excavation.dead_in_nest","report.intervention.excavation.excavated"),
      new MappedNullIfNotInventoriedColumn("# of Live Hatchlings", "report.intervention.excavation.live_in_nest"
              ,"report.intervention.excavation.excavated"),
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
          ""),
          new MappedColumn("Additional Notes","report.additional_notes"),
          new MappedNotNullColumn("Photo","report.image.1"),
          new MappedColumnAbsoluteValueDouble("Latitude", "report.location.coordinates.lat"),
      new MappedColumnAbsoluteValueDouble("Longitude", "report.location.coordinates.long")
 );

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