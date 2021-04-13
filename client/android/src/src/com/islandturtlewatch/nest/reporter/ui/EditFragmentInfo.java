package com.islandturtlewatch.nest.reporter.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.google.common.base.Optional;
import com.google.firebase.auth.FirebaseAuth;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.Report.NestStatus;
import com.islandturtlewatch.nest.data.ReportProto.Report.Species;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateHandler;
import com.islandturtlewatch.nest.reporter.R;
import com.islandturtlewatch.nest.reporter.data.Date;
import com.islandturtlewatch.nest.reporter.data.ReportMutations;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.AbandonedBodyPitsMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.AbandonedEggCavitiesMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.DateFoundMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.FalseCrawlNumberMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.NestNumberMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.NestStatusMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.NoDiggingMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.ObserversMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.SpeciesMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.SpeciesOtherMutation;
import com.islandturtlewatch.nest.reporter.util.DateUtil;
import com.islandturtlewatch.nest.reporter.util.DialogUtil;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditFragmentInfo extends EditFragment {
  private static final Map<Integer, ClickHandler> CLICK_HANDLERS =
      ClickHandler.toMap(
          new HandleSetInfoDate(),
          new HandleSetObervers(),
          new HandleSetNestVerified(),
          new HandleSetNestNotVerified(),
          new HandleSetNestRelocated(),
          new HandleSetFalseCrawl(),
          new HandleSetPossibleFalseCrawl(),
          new HandleSetAbandonedBodyPits(),
          new HandleSetAbandonedEggCavities(),
          new HandleSetSpeciesLoggerHead(),
          new HandleSetSpeciesGreen(),
          new HandleSetSpeciesOther(),
          new HandleSetNoDigging());

  private static final Map<Integer, TextChangeHandler> TEXT_CHANGE_HANDLERS =
      TextChangeHandler.toMap(
          new HandleUpdateNestNumber(),
          new HandleUpdateFalseCrawlNumber(),
          new HandleUpdateObservers(),
          new HandleUpdatePossibleFalseCrawlNumber(),
          new HandleUpdateSpeciesOther());

  private static final Map<Integer, OnItemSelectedHandler> ITEM_SELECTED_HANDLERS =
          OnItemSelectedHandler.toMap(
                  new HandleSelectMarkingStrategy()
                  //add handlers
          );

  // Determined from user name.
  private int sectionNumber;
  private static final Pattern USERNAME_PATTERN =
      Pattern.compile("section([0-9]+)@islandturtlewatch.com");

  @Override
  public Map<Integer, ClickHandler> getClickHandlers() {
    return CLICK_HANDLERS;
  }

  @Override
  public Map<Integer, TextChangeHandler> getTextChangeHandlers() {
    return TEXT_CHANGE_HANDLERS;
  }

  @Override
  public Map<Integer,OnItemSelectedHandler> getOnItemSelectedHandlers() {return ITEM_SELECTED_HANDLERS;}

  @Override
  public View onCreateView(LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {
    updateSectionNumber();
    return inflater.inflate(R.layout.edit_fragment_info, container, false);
  }

  private void updateSectionNumber() {

    String username = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getEmail() : "";
    Matcher matcher = USERNAME_PATTERN.matcher(username);
    if (matcher.matches()) {
      sectionNumber = Integer.parseInt(matcher.group(1));
    }
  }

  @Override
  public void updateSection(Report report) {
    setText(R.id.fieldSectionNumber, Integer.toString(sectionNumber));

    setText(R.id.fieldNestNumber, report.hasNestNumber() ?
            Integer.toString(report.getNestNumber()) : "");
    setVisible(R.id.rowNestNumber, report.hasNestNumber());

    setText(R.id.fieldFalseCrawlNumber, report.hasFalseCrawlNumber() ?
            Integer.toString(report.getFalseCrawlNumber()) : "");
    setVisible(R.id.rowFalseCrawlNumber, report.hasFalseCrawlNumber());
    setText(R.id.fieldPossibleFalseCrawlNumber,report.hasPossibleFalseCrawlNumber() ?
    Integer.toString(report.getPossibleFalseCrawlNumber()) : "");
    if (report.hasTimestampFoundMs()) {
      setDate(R.id.buttonDateFound, report.getTimestampFoundMs());
      setText(R.id.labelIncubationDate,
          DateUtil.getFormattedDate(DateUtil.plusDays(report.getTimestampFoundMs(), 55)));
    } else {
      clearDate(R.id.buttonDateFound);
      setText(R.id.labelIncubationDate, "");
    }

    setText(R.id.fieldObservers, report.hasObservers() ? report.getObservers() : "");

    setChecked(R.id.fieldNestVerified, report.getStatus() == NestStatus.NEST_VERIFIED);
    setChecked(R.id.fieldNestNotVerified, report.getStatus() == NestStatus.NEST_NOT_VERIFIED);
    setChecked(R.id.fieldNestRelocated, report.getIntervention().getRelocation().getWasRelocated());
    setChecked(R.id.fieldFalseCrawl, report.getStatus() == NestStatus.FALSE_CRAWL);
    setChecked(R.id.fieldPossibleFalseCrawl, report.getPossibleFalseCrawl());

    setChecked(R.id.fieldAbandonedBodyPits, report.getCondition().getAbandonedBodyPits());
    setChecked(R.id.fieldAbandonedEggCavities, report.getCondition().getAbandonedEggCavities());
    setChecked(R.id.fieldNoDigging, report.getCondition().getNoDigging());

    setChecked(R.id.fieldSpeciesLoggerHead, report.getSpecies() == Species.LOGGERHEAD);
    setChecked(R.id.fieldSpeciesGreen, report.getSpecies() == Species.GREEN);
    setChecked(R.id.fieldSpeciesOther, report.getSpecies() == Species.OTHER);

    setEnabled(R.id.fieldSpeciesOtherValue, report.getSpecies() == Species.OTHER);
    setText(R.id.fieldSpeciesOtherValue, report.getSpeciesOther());
  }

  private static class HandleUpdateNestNumber extends TextChangeHandler {
    protected HandleUpdateNestNumber() {
      super(R.id.fieldNestNumber);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new NestNumberMutation(getInteger(newText)));
    }
  }

  private static class HandleUpdatePossibleFalseCrawlNumber extends TextChangeHandler {
    protected HandleUpdatePossibleFalseCrawlNumber() {
      super(R.id.fieldPossibleFalseCrawlNumber);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ReportMutations.PossibleFalseCrawlNumberMutation(getInteger(newText)));
    }
  }

  private static class HandleUpdateFalseCrawlNumber extends TextChangeHandler {
    protected HandleUpdateFalseCrawlNumber() {
      super(R.id.fieldFalseCrawlNumber);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new FalseCrawlNumberMutation(getInteger(newText)));
    }
  }

  private static class HandleUpdateObservers extends TextChangeHandler {
    protected HandleUpdateObservers() {
      super(R.id.fieldObservers);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ObserversMutation(newText));
    }
  }

  private static class HandleUpdateSpeciesOther extends TextChangeHandler {
    protected HandleUpdateSpeciesOther() {
      super(R.id.fieldSpeciesOtherValue);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new SpeciesOtherMutation(newText));
    }
  }

  private static class HandleSetInfoDate extends DatePickerClickHandler {
    protected HandleSetInfoDate() {
      super(R.id.buttonDateFound);
    }
    @Override
    public void onDateSet(DatePicker view, Optional<Date> maybeDate) {
      updateHandler.applyMutation(new DateFoundMutation(maybeDate));
    }
  }

  private static class HandleSetObervers extends ClickHandler {
    protected HandleSetObervers() {
      super(R.id.fieldObservers);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ObserversMutation(getText(view)));
    }
  }

  private static class HandleSetNestVerified extends ClickHandler {
    protected HandleSetNestVerified() {
      super(R.id.fieldNestVerified);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new NestStatusMutation(NestStatus.NEST_VERIFIED));
      updateHandler.applyMutation(new ReportMutations.PossibleFalseCrawlMutation(false));
      updateHandler.applyMutation(new NoDiggingMutation(false));
    }
  }

  private static class HandleSetNestNotVerified extends ClickHandler {
    protected HandleSetNestNotVerified() {
      super(R.id.fieldNestNotVerified);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new NestStatusMutation(NestStatus.NEST_NOT_VERIFIED));
      updateHandler.applyMutation(new ReportMutations.PossibleFalseCrawlMutation(false));
      updateHandler.applyMutation(new NoDiggingMutation(false));
    }
  }

  private static class HandleSetNestRelocated extends ClickHandler {
    protected HandleSetNestRelocated() {
      super(R.id.fieldNestRelocated);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ReportMutations.RelocatedMutation(isChecked(view)));
    }
  }

  private static class HandleSetPossibleFalseCrawl extends ClickHandler {
    protected HandleSetPossibleFalseCrawl() {
      super(R.id.fieldPossibleFalseCrawl);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ReportMutations.PossibleFalseCrawlMutation(isChecked(view)));
      if (isChecked(view)) {
        updateHandler.applyMutation(new NestStatusMutation(NestStatus.FALSE_CRAWL));
        updateHandler.applyMutation(new NoDiggingMutation(true));
      }
    }
  }

  private static class HandleSetFalseCrawl extends ClickHandler {
    protected HandleSetFalseCrawl() {
      super(R.id.fieldFalseCrawl);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new NestStatusMutation(NestStatus.FALSE_CRAWL));
      updateHandler.applyMutation(new NoDiggingMutation(true));
    }
  }

  private static class HandleSetAbandonedBodyPits extends ClickHandler {
    protected HandleSetAbandonedBodyPits() {
      super(R.id.fieldAbandonedBodyPits);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new AbandonedBodyPitsMutation(isChecked(view)));
    }
  }

  private static class HandleSetAbandonedEggCavities extends ClickHandler {
    protected HandleSetAbandonedEggCavities() {
      super(R.id.fieldAbandonedEggCavities);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new AbandonedEggCavitiesMutation(isChecked(view)));
    }
  }
  private static class HandleSetNoDigging extends ClickHandler {
    protected HandleSetNoDigging() {
      super(R.id.fieldNoDigging);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new NoDiggingMutation(isChecked(view)));
    }
  }
  private static class HandleSetSpeciesLoggerHead extends ClickHandler {
    protected HandleSetSpeciesLoggerHead() {
      super(R.id.fieldSpeciesLoggerHead);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new SpeciesMutation(Species.LOGGERHEAD));
    }
  }
  private static class HandleSetSpeciesGreen extends ClickHandler {
    protected HandleSetSpeciesGreen() {
      super(R.id.fieldSpeciesGreen);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      DialogUtil.acknowledge(view.getContext(),
          "Need photos of this species and please contact Suzi.");
      updateHandler.applyMutation(new SpeciesMutation(Species.GREEN));
    }
  }
  private static class HandleSetSpeciesOther extends ClickHandler {
    protected HandleSetSpeciesOther() {
      super(R.id.fieldSpeciesOther);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      DialogUtil.acknowledge(view.getContext(),
          "Need photos of this species and please contact Suzi.");
      updateHandler.applyMutation(new SpeciesMutation(Species.OTHER));
    }
  }

  private static class HandleSelectMarkingStrategy extends OnItemSelectedHandler {
    boolean firstCall = true;

    protected HandleSelectMarkingStrategy() {
      super(R.id.fieldMarkingStrategy);
    }
    @Override
    public void handleItemSelected(String value, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ReportMutations.MarkingStrategyMutation(value));
    }
  }
}
