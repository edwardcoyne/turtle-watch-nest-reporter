package com.islandturtlewatch.nest.reporter.ui;

import java.util.Map;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.google.common.base.Optional;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.Report.NestStatus;
import com.islandturtlewatch.nest.data.ReportProto.Report.Species;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateHandler;
import com.islandturtlewatch.nest.reporter.R;
import com.islandturtlewatch.nest.reporter.util.DateUtil;
import com.islandturtlewatch.nest.reporter.util.DialogUtil;

public class EditFragmentInfo extends EditFragment {
  private static final Map<Integer, ClickHandler> CLICK_HANDLERS =
      ClickHandler.toMap(
          new HandleSetInfoDate(),
          new HandleSetObervers(),
          new HandleSetNestVerified(),
          new HandleSetNestNotVerified(),
          new HandleSetNestRelocated(),
          new HandleSetFalseCrawl(),
          new HandleSetAbandonedBodyPits(),
          new HandleSetAbandonedEggCavities(),
          new HandleSetSpeciesLoggerHead(),
          new HandleSetSpeciesGreen(),
          new HandleSetSpeciesOther());

  private static final Map<Integer, TextChangeHandler> TEXT_CHANGE_HANDLERS =
      TextChangeHandler.toMap(
          new HandleUpdateNestNumber(),
          new HandleUpdateFalseCrawlNumber(),
          new HandleUpdateObservers(),
          new HandleUpdateSpeciesOther());

  @Override
  public Map<Integer, ClickHandler> getClickHandlers() {
    return CLICK_HANDLERS;
  }

  @Override
  public Map<Integer, TextChangeHandler> getTextChangeHandlers() {
    return TEXT_CHANGE_HANDLERS;
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.edit_fragment_info, container, false);
  }

  @Override
  public void updateSection(Report report) {
    setText(R.id.fieldNestNumber, report.hasNestNumber() ?
        Integer.toString(report.getNestNumber()) : "");
    setVisible(R.id.rowNestNumber, report.hasNestNumber());

    setText(R.id.fieldFalseCrawlNumber, report.hasFalseCrawlNumber() ?
        Integer.toString(report.getFalseCrawlNumber()) : "");
    setVisible(R.id.rowFalseCrawlNumber, report.hasFalseCrawlNumber());

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
    setChecked(R.id.fieldNestRelocated, report.getStatus() == NestStatus.NEST_RELOCATED);
    setChecked(R.id.fieldFalseCrawl, report.getStatus() == NestStatus.FALSE_CRAWL);

    setChecked(R.id.fieldAbandonedBodyPits, report.getCondition().getAbandonedBodyPits());
    setChecked(R.id.fieldAbandonedEggCavities, report.getCondition().getAbandonedEggCavities());

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
      if (newText.isEmpty()) {
        updateHandler.updateNestNumber(Optional.<Integer>absent());
      } else {
        int newValue = Integer.parseInt(newText);
        //TODO(edcoyne): display error if not parsable
        updateHandler.updateNestNumber(Optional.of(newValue));
      }
    }
  }
  private static class HandleUpdateFalseCrawlNumber extends TextChangeHandler {
    protected HandleUpdateFalseCrawlNumber() {
      super(R.id.fieldFalseCrawlNumber);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      if (newText.isEmpty()) {
        updateHandler.updateFalseCrawlNumber(Optional.<Integer>absent());
      } else {
        int newValue = Integer.parseInt(newText);
        //TODO(edcoyne): display error if not parsable
        updateHandler.updateFalseCrawlNumber(Optional.of(newValue));
      }
    }
  }

  private static class HandleUpdateObservers extends TextChangeHandler {
    protected HandleUpdateObservers() {
      super(R.id.fieldObservers);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.updateObservers(newText);
    }
  }

  private static class HandleUpdateSpeciesOther extends TextChangeHandler {
    protected HandleUpdateSpeciesOther() {
      super(R.id.fieldSpeciesOtherValue);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.updateSpeciesOther(newText);
    }
  }

  private static class HandleSetInfoDate extends DatePickerClickHandler {
    protected HandleSetInfoDate() {
      super(R.id.buttonDateFound);
    }
    @Override
    public void onDateSet(DatePicker view,
        int year,
        int month,
        int day) {
      Log.d(EditFragmentInfo.class.getSimpleName(),
          "Set date to " + year + "/" + month + "/" + day);
      displayResult(updateHandler.updateDateFound(year, month, day));
    }
  }

  private static class HandleSetObervers extends ClickHandler {
    protected HandleSetObervers() {
      super(R.id.fieldObservers);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateObservers(getText(view));
    }
  }

  private static class HandleSetNestVerified extends ClickHandler {
    protected HandleSetNestVerified() {
      super(R.id.fieldNestVerified);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateNestStatus(NestStatus.NEST_VERIFIED);
    }
  }

  private static class HandleSetNestNotVerified extends ClickHandler {
    protected HandleSetNestNotVerified() {
      super(R.id.fieldNestNotVerified);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.updateNestStatus(NestStatus.NEST_NOT_VERIFIED);
    }
  }

  private static class HandleSetNestRelocated extends ClickHandler {
    protected HandleSetNestRelocated() {
      super(R.id.fieldNestRelocated);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.updateNestStatus(NestStatus.NEST_RELOCATED);
    }
  }

  private static class HandleSetFalseCrawl extends ClickHandler {
    protected HandleSetFalseCrawl() {
      super(R.id.fieldFalseCrawl);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.updateNestStatus(NestStatus.FALSE_CRAWL);
    }
  }

  private static class HandleSetAbandonedBodyPits extends ClickHandler {
    protected HandleSetAbandonedBodyPits() {
      super(R.id.fieldAbandonedBodyPits);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateAbandonedBodyPits(isChecked(view));
    }
  }

  private static class HandleSetAbandonedEggCavities extends ClickHandler {
    protected HandleSetAbandonedEggCavities() {
      super(R.id.fieldAbandonedEggCavities);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateAbandonedEggCavities(isChecked(view));
    }
  }
  private static class HandleSetSpeciesLoggerHead extends ClickHandler {
    protected HandleSetSpeciesLoggerHead() {
      super(R.id.fieldSpeciesLoggerHead);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.updateSpecies(Species.LOGGERHEAD);
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
      updateHandler.updateSpecies(Species.GREEN);
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
      updateHandler.updateSpecies(Species.OTHER);
    }
  }
}
