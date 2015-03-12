package com.islandturtlewatch.nest.reporter.ui;

import java.util.Map;

import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.common.base.Preconditions;
import com.islandturtlewatch.nest.data.ReportProto.NestCondition;
import com.islandturtlewatch.nest.data.ReportProto.NestCondition.PreditationEvent;
import com.islandturtlewatch.nest.data.ReportProto.NestCondition.WashEvent;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateHandler;
import com.islandturtlewatch.nest.reporter.R;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.DeletePredationMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.DeleteWashOverMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.EggsScatteredMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.PoachedDateMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.PredationDateMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.PredationNumEggsMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.PredationPredatorMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.RootsInvadedMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.VandalizedDateMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.WasPoachedMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.WasVandalizedMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.WashoutDateMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.WashoutStormNameMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.WashoverDateMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.WashoverStormNameMutation;
import com.islandturtlewatch.nest.reporter.util.DateUtil;

public class EditFragmentNestCondition extends EditFragment {
  private static final Map<Integer, ClickHandler> CLICK_HANDLERS =
      ClickHandler.toMap(
          new HandleSetEggsScattered(),
          new HandleSetPoached(),
          new HandleSetPoachedDate(),
          new HandleSetRootsInvaded(),
          new HandleSetVandalized(),
          new HandleSetVandalizedDate(),
          new HandleSetWashoutDate());

  private static final Map<Integer, TextChangeHandler> TEXT_CHANGE_HANDLERS =
      TextChangeHandler.toMap(new HandleUpdateWashoutStorm());

  @Override
  public Map<Integer, TextChangeHandler> getTextChangeHandlers() {
    return TEXT_CHANGE_HANDLERS;
  }

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
    NestCondition condition = report.getCondition();

    clearTable(R.id.tableWashOver);
    for (int i = 0; i < condition.getWashOverCount(); i++) {
      addWashOverRow(i, condition.getWashOver(i), true);
    }
    // Add blank line.
    addWashOverRow(condition.getWashOverCount(), WashEvent.getDefaultInstance(), false);

    if (condition.getWashOut().hasTimestampMs()) {
      setDate(R.id.buttonWashOutDate, condition.getWashOut().getTimestampMs());
    } else {
      clearDate(R.id.buttonWashOutDate);
    }

    setText(R.id.fieldWashOutStormName, condition.getWashOut().getStormName());

    clearTable(R.id.tablePredatitation);
    for (int i = 0; i < condition.getPreditationCount(); i++) {
      addPredationRow(i, condition.getPreditation(i), true);
    }
    // Add blank line.
    addPredationRow(condition.getPreditationCount(), PreditationEvent.getDefaultInstance(), false);

    setChecked(R.id.fieldDamageVandalized, condition.getVandalized());
    setEnabled(R.id.buttonDamageVandalizedDate, condition.getVandalized());
    if (condition.hasVandalizedTimestampMs()) {
      setDate(R.id.buttonDamageVandalizedDate, condition.getVandalizedTimestampMs());
    } else {
      clearDate(R.id.buttonDamageVandalizedDate);
    }

    setChecked(R.id.fieldDamagePoached, condition.getPoached());
    setEnabled(R.id.buttonDamagePoachedDate, condition.getPoached());
    if (condition.hasPoachedTimestampMs()) {
      setDate(R.id.buttonDamagePoachedDate, condition.getPoachedTimestampMs());
    } else {
      clearDate(R.id.buttonDamagePoachedDate);
    }

