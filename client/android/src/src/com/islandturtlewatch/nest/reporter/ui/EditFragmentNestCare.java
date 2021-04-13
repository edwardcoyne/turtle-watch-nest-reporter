package com.islandturtlewatch.nest.reporter.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.islandturtlewatch.nest.data.ReportProto.GpsCoordinates;
import com.islandturtlewatch.nest.data.ReportProto.Intervention;
import com.islandturtlewatch.nest.data.ReportProto.Intervention.ProtectionEvent;
import com.islandturtlewatch.nest.data.ReportProto.Intervention.ProtectionEvent.Reason;
import com.islandturtlewatch.nest.data.ReportProto.Intervention.ProtectionEvent.Type;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateHandler;
import com.islandturtlewatch.nest.reporter.R;
import com.islandturtlewatch.nest.reporter.data.Date;
import com.islandturtlewatch.nest.reporter.data.ReportMutations;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.DateProtectedMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.EggsDestroyedMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.NewAddressMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.RelocationGpsMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.ProtectionTypeMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.WhyProtectedMutation;
import com.islandturtlewatch.nest.reporter.ui.GpsCoordinateDialog.GpsLocationCallback;

import java.util.Map;

public class EditFragmentNestCare extends EditFragment {
  private static final Map<Integer, ClickHandler> CLICK_HANDLERS =
      ClickHandler.toMap(
          new HandleSetAfterPredation(),
          new HandleSetBeforePredation(),
          new HandleSetLightProblem(),
          new HandleSetReasonOther(),
          new HandleSetRestrainingCage(),
          new HandleSetSelfRealeasingCage(),
          new HandleSetSelfRealeasingFlat(),
          new HandleSetProtectedDate(),
          new HandleSetChangeAfterPredation(),
          new HandleSetChangeBeforePredation(),
          new HandleSetChangeLightProblem(),
          new HandleSetChangeRestrainingCage(),
          new HandleSetChangeSelfRealeasingCage(),
          new HandleSetChangeSelfRealeasingFlat(),
          new HandleSetChangeProtectedDate(),
          new HandleSetReasonChangeOther(),
          new HandleSetNewGps());

