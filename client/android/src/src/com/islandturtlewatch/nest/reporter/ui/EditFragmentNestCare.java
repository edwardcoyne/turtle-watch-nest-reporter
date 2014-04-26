package com.islandturtlewatch.nest.reporter.ui;

import java.util.Map;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.google.common.base.Optional;
import com.islandturtlewatch.nest.data.ReportProto.Intervention;
import com.islandturtlewatch.nest.data.ReportProto.Intervention.ProtectionEvent;
import com.islandturtlewatch.nest.data.ReportProto.Intervention.ProtectionEvent.Reason;
import com.islandturtlewatch.nest.data.ReportProto.Intervention.ProtectionEvent.Type;
import com.islandturtlewatch.nest.data.ReportProto.Relocation;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateHandler;
import com.islandturtlewatch.nest.reporter.R;

public class EditFragmentNestCare extends EditFragment {
  private static final Map<Integer, ClickHandler> CLICK_HANDLERS =
      ClickHandler.toMap(
          new HandleSetAdopted(),
          new HandleSetAfterPredation(),
          new HandleSetBeforePredation(),
          new HandleSetLightProblem(),
          new HandleSetRelocated(),
          new HandleSetRestrainingCage(),
          new HandleSetSelfRealeasingCage(),
          new HandleSetSelfRealeasingFlat(),
          new HandleSetHighWater(),
          new HandleSetPredation(),
          new HandleSetWashingOut(),
          new HandleSetConstruction(),
          new HandleSetProtectedDate(),
          new HandleSetRelocationDate());

  private static final Map<Integer, TextChangeHandler> TEXT_CHANGE_HANDLERS =
      TextChangeHandler.toMap(
          new HandleUpdateNewAddress(),
          new HandleUpdateEggsRelocated(),
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
    setChecked(R.id.fieldNestAdopted, intervention.getAdopted());

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

    Relocation relocation = intervention.getRelocation();
    setChecked(R.id.fieldNestRelocated, relocation.getWasRelocated());
    setVisible(R.id.tableRelocated, isChecked(R.id.fieldNestRelocated));

    if (relocation.hasTimestampMs()) {
      setDate(R.id.buttonRelocatedDate, relocation.getTimestampMs());
    } else {
      clearDate(R.id.buttonRelocatedDate);
    }

    setText(R.id.fieldNewAddress, relocation.getNewAddress());
    // TODO(edcoyne) Set gps coordinates
    setText(R.id.fieldEggsRelocated, relocation.hasEggsRelocated() ?
        Integer.toString(relocation.getEggsRelocated()) : "");
    setText(R.id.fieldEggsDestroyed, relocation.hasEggsDestroyed() ?
        Integer.toString(relocation.getEggsDestroyed()) : "");
    setChecked(R.id.fieldHighWater, relocation.getReasonHighWater());
    setChecked(R.id.fieldPredation, relocation.getReasonPredation());
    setChecked(R.id.fieldWashingOut, relocation.getReasonWashingOut());
    setChecked(R.id.fieldConstruction, relocation.getReasonConstructionRenourishment());
  }

  private static class HandleSetProtectedDate extends DatePickerClickHandler {
    protected HandleSetProtectedDate() {
      super(R.id.buttonProtectedDate);
    }

    @Override
    public void onDateSet(DatePicker view,
        int year,
        int month,
        int day) {
      Log.d(EditFragmentNestCare.class.getSimpleName(),
          "Set date to " + year + "/" + month + "/" + day);
      displayResult(updateHandler.updateDateProtected(year, month, day));
    }
  }
  private static class HandleSetRelocationDate extends DatePickerClickHandler {
    protected HandleSetRelocationDate() {
      super(R.id.buttonRelocatedDate);
    }

    @Override
    public void onDateSet(DatePicker view,
        int year,
        int month,
        int day) {
      Log.d(EditFragmentNestCare.class.getSimpleName(),
          "Set date to " + year + "/" + month + "/" + day);
      displayResult(updateHandler.updateDateRelocated(year, month, day));
    }
  }
  private static class HandleSetAdopted extends ClickHandler {
    protected HandleSetAdopted() {
      super(R.id.fieldNestAdopted);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateAdopted(isChecked(view));
    }
  }
  private static class HandleSetSelfRealeasingCage extends ClickHandler {
    protected HandleSetSelfRealeasingCage() {
      super(R.id.fieldSelfReleasingCage);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateProtectionType(ProtectionEvent.Type.SELF_RELEASING_CAGE);
    }
  }
  private static class HandleSetSelfRealeasingFlat extends ClickHandler {
    protected HandleSetSelfRealeasingFlat() {
      super(R.id.fieldSelfReleasingFlat);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateProtectionType(ProtectionEvent.Type.SELF_RELEASING_FLAT);
    }
  }
  private static class HandleSetRestrainingCage extends ClickHandler {
    protected HandleSetRestrainingCage() {
      super(R.id.fieldRestrainingCage);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateProtectionType(ProtectionEvent.Type.RESTRAINING_CAGE);
    }
  }
  private static class HandleSetBeforePredation extends ClickHandler {
    protected HandleSetBeforePredation() {
      super(R.id.fieldBeforePredation);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateWhenProtected(ProtectionEvent.Reason.BEFORE_PREDITATION);
    }
  }
  private static class HandleSetAfterPredation extends ClickHandler {
    protected HandleSetAfterPredation() {
      super(R.id.fieldAfterPredation);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateWhenProtected(ProtectionEvent.Reason.AFTER_PREDITATION);
    }
  }
  private static class HandleSetLightProblem extends ClickHandler {
    protected HandleSetLightProblem() {
      super(R.id.fieldForLightProblem);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateWhenProtected(ProtectionEvent.Reason.FOR_LIGHT_PROBLEMS);
    }
  }
  private static class HandleSetRelocated extends ClickHandler {
    protected HandleSetRelocated() {
      super(R.id.fieldNestRelocated);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateRelocated(isChecked(view));
    }
  }

  private static class HandleSetHighWater extends ClickHandler {
    protected HandleSetHighWater() {
      super(R.id.fieldHighWater);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateRelocationReasonHighWater(isChecked(view));
    }
  }
  private static class HandleSetPredation extends ClickHandler {
    protected HandleSetPredation() {
      super(R.id.fieldPredation);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateRelocationReasonPredation(isChecked(view));
    }
  }
  private static class HandleSetWashingOut extends ClickHandler {
    protected HandleSetWashingOut() {
      super(R.id.fieldWashingOut);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateRelocationReasonWashingOut(isChecked(view));
    }
  }
  private static class HandleSetConstruction extends ClickHandler {
    protected HandleSetConstruction() {
      super(R.id.fieldConstruction);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateRelocationReasonConstruction(isChecked(view));
    }
  }

  private static class HandleUpdateNewAddress extends TextChangeHandler {
    protected HandleUpdateNewAddress() {
      super(R.id.fieldNewAddress);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.updateNewAddress(newText);
    }
  }
  private static class HandleUpdateEggsRelocated extends TextChangeHandler {
    protected HandleUpdateEggsRelocated() {
      super(R.id.fieldEggsRelocated);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      Optional<Integer> newValue = Optional.absent();
      if (!newText.isEmpty()) {
        newValue = Optional.of(Integer.parseInt(newText));
      }
      //TODO(edcoyne): display error if not parsable
      updateHandler.updateNumberOfEggsRelocated(newValue);
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
      updateHandler.updateNumberOfEggsDestroyed(newValue);
    }
  }
}
