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
import com.islandturtlewatch.nest.reporter.data.Date;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.AdditionalHatchDateMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.DisorentationMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.ExcavationDateMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.ExcavationDeadInNestMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.ExcavationDeadPippedMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.ExcavationEggsDestroyedMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.ExcavationFailureMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.ExcavationFailureOtherMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.ExcavationHatchedMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.ExcavationLiveInNestMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.ExcavationLivePippedMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.ExcavationWholeUnhatchedMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.HatchDateMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.WasExcavatedMutation;

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
      setDate(R.id.buttonHatchDate, condition.getHatchTimestampMs());
    } else {
      clearDate(R.id.buttonHatchDate);
    }
    if (condition.hasAdditionalHatchTimestampMs()) {
      setDate(R.id.buttonAdditionalHatchDate, condition.getAdditionalHatchTimestampMs());
    } else {
      clearDate(R.id.buttonAdditionalHatchDate);
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
      setDate(R.id.buttonExcavationDate, excavation.getTimestampMs());
    } else {
      clearDate(R.id.buttonExcavationDate);
    }

    setVisible(R.id.tableExcavationCounts, excavation.getExcavated());

    setText(R.id.fieldDeadInNest,
        excavation.hasDeadInNest() ? Integer.toString(excavation.getDeadInNest()) : "");
    setText(R.id.fieldLiveInNest,
        excavation.hasLiveInNest() ? Integer.toString(excavation.getLiveInNest()) : "");

    Adder adder = new Adder();
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
    public void onDateSet(DatePicker view, Optional<Date> maybeDate) {
      updateHandler.applyMutation(new HatchDateMutation(maybeDate));
    }
  }
  private static class HandleSetAdditionalHatchDate extends DatePickerClickHandler {
    protected HandleSetAdditionalHatchDate() {
      super(R.id.buttonAdditionalHatchDate);
    }

    @Override
    public void onDateSet(DatePicker view, Optional<Date> maybeDate) {
      updateHandler.applyMutation(new AdditionalHatchDateMutation(maybeDate));
    }
  }
  private static class HandleSetExcavationDate extends DatePickerClickHandler {
    protected HandleSetExcavationDate() {
      super(R.id.buttonExcavationDate);
    }

    @Override
    public void onDateSet(DatePicker view, Optional<Date> maybeDate) {
      updateHandler.applyMutation(new ExcavationDateMutation(maybeDate));
    }
  }

  private static class HandleSetDisorientation extends ClickHandler {
    protected HandleSetDisorientation() {
      super(R.id.fieldDisorientation);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new DisorentationMutation(isChecked(view)));
    }
  }
  private static class HandleSetExcavated extends ClickHandler {
    protected HandleSetExcavated() {
      super(R.id.fieldExcavated);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new WasExcavatedMutation(isChecked(view)));
    }
  }
  private static class HandleSetEggsNotFound extends ClickHandler {
    protected HandleSetEggsNotFound() {
      super(R.id.fieldEggsNotFound);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(
          new ExcavationFailureMutation(ExcavationFailureReason.EGGS_NOT_FOUND));
    }
  }
  private static class HandleSetEggsTooDecayed extends ClickHandler {
    protected HandleSetEggsTooDecayed() {
      super(R.id.fieldEggsTooDecayed);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(
          new ExcavationFailureMutation(ExcavationFailureReason.EGGS_HATCHLINGS_TOO_DECAYED));
    }
  }
  private static class HandleSetReasonOther extends ClickHandler {
    protected HandleSetReasonOther() {
      super(R.id.fieldNoExcavationOther);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ExcavationFailureMutation(ExcavationFailureReason.OTHER));
    }
  }


  private static class HandleUpdateDeadInNest extends TextChangeHandler {
    protected HandleUpdateDeadInNest() {
      super(R.id.fieldDeadInNest);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ExcavationDeadInNestMutation(getInteger(newText)));
    }
  }
  private static class HandleUpdateLiveInNest extends TextChangeHandler {
    protected HandleUpdateLiveInNest() {
      super(R.id.fieldLiveInNest);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ExcavationLiveInNestMutation(getInteger(newText)));
    }
  }
  private static class HandleUpdateHatchedShells extends TextChangeHandler {
    protected HandleUpdateHatchedShells() {
      super(R.id.fieldHatchedShells);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ExcavationHatchedMutation(getInteger(newText)));
    }
  }
  private static class HandleUpdateDeadPipped extends TextChangeHandler {
    protected HandleUpdateDeadPipped() {
      super(R.id.fieldDeadPipped);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ExcavationDeadPippedMutation(getInteger(newText)));
    }
  }
  private static class HandleUpdateLivePipped extends TextChangeHandler {
    protected HandleUpdateLivePipped() {
      super(R.id.fieldLivePipped);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ExcavationLivePippedMutation(getInteger(newText)));
    }
  }
  private static class HandleUpdateWholeUnhatched extends TextChangeHandler {
    protected HandleUpdateWholeUnhatched() {
      super(R.id.fieldWholeUnhatched);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ExcavationWholeUnhatchedMutation(getInteger(newText)));
    }
  }
  private static class HandleUpdateEggsDestroyed extends TextChangeHandler {
    protected HandleUpdateEggsDestroyed() {
      super(R.id.fieldEggsDestroyed);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ExcavationEggsDestroyedMutation(getInteger(newText)));
    }
  }
  private static class HandleUpdateReasonOther extends TextChangeHandler {
    protected HandleUpdateReasonOther() {
      super(R.id.fieldNoExcavationOtherValue);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ExcavationFailureOtherMutation(newText));
    }
  }

}