  private static final Map<Integer, TextChangeHandler> TEXT_CHANGE_HANDLERS =
      TextChangeHandler.toMap(
          new HandleUpdateNewAddress(),
          new HandleUpdateReasonOther(),
          new HandleUpdateReasonChangeOther(),
          new HandleUpdateProtectionChange(),
          new HandleUpdateEggsDestroyed());

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
      return inflater.inflate(R.layout.edit_fragment_nest_care, container, false);
  }

  @Override
  public void updateSection(Report report) {
    Intervention intervention = report.getIntervention();

    if (intervention.getProtectionEvent().hasTimestampMs()) {
      setDate(R.id.buttonProtectedDate, intervention.getProtectionEvent().getTimestampMs());
    } else {
      clearDate(R.id.buttonProtectedDate);
    }

    setChecked(R.id.fieldSelfReleasingCage,
        intervention.getProtectionEvent().getType() == Type.SELF_RELEASING_CAGE);
    setChecked(R.id.fieldSelfReleasingFlat,
        intervention.getProtectionEvent().getType() == Type.SELF_RELEASING_FLAT);
    setChecked(R.id.fieldRestrainingCage,
        intervention.getProtectionEvent().getType() == Type.RESTRAINING_CAGE);

    setChecked(R.id.fieldBeforePredation,
        intervention.getProtectionEvent().getReason() == Reason.BEFORE_PREDITATION);
    setChecked(R.id.fieldAfterPredation,
        intervention.getProtectionEvent().getReason() == Reason.AFTER_PREDITATION);
    setChecked(R.id.fieldForLightProblem,
        intervention.getProtectionEvent().getReason() == Reason.FOR_LIGHT_PROBLEMS);
    setChecked(R.id.fieldReasonOther,
            intervention.getProtectionEvent().getReason() == Reason.OTHER);
    setEnabled(R.id.fieldReasonOtherValue,
            intervention.getProtectionEvent().getReason() == Reason.OTHER);
    setText(R.id.fieldReasonOtherValue,
            intervention.getProtectionEvent().getReasonOther());
    setText(R.id.fieldChangeProtectionReason,intervention.getProtectionChangedReason());
//    pete and repeat

    if (intervention.getProtectionChangedEvent().hasTimestampMs()) {
      setDate(R.id.buttonProtectedChangeDate, intervention.getProtectionChangedEvent().getTimestampMs());
    } else {
      clearDate(R.id.buttonProtectedChangeDate);
    }

    setChecked(R.id.fieldSelfReleasingCageChange,
            intervention.getProtectionChangedEvent().getType() == Type.SELF_RELEASING_CAGE);
    setChecked(R.id.fieldSelfReleasingFlatChange,
            intervention.getProtectionChangedEvent().getType() == Type.SELF_RELEASING_FLAT);
    setChecked(R.id.fieldRestrainingCageChange,
            intervention.getProtectionChangedEvent().getType() == Type.RESTRAINING_CAGE);

    setChecked(R.id.fieldBeforePredationChange,
            intervention.getProtectionChangedEvent().getReason() == Reason.BEFORE_PREDITATION);
    setChecked(R.id.fieldAfterPredationChange,
            intervention.getProtectionChangedEvent().getReason() == Reason.AFTER_PREDITATION);
    setChecked(R.id.fieldForLightProblemChange,
            intervention.getProtectionChangedEvent().getReason() == Reason.FOR_LIGHT_PROBLEMS);

    setChecked(R.id.fieldChangeReasonOther,
            intervention.getProtectionChangedEvent().getReason() == Reason.OTHER);
    setEnabled(R.id.fieldChangeReasonOtherValue,
            intervention.getProtectionChangedEvent().getReason() == Reason.OTHER);
    setText(R.id.fieldChangeReasonOtherValue,
            intervention.getProtectionChangedEvent().getReasonOther());
  }

  private static class HandleSetProtectedDate extends DatePickerClickHandler {
    protected HandleSetProtectedDate() {
      super(R.id.buttonProtectedDate);
    }

    @Override
    public void onDateSet(DatePicker view, Optional<Date> maybeDate) {
      updateHandler.applyMutation(new DateProtectedMutation(maybeDate));
    }
  }

  private static class HandleSetSelfRealeasingCage extends ClickHandler {
    protected HandleSetSelfRealeasingCage() {
      super(R.id.fieldSelfReleasingCage);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(
          new ProtectionTypeMutation(ProtectionEvent.Type.SELF_RELEASING_CAGE));
    }
  }
  private static class HandleSetSelfRealeasingFlat extends ClickHandler {
    protected HandleSetSelfRealeasingFlat() {
      super(R.id.fieldSelfReleasingFlat);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(
          new ProtectionTypeMutation(ProtectionEvent.Type.SELF_RELEASING_FLAT));
    }
  }
  private static class HandleSetRestrainingCage extends ClickHandler {
    protected HandleSetRestrainingCage() {
      super(R.id.fieldRestrainingCage);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(
          new ProtectionTypeMutation(ProtectionEvent.Type.RESTRAINING_CAGE));
    }
  }
  private static class HandleSetBeforePredation extends ClickHandler {
    protected HandleSetBeforePredation() {
      super(R.id.fieldBeforePredation);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(
          new WhyProtectedMutation(ProtectionEvent.Reason.BEFORE_PREDITATION));
    }
  }
  private static class HandleSetAfterPredation extends ClickHandler {
    protected HandleSetAfterPredation() {
      super(R.id.fieldAfterPredation);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(
          new WhyProtectedMutation(ProtectionEvent.Reason.AFTER_PREDITATION));
    }
  }
  private static class HandleSetLightProblem extends ClickHandler {
    protected HandleSetLightProblem() {
      super(R.id.fieldForLightProblem);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(
          new WhyProtectedMutation(ProtectionEvent.Reason.FOR_LIGHT_PROBLEMS));
    }
  }

  private static class HandleSetReasonOther extends ClickHandler {
    protected HandleSetReasonOther() {
      super(R.id.fieldReasonOther);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(
              new WhyProtectedMutation(Reason.OTHER));
    }
  }

  private static class HandleUpdateReasonOther extends TextChangeHandler {
    protected HandleUpdateReasonOther() {
      super(R.id.fieldReasonOtherValue);
    }
    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ReportMutations.ReasonOtherValueMutation(newText));
    }
  }

