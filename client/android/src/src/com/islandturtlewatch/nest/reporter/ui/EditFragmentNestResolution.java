package com.islandturtlewatch.nest.reporter.ui;

import java.util.Map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.islandturtlewatch.nest.data.ReportProto.Excavation;
import com.islandturtlewatch.nest.data.ReportProto.Excavation.ExcavationFailureReason;
import com.islandturtlewatch.nest.data.ReportProto.NestCondition;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateHandler;
import com.islandturtlewatch.nest.reporter.R;
import com.islandturtlewatch.nest.reporter.util.DateUtil;

public class EditFragmentNestResolution extends EditFragment {
  private static final Map<Integer, ClickHandler> CLICK_HANDLERS =
      ClickHandler.toMap(
          new HandleSetAdditionalHatchDate(),
          new HandleSetDisorientation(),
          new HandleSetEggsNotFound(),
          new HandleSetEggsTooDecayed(),
          new HandleSetExcavated(),
          new HandleSetExcavationDate(),
          new HandleSetHatchDate(),
          new HandleSetReasonOther());

  private static final Map<Integer, TextChangeHandler> TEXT_CHANGE_HANDLERS =
      TextChangeHandler.toMap(
          new HandleUpdateDeadInNest(),
          new HandleUpdateDeadPipped(),
          new HandleUpdateEggsDestroyed(),
          new HandleUpdateHatchedShells(),
          new HandleUpdateLiveInNest(),
          new HandleUpdateLivePipped(),
          new HandleUpdateWholeUnhatched(),
          new HandleUpdateReasonOther());

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
    return inflater.inflate(R.layout.edit_fragment_nest_resolution, container, false);
  }


  @Override
  public void updateSection(Report report) {
    NestCondition condition = report.getCondition();
    if (condition.hasHatchTimestampMs()) {
      setText(R.id.buttonHatchDate,
          DateUtil.getFormattedDate(condition.getHatchTimestampMs()));
    }
    if (condition.hasAdditionalHatchTimestampMs()) {
      setText(R.id.buttonAdditionalHatchDate,
          DateUtil.getFormattedDate(condition.getAdditionalHatchTimestampMs()));
    }
    setChecked(R.id.fieldDisorientation, condition.getDisorientation());

    Excavation excavation = report.getIntervention().getExcavation();
    setChecked(R.id.fieldExcavated, excavation.getExcavated());

    setVisible(!excavation.getExcavated(), ImmutableList.of(R.id.rowWhyNotExcavatedLabel,
        R.id.rowWhyNotExcavatedFields1, R.id.rowWhyNotExcavatedFields2));

    setChecked(R.id.fieldEggsNotFound,
        excavation.getFailureReason() == ExcavationFailureReason.EGGS_NOT_FOUND);
    setChecked(R.id.fieldEggsTooDecayed,
        excavation.getFailureReason() == ExcavationFailureReason.EGGS_HATCHLINGS_TOO_DECAYED);
    setChecked(R.id.fieldNoExcavationOther,
        excavation.getFailureReason() == ExcavationFailureReason.OTHER);
    setEnabled(R.id.fieldNoExcavationOtherValue,
        excavation.getFailureReason() == ExcavationFailureReason.OTHER);
    setText(R.id.fieldNoExcavationOtherValue, excavation.getFailureOther());

    if (excavation.hasTimestampMs()) {
      setText(R.id.buttonExcavationDate,
          DateUtil.getFormattedDate(excavation.getTimestampMs()));
    }

    setVisible(R.id.tableExcavationCounts, excavation.getExcavated());

    Adder adder = new Adder();
    setText(R.id.fieldDeadInNest,
        excavation.hasDeadInNest() ? Integer.toString(adder.add(excavation.getDeadInNest()))
            : "");
    setText(R.id.fieldLiveInNest,
        excavation.hasLiveInNest() ? Integer.toString(adder.add(excavation.getLiveInNest()))
            : "");
    setText(R.id.fieldHatchedShells,
        excavation.hasHatchedShells() ? Integer.toString(adder.add(excavation.getHatchedShells()))
            : "");
    setText(R.id.fieldDeadPipped,
        excavation.hasDeadPipped() ? Integer.toString(adder.add(excavation.getDeadPipped()))
            : "");
    setText(R.id.fieldLivePipped,
        excavation.hasLivePipped() ? Integer.toString(adder.add(excavation.getLivePipped()))
            : "");
    setText(R.id.fieldWholeUnhatched,
        excavation.hasWholeUnhatched() ? Integer.toString(adder.add(excavation.getWholeUnhatched()))
            : "");
    setText(R.id.fieldEggsDestroyed,
        excavation.hasEggsDestroyed() ? Integer.toString(adder.add(excavation.getEggsDestroyed()))
            : "");
    setText(R.id.displayTotalEggs, Integer.toString(adder.total));
  }

  private static class Adder{
    int total = 0;
    public int add(int delta) {
      total += delta;
      return delta;
    }
  }

  private static class HandleSetHatchDate extends DatePickerClickHandler {
    protected HandleSetHatchDate() {
      super(R.id.buttonHatchDate);
    }

    @Override
    public void onDateSet(DatePicker view,
        int year,
        int month,
        int day) {
      displayResult(updateHandler.updateHatchDate(year, month, day));
    }
  }
  private static class HandleSetAdditionalHatchDate extends DatePickerClickHandler {
    protected HandleSetAdditionalHatchDate() {
      super(R.id.buttonAdditionalHatchDate);
    }

    @Override
    public void onDateSet(DatePicker view,
        int year,
        int month,
        int day) {
      displayResult(updateHandler.updateAdditionalHatchDate(year, month, day));
    }
  }
  private static class HandleSetExcavationDate extends DatePickerClickHandler {
    protected HandleSetExcavationDate() {
      super(R.id.buttonExcavationDate);
    }

    @Override
    public void onDateSet(DatePicker view,
        int year,
        int month,
        int day) {
      displayResult(updateHandler.updateExcavationDate(year, month, day));
    }
  }

  private static class HandleSetDisorientation extends ClickHandler {
    protected HandleSetDisorientation() {
      super(R.id.fieldDisorientation);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateDisorentation(isChecked(view));
    }
  }
  private static class HandleSetExcavated extends ClickHandler {
    protected HandleSetExcavated() {
      super(R.id.fieldExcavated);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateExcavated(isChecked(view));
    }
  }
  private static class HandleSetEggsNotFound extends ClickHandler {
    protected HandleSetEggsNotFound() {
      super(R.id.fieldEggsNotFound);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateExcavationFailure(ExcavationFailureReason.EGGS_NOT_FOUND);
    }
  }
  private static class HandleSetEggsTooDecayed extends ClickHandler {
    protected HandleSetEggsTooDecayed() {
      super(R.id.fieldEggsTooDecayed);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateExcavationFailure(ExcavationFailureReason.EGGS_HATCHLINGS_TOO_DECAYED);
    }
  }
  private static class HandleSetReasonOther extends ClickHandler {
    protected HandleSetReasonOther() {
      super(R.id.fieldNoExcavationOther);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateExcavationFailure(ExcavationFailureReason.OTHER);
    }
  }


  private static class HandleUpdateDeadInNest extends TextChangeHandler {
    protected HandleUpdateDeadInNest() {
      super(R.id.fieldDeadInNest);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      Optional<Integer> newValue = Optional.absent();
      if (!newText.isEmpty()) {
        newValue = Optional.of(Integer.parseInt(newText));
      }
      //TODO(edcoyne): display error if not parsable
      updateHandler.updateDeadInNest(newValue);
    }
  }
  private static class HandleUpdateLiveInNest extends TextChangeHandler {
    protected HandleUpdateLiveInNest() {
      super(R.id.fieldLiveInNest);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      Optional<Integer> newValue = Optional.absent();
      if (!newText.isEmpty()) {
        newValue = Optional.of(Integer.parseInt(newText));
      }
      //TODO(edcoyne): display error if not parsable
      updateHandler.updateLiveInNest(newValue);
    }
  }
  private static class HandleUpdateHatchedShells extends TextChangeHandler {
    protected HandleUpdateHatchedShells() {
      super(R.id.fieldHatchedShells);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      Optional<Integer> newValue = Optional.absent();
      if (!newText.isEmpty()) {
        newValue = Optional.of(Integer.parseInt(newText));
      }
      //TODO(edcoyne): display error if not parsable
      updateHandler.updateHatchedShells(newValue);
    }
  }
  private static class HandleUpdateDeadPipped extends TextChangeHandler {
    protected HandleUpdateDeadPipped() {
      super(R.id.fieldDeadPipped);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      Optional<Integer> newValue = Optional.absent();
      if (!newText.isEmpty()) {
        newValue = Optional.of(Integer.parseInt(newText));
      }
      //TODO(edcoyne): display error if not parsable
      updateHandler.updateDeadPipped(newValue);
    }
  }
  private static class HandleUpdateLivePipped extends TextChangeHandler {
    protected HandleUpdateLivePipped() {
      super(R.id.fieldLivePipped);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      Optional<Integer> newValue = Optional.absent();
      if (!newText.isEmpty()) {
        newValue = Optional.of(Integer.parseInt(newText));
      }
      //TODO(edcoyne): display error if not parsable
      updateHandler.updateLivePipped(newValue);
    }
  }
  private static class HandleUpdateWholeUnhatched extends TextChangeHandler {
    protected HandleUpdateWholeUnhatched() {
      super(R.id.fieldWholeUnhatched);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      Optional<Integer> newValue = Optional.absent();
      if (!newText.isEmpty()) {
        newValue = Optional.of(Integer.parseInt(newText));
      }
      //TODO(edcoyne): display error if not parsable
      updateHandler.updateWholeUnhatched(newValue);
    }
  }
  private static class HandleUpdateEggsDestroyed extends TextChangeHandler {
    protected HandleUpdateEggsDestroyed() {
      super(R.id.fieldEggsDestroyed);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      Optional<Integer> newValue = Optional.absent();
      if (!newText.isEmpty()) {
        newValue = Optional.of(Integer.parseInt(newText));
      }
      //TODO(edcoyne): display error if not parsable
      updateHandler.updateEggsDestroyed(newValue);
    }
  }
  private static class HandleUpdateReasonOther extends TextChangeHandler {
    protected HandleUpdateReasonOther() {
      super(R.id.fieldNoExcavationOtherValue);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.updateExcavationFailureOther(newText);
    }
  }

}