    setChecked(R.id.fieldDamageRootsInvaded, condition.getRootsInvadedEggshells());
    setChecked(R.id.fieldDamageEggsScattered, condition.getEggsScatteredByAnother());
  }

  private void addWashOverRow(final int ordinal, WashEvent event, boolean showDelete) {
    Button date_button = new Button(getActivity());
    if (event.hasTimestampMs()) {
      date_button.setText(DateUtil.getFormattedDate(event.getTimestampMs()));
    } else {
      date_button.setText(R.string.date_button);
    }

    SimpleDatePickerClickHandler clickHandler = new SimpleDatePickerClickHandler(){
      @Override
      public void onDateSet(DatePicker view, int year, int monthOfYear,
          int dayOfMonth) {
        updateHandler.applyMutation(
            new WashoverDateMutation(ordinal, year, monthOfYear, dayOfMonth));
      }};
    if (event.hasTimestampMs()) {
      clickHandler.setDate(event.getTimestampMs());
    } else {
      clickHandler.setDate(System.currentTimeMillis());
    }
    date_button.setOnClickListener(listenerProvider.getOnClickListener(clickHandler));

    FocusMonitoredEditText storm_name = new FocusMonitoredEditText(getActivity());
    storm_name.setHint(R.string.edit_nest_condition_storm_name);
    storm_name.setText(event.getStormName());
    listenerProvider.setFocusLossListener(storm_name, new TextChangeHandlerSimple() {
        @Override
        public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
          updateHandler.applyMutation(new WashoverStormNameMutation(ordinal, newText));
        }
      });

    Button delete = new Button(getActivity());
    delete.setText("X");
    delete.setOnClickListener(listenerProvider.getOnClickListener(new ClickHandlerSimple() {
      @Override public void handleClick(View view, DataUpdateHandler updateHandler) {
        updateHandler.applyMutation(new DeleteWashOverMutation(ordinal));
      }
    }));

    TableRow row = new TableRow(getActivity());
    row.setId(ordinal);
    row.addView(date_button);
    storm_name.setLayoutParams(
        new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
    row.addView(storm_name);
    if (showDelete) {
      row.addView(delete);
    }
    getTable(R.id.tableWashOver).addView(row);
  }

  private void addPredationRow(final int ordinal, PreditationEvent event, boolean showDelete) {
    Button date_button = new Button(getActivity());
    if (event.hasTimestampMs()) {
      date_button.setText(DateUtil.getFormattedDate(event.getTimestampMs()));
    } else {
      date_button.setText(R.string.date_button);
    }

    date_button.setOnClickListener(listenerProvider.getOnClickListener(new ClickHandlerSimple() {
      @Override public void handleClick(View view, final DataUpdateHandler updateHandler) {
        CurrentDatePicker.showOnView(view, new OnDateSetListener() {
          @Override
          public void onDateSet(
              DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            updateHandler.applyMutation(
                new PredationDateMutation(ordinal, year, monthOfYear, dayOfMonth));
          }
        });
      }
    }));

    FocusMonitoredEditText num_eggs = new FocusMonitoredEditText(getActivity());
    num_eggs.setHint(R.string.edit_nest_condition_num_eggs);
    num_eggs.setInputType(InputType.TYPE_CLASS_NUMBER);
    num_eggs.setText(event.hasNumberOfEggs() ? Integer.toString(event.getNumberOfEggs()) : "");
    listenerProvider.setFocusLossListener(num_eggs, new TextChangeHandlerSimple() {
        @Override
        public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
          updateHandler.applyMutation(new PredationNumEggsMutation(ordinal, getInteger(newText)));
        }
      });
    FocusMonitoredEditText predator = new FocusMonitoredEditText(getActivity());
    predator.setHint(R.string.edit_nest_condition_predator);
    predator.setText(event.getPredator());
    listenerProvider.setFocusLossListener(predator, new TextChangeHandlerSimple() {
        @Override
        public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
          updateHandler.applyMutation(new PredationPredatorMutation(ordinal, newText));
        }
      });

    Button delete = new Button(getActivity());
    delete.setText("X");
    delete.setOnClickListener(listenerProvider.getOnClickListener(new ClickHandlerSimple() {
      @Override public void handleClick(View view, DataUpdateHandler updateHandler) {
        updateHandler.applyMutation(new DeletePredationMutation(ordinal));
      }
    }));

    TableRow row = new TableRow(getActivity());
    row.addView(date_button);
    num_eggs.setLayoutParams(
        new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
    row.addView(num_eggs);
    predator.setLayoutParams(
        new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
    row.addView(predator);
    if (showDelete) {
      row.addView(delete);
    }
    getTable(R.id.tablePredatitation).addView(row);
  }

  private void clearTable(int viewId) {
    TableLayout table = getTable(viewId);
    // since we use focus listeners, MUST ensure no focus before deletion.
    removeFocus(table);
    table.removeAllViews();
  }

  private void removeFocus(View view) {
    if (view instanceof ViewGroup) {
      ViewGroup group = (ViewGroup) view;
      for (int i=0; i < group.getChildCount(); i++) {
        removeFocus(group.getChildAt(i));
      }
    } else {
      if (view.hasFocus()) {
        view.clearFocus();
        view.requestFocus();
      }
    }
  }

  private TableLayout getTable(int viewId) {
    View view = getActivity().findViewById(viewId);
    Preconditions.checkArgument(view instanceof TableLayout);
    return (TableLayout)view;
  }

  private static class HandleSetWashoutDate extends DatePickerClickHandler {
    protected HandleSetWashoutDate() {
      super(R.id.buttonWashOutDate);
    }

    @Override
    public void onDateSet(DatePicker view,
        int year,
        int month,
        int day) {
      updateHandler.applyMutation(new WashoutDateMutation(year, month, day));
    }
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
      updateHandler.applyMutation(new VandalizedDateMutation(year, month, day));
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
      updateHandler.applyMutation(new PoachedDateMutation(year, month, day));
    }
  }

  private static class HandleSetVandalized extends ClickHandler {
    protected HandleSetVandalized() {
      super(R.id.fieldDamageVandalized);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new WasVandalizedMutation(isChecked(view)));
    }
  }
  private static class HandleSetPoached extends ClickHandler {
    protected HandleSetPoached() {
      super(R.id.fieldDamagePoached);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new WasPoachedMutation(isChecked(view)));
    }
  }
  private static class HandleSetRootsInvaded extends ClickHandler {
    protected HandleSetRootsInvaded() {
      super(R.id.fieldDamageRootsInvaded);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new RootsInvadedMutation(isChecked(view)));
    }
  }
  private static class HandleSetEggsScattered extends ClickHandler {
    protected HandleSetEggsScattered() {
      super(R.id.fieldDamageEggsScattered);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new EggsScatteredMutation(isChecked(view)));
    }
  }

  private static class HandleUpdateWashoutStorm extends TextChangeHandler {
    protected HandleUpdateWashoutStorm() {
      super(R.id.fieldWashOutStormName);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new WashoutStormNameMutation(newText));
    }
  }
}