//  TODO: copy paste is inferior to building a proper function
private static class HandleSetChangeProtectedDate extends DatePickerClickHandler {
  protected HandleSetChangeProtectedDate() {
    super(R.id.buttonProtectedChangeDate);
  }

  @Override
  public void onDateSet(DatePicker view, Optional<Date> maybeDate) {
    updateHandler.applyMutation(new ReportMutations.DateProtectedChangeMutation(maybeDate));
  }
}

  private static class HandleSetChangeSelfRealeasingCage extends ClickHandler {
    protected HandleSetChangeSelfRealeasingCage() {
      super(R.id.fieldSelfReleasingCageChange);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(
              new ReportMutations.ProtectionChangeTypeMutation(ProtectionEvent.Type.SELF_RELEASING_CAGE));
    }
  }
  private static class HandleSetChangeSelfRealeasingFlat extends ClickHandler {
    protected HandleSetChangeSelfRealeasingFlat() {
      super(R.id.fieldSelfReleasingFlatChange);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(
              new ReportMutations.ProtectionChangeTypeMutation(ProtectionEvent.Type.SELF_RELEASING_FLAT));
    }
  }
  private static class HandleSetChangeRestrainingCage extends ClickHandler {
    protected HandleSetChangeRestrainingCage() {
      super(R.id.fieldRestrainingCageChange);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(
              new ReportMutations.ProtectionChangeTypeMutation(ProtectionEvent.Type.RESTRAINING_CAGE));
    }
  }
  private static class HandleSetChangeBeforePredation extends ClickHandler {
    protected HandleSetChangeBeforePredation() {
      super(R.id.fieldBeforePredationChange);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(
              new ReportMutations.WhyProtectedChangeMutation(ProtectionEvent.Reason.BEFORE_PREDITATION));
    }
  }
  private static class HandleSetChangeAfterPredation extends ClickHandler {
    protected HandleSetChangeAfterPredation() {
      super(R.id.fieldAfterPredationChange);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(
              new ReportMutations.WhyProtectedChangeMutation(ProtectionEvent.Reason.AFTER_PREDITATION));
    }
  }
  private static class HandleSetChangeLightProblem extends ClickHandler {
    protected HandleSetChangeLightProblem() {
      super(R.id.fieldForLightProblemChange);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(
              new ReportMutations.WhyProtectedChangeMutation(ProtectionEvent.Reason.FOR_LIGHT_PROBLEMS));
    }


  }
  private static class HandleSetReasonChangeOther extends ClickHandler {
    protected HandleSetReasonChangeOther() {
      super(R.id.fieldChangeReasonOther);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(
              new ReportMutations.WhyProtectedChangeMutation(Reason.OTHER));
    }
  }

  private static class HandleUpdateReasonChangeOther extends TextChangeHandler {
    protected HandleUpdateReasonChangeOther() {
      super(R.id.fieldChangeReasonOtherValue);
    }
    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ReportMutations.ReasonOtherValueChangeMutation(newText));
    }
  }

  private static class HandleUpdateProtectionChange extends TextChangeHandler {
    protected HandleUpdateProtectionChange() {
      super(R.id.fieldChangeProtectionReason);
    }
    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ReportMutations.ChangeNestProtectionReasonMutation(newText));
    }
  }
  //End badwrong

  private static class HandleUpdateNewAddress extends TextChangeHandler {
    protected HandleUpdateNewAddress() {
      super(R.id.fieldNewAddress);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new NewAddressMutation(newText));
    }
  }

  private static class HandleUpdateEggsDestroyed extends TextChangeHandler {
    protected HandleUpdateEggsDestroyed() {
      super(R.id.fieldEggsDestroyed);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new EggsDestroyedMutation(getInteger(newText)));
    }
  }

  private static class HandleSetNewGps extends ClickHandler {
    protected HandleSetNewGps() {
      super(R.id.buttonNewGps);
    }

    @Override
    public void handleClick(View view, final DataUpdateHandler updateHandler) {
      GpsCoordinateDialog dialog = new GpsCoordinateDialog();
      Preconditions.checkArgument(view.getContext() instanceof Activity);
      dialog.setCallback(new GpsLocationCallback() {
        @Override
        public void location(GpsCoordinates coordinates) {
          updateHandler.applyMutation(new RelocationGpsMutation(coordinates));
        }
      });

      dialog.show(((Activity)view.getContext()).getFragmentManager(), "GPS");
    }
  }
}
