package com.islandturtlewatch.nest.reporter.ui;

import java.util.Map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.islandturtlewatch.nest.data.ReportProto.NestCondition;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateHandler;
import com.islandturtlewatch.nest.reporter.R;
import com.islandturtlewatch.nest.reporter.util.DateUtil;

public class EditFragmentNestCondition extends EditFragment {
  private static final Map<Integer, ClickHandler> CLICK_HANDLERS =
      ClickHandler.toMap(
          new HandleSetEggsScattered(),
          new HandleSetPoached(),
          new HandleSetPoachedDate(),
          new HandleSetRootsInvaded(),
          new HandleSetVandalized(),
          new HandleSetVandalizedDate());

  @Override
  public Map<Integer, ClickHandler> getClickHandlers() {
    return CLICK_HANDLERS;
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.edit_fragment_nest_condition, container, false);
  }

  @Override
  public void updateSection(Report report) {
    // TODO(edcoyne): hookup dynamic growth fields.

    NestCondition condition = report.getCondition();
    setChecked(R.id.fieldDamageVandalized, condition.getVandalized());
    setEnabled(R.id.buttonDamageVandalizedDate, condition.getVandalized());
    if (condition.hasVandalizedTimestampMs()) {
      setText(R.id.buttonDamageVandalizedDate,
          DateUtil.getFormattedDate(condition.getVandalizedTimestampMs()));
    }

    setChecked(R.id.fieldDamagePoached, condition.getPoached());
    setEnabled(R.id.buttonDamagePoachedDate, condition.getPoached());
    if (condition.hasPoachedTimestampMs()) {
      setText(R.id.buttonDamagePoachedDate,
          DateUtil.getFormattedDate(condition.getPoachedTimestampMs()));
    }

    setChecked(R.id.fieldDamageRootsInvaded, condition.getRootsInvadedEggshells());
    setChecked(R.id.fieldDamageEggsScattered, condition.getEggsScatteredByAnother());
  }

  private static class HandleSetVandalizedDate extends DatePickerClickHandler {
    protected HandleSetVandalizedDate() {
      super(R.id.buttonDamageVandalizedDate);
    }

    @Override
    public void onDateSet(DatePicker view,
        int year,
        int month,
        int day) {
      displayResult(updateHandler.updateVandalizedDate(year, month, day));
    }
  }
  private static class HandleSetPoachedDate extends DatePickerClickHandler {
    protected HandleSetPoachedDate() {
      super(R.id.buttonDamagePoachedDate);
    }

    @Override
    public void onDateSet(DatePicker view,
        int year,
        int month,
        int day) {
      displayResult(updateHandler.updatePoachedDate(year, month, day));
    }
  }

  private static class HandleSetVandalized extends ClickHandler {
    protected HandleSetVandalized() {
      super(R.id.fieldDamageVandalized);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateVandalized(isChecked(view));
    }
  }
  private static class HandleSetPoached extends ClickHandler {
    protected HandleSetPoached() {
      super(R.id.fieldDamagePoached);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updatePoached(isChecked(view));
    }
  }
  private static class HandleSetRootsInvaded extends ClickHandler {
    protected HandleSetRootsInvaded() {
      super(R.id.fieldDamageRootsInvaded);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateRootsInvaded(isChecked(view));
    }
  }
  private static class HandleSetEggsScattered extends ClickHandler {
    protected HandleSetEggsScattered() {
      super(R.id.fieldDamageEggsScattered);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateEggsScattered(isChecked(view));
    }
  }
}
