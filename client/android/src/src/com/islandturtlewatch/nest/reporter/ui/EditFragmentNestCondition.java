package com.islandturtlewatch.nest.reporter.ui;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.islandturtlewatch.nest.data.ReportProto.NestCondition;
import com.islandturtlewatch.nest.data.ReportProto.NestCondition.PreditationEvent;
import com.islandturtlewatch.nest.data.ReportProto.NestCondition.WashEvent;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateHandler;
import com.islandturtlewatch.nest.reporter.R;
import com.islandturtlewatch.nest.reporter.data.Date;
import com.islandturtlewatch.nest.reporter.data.ReportMutations;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.DeletePredationMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.DeleteWashOverMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.PartialWashoutDateMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.PartialWashoutStormNameMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.PoachedDateMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.PoachedEggsRemovedMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.PredationDateMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.PredationNumEggsMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.PredationPredatorMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.RootsInvadedMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.VandalismTypeMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.VandalizedDateMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.WasPoachedMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.WasVandalizedMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.WashoutDateMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.WashoutStormNameMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.WashoverDateMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.WashoverStormNameMutation;
import com.islandturtlewatch.nest.reporter.ui.ClearableDatePickerDialog.OnDateSetListener;
import com.islandturtlewatch.nest.reporter.util.DateUtil;

import java.util.Map;

public class EditFragmentNestCondition extends EditFragment {

  private final int NUM_PREDATORS = 13;
  private static final Map<Integer, ClickHandler> CLICK_HANDLERS =
      ClickHandler.toMap(
//          new HandleSetEggsScattered(),
//          new HandleSetEggsScatteredDate(),
          new HandleSetPostHatchWashout(),
          new HandleSetPoached(),
          new HandleSetPoachedDate(),
          new HandleSetPoachedEggsRemoved(),
          new HandleSetRootsInvaded(),
          new HandleSetNestDugInto(),
          new HandleSetVandalized(),
          new HandleSetVandalizedDate(),
          new HandleSetVandalismStakesRemoved(),
          new HandleSetVandalismDugInto(),
          new HandleSetVandalismEggsAffected(),
          new HandleSetWashoutDate(),
          new HandleSetPartialWashoutDate(),
          new HandleSetProportionAll(),
          new HandleSetProportionMost(),
          new HandleSetProportionSome(),
          new HandleSetProportionFew(),
          new HandleSetPredatorDate(),
          new HandleSetActivelyRecordPredationEvents(),
              //NestInundated is deprecated, use new inundatedEvent instead
//          new HandleSetNestInundated(),
//          new HandleSetNestInundatedDate(),
          new HandleSetNestDepredation(),
          new HandleUpdateEggsDamagedByAnotherTurtle());

  private static final Map<Integer, OnItemSelectedHandler> ITEM_SELECTED_HANDLERS =
          OnItemSelectedHandler.toMap(
              new HandleSelectPredator()
                  //add handlers
          );

  private static final Map<Integer, TextChangeHandler> TEXT_CHANGE_HANDLERS =
      TextChangeHandler.toMap(
              new HandleUpdateWashoutStorm(),
              new HandleDescribeControlMethods(),
              new HandleUpdateNumEggs(),
              new HandleUpdatePredatorOther(),
              new HandleUpdatePartialWashoutStorm());

  @Override
  public Map<Integer, TextChangeHandler> getTextChangeHandlers() {
    return TEXT_CHANGE_HANDLERS;
  }

  @Override
  public Map<Integer,OnItemSelectedHandler> getOnItemSelectedHandlers() {return ITEM_SELECTED_HANDLERS;}

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

    clearTable(R.id.tableInundatedEvent);
    for (int i = 0; i < condition.getInundatedEventCount();i++) {
      //add each inundatedEvent already in the Report
      addInundatedEventRow(i,condition.getInundatedEvent(i),true);
    }
    //add one blank line as well.
    addInundatedEventRow(condition.getInundatedEventCount(),WashEvent.getDefaultInstance(),false);

    if (condition.getWashOut().hasTimestampMs()) {
      setDate(R.id.buttonWashOutDate,
              condition.getWashOut().getTimestampMs());
    } else {
      clearDate(R.id.buttonWashOutDate);
    }

    if (condition.getPartialWashout().hasTimestampMs()) {
      setDate(R.id.buttonPartialWashOutDate,
              condition.getPartialWashout().getTimestampMs());
    } else {
      clearDate(R.id.buttonPartialWashOutDate);
    }

    setChecked(R.id.fieldRecordedAll,condition.getPropEventsRecorded() == NestCondition.ProportionEventsRecorded.ALL);
    setChecked(R.id.fieldRecordedMost, condition.getPropEventsRecorded() == NestCondition.ProportionEventsRecorded.MOST);
    setChecked(R.id.fieldRecordedSome,condition.getPropEventsRecorded() == NestCondition.ProportionEventsRecorded.SOME);
    setChecked(R.id.fieldRecordedFew, condition.getPropEventsRecorded() == NestCondition.ProportionEventsRecorded.FEW);

    setText(R.id.fieldWashOutStormName, condition.getWashOut().getStormName());
    setText(R.id.fieldPartialWashOutStormName, condition.getPartialWashout().getStormName());
    setText(R.id.fieldDescribeControlMethods,condition.getDescribeControlMethods());

if (condition.getPreditationCount()>0) {
  if (condition.getPreditation(0).hasTimestampMs()) {
    setDate(R.id.buttonPredatorDate, condition.getPreditation(0).getTimestampMs());
  } else clearDate(R.id.buttonPredatorDate);

  if (condition.getPreditation(0).hasNumberOfEggs()) {
    setText(R.id.fieldNumberEggs, Integer.toString(condition.getPreditation(0).getNumberOfEggs()));
  } else setText(R.id.fieldNumberEggs, "");

  Spinner pSpinner = (Spinner) getActivity().findViewById(R.id.fieldPredatorSelect);
  boolean showFieldOther;

  if (condition.getPreditation(0).hasPredator()) {
    int predNum = getPredatorIndex(condition.getPreditation(0).getPredator());
    showFieldOther = (predNum == NUM_PREDATORS);
    pSpinner.setSelection(getPredatorIndex(condition.getPreditation(0).getPredator()));
  } else {
    showFieldOther = false;
    pSpinner.setSelection(0);
  }
  setVisible(R.id.fieldPredatorOther, showFieldOther);
  setText(R.id.fieldPredatorOther, condition.getPreditation(0).getPredator());
}
//    clearTable(R.id.tablePredatitation);
//    for (int i = 0; i < condition.getPreditationCount(); i++) {
//      addPredationRow(i, condition.getPreditation(i), true);
//    }
//    // Add blank line.
//    addPredationRow(condition.getPreditationCount(), PreditationEvent.getDefaultInstance(), false);

    setChecked(R.id.fieldDamageEggsDamagedByAnotherTurtle,condition.getEggsDamagedByAnotherTurtle());
    setChecked(R.id.fieldDamageNestDepredated, condition.getNestDepredated());
//    setChecked(R.id.fieldDamageNestInundated, condition.getNestInundated());
//    setEnabled(R.id.buttonDamageNestInundatedDate,condition.getNestInundated());
//    if (condition.hasNestInundatedTimestampMs()) {
//      setDate(R.id.buttonDamageNestInundatedDate, condition.getNestInundatedTimestampMs());
//    } else {
//      clearDate(R.id.buttonDamageNestInundatedDate);
//    }
    setChecked(R.id.fieldDamageVandalized, condition.getVandalized());
    setVisible(R.id.fieldProvideDetailsText,condition.getVandalized());

    setEnabled(R.id.buttonDamageVandalizedDate, condition.getVandalized());
    if (condition.hasVandalizedTimestampMs()) {
      setDate(R.id.buttonDamageVandalizedDate, condition.getVandalizedTimestampMs());
    } else {
      clearDate(R.id.buttonDamageVandalizedDate);
    }
    setVisible(R.id.rowVandalismType, condition.getVandalized());
    setChecked(R.id.fieldVandalismStakesRemoved,
            condition.getVandalismType() == NestCondition.VandalismType.STAKES_REMOVED);
    setChecked(R.id.fieldVandalismDugInto,
        condition.getVandalismType() == NestCondition.VandalismType.NEST_DUG_INTO);
    setChecked(R.id.fieldVandalismEggsAffected,
        condition.getVandalismType() == NestCondition.VandalismType.EGGS_AFFECTED);

    setEnabled(R.id.fieldPostHatchWashout, (
            condition.getPartialWashout().hasTimestampMs() ||
                    condition.getWashOut().hasTimestampMs()));

    setChecked(R.id.fieldPostHatchWashout, condition.getPostHatchWashout() &&
            (condition.getWashOut().hasTimestampMs() ||
                    condition.getPartialWashout().hasTimestampMs()));

    setChecked(R.id.fieldNestDugInto,condition.getNestDugInto());
    setChecked(R.id.fieldDamagePoached, condition.getPoached());
    setEnabled(R.id.buttonDamagePoachedDate, condition.getPoached());
    if (condition.hasPoachedTimestampMs()) {
      setDate(R.id.buttonDamagePoachedDate, condition.getPoachedTimestampMs());
    } else {
      clearDate(R.id.buttonDamagePoachedDate);
    }
    setVisible(R.id.rowPoachedDetails, condition.getPoached());
    setChecked(R.id.fieldDamagePoachedEggsRemoved, condition.getPoachedEggsRemoved());

//    setChecked(R.id.fieldDamageEggsScattered, condition.getEggsScatteredByAnother());
//    setEnabled(R.id.buttonDamageEggsScatteredDate, condition.getEggsScatteredByAnother());
//    if (condition.hasEggsScatteredByAnotherTimestampMs()) {
//      setDate(R.id.buttonDamageEggsScatteredDate, condition.getEggsScatteredByAnotherTimestampMs());
//    } else {
//      clearDate(R.id.buttonDamageEggsScatteredDate);
//    }

    setChecked(R.id.fieldDamageRootsInvaded, condition.getRootsInvadedEggshells());
    setChecked(R.id.fieldActivelyRecordEvents,condition.getActivelyRecordEvents());
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
      public void onDateSet(DatePicker view, Optional<Date> maybeDate) {
        updateHandler.applyMutation(new WashoverDateMutation(ordinal, maybeDate));
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
      @Override
      public void handleClick(View view, DataUpdateHandler updateHandler) {
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

  private void addInundatedEventRow (final int ordinal, WashEvent event,  boolean showDelete) {
    Button date_button = new Button(getActivity());
    if (event.hasTimestampMs()) {
      date_button.setText(DateUtil.getFormattedDate(event.getTimestampMs()));
    } else {
      date_button.setText(R.string.date_button);
    }

    SimpleDatePickerClickHandler clickHandler = new SimpleDatePickerClickHandler() {
      @Override
      public void onDateSet(DatePicker view, Optional<Date> maybeDate) {
        updateHandler.applyMutation(new ReportMutations.InundatedEventDateMutation(ordinal, maybeDate));
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
        updateHandler.applyMutation(new ReportMutations.InundatedEventStormNameMutation(ordinal, newText));
      }
    });

    Button delete = new Button(getActivity());
    delete.setText("X");
    delete.setOnClickListener(listenerProvider.getOnClickListener(new ClickHandlerSimple() {
      @Override
      public void handleClick(View view, DataUpdateHandler updateHandler) {
        updateHandler.applyMutation(new ReportMutations.DeleteInundatedEventMutation(ordinal));
      }
    }));

    TableRow row = new TableRow(getActivity());
    row.setId(ordinal);
    row.addView(date_button);
    storm_name.setLayoutParams(
            new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, 1f));
    row.addView(storm_name);
    if (showDelete) {
      row.addView(delete);
    }
    getTable(R.id.tableInundatedEvent).addView(row);
  }

  private void addPredationRow(final int ordinal, final PreditationEvent event, boolean showDelete) {
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
              DatePicker view, Optional<Date> maybeDate) {
            updateHandler.applyMutation(
                new PredationDateMutation(ordinal, maybeDate));
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
    final FocusMonitoredEditText predator = new FocusMonitoredEditText(getActivity());
    predator.setHint(R.string.edit_nest_condition_predator);
    predator.setText(event.getPredator());
    //TODO: add logic to display when "Other" is selected
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

    Spinner predatorSpinner = new Spinner(getActivity());
    ArrayAdapter predatorAdapter = ArrayAdapter.createFromResource(getActivity(),R.array.predator_array,
            android.R.layout.simple_spinner_item);
    predatorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    predatorSpinner.setAdapter(predatorAdapter);
    predator.setVisibility(View.INVISIBLE);

  if (event.hasPredator()) {
    predatorSpinner.setSelection(getPredatorIndex(event.getPredator()));
  }else {
    predatorSpinner.setSelection(0);
  }
    predatorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String mPredator = parent.getItemAtPosition(position).toString();

        if (mPredator.equals("Other")) {
          predator.setVisibility(View.VISIBLE);
          predator.setText("");

        } else if (position == 0){
          //on start
        }else {

        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });

    TableRow row = new TableRow(getActivity());
    TableRow row2 = new TableRow(getActivity());
    TableRow row3 = new TableRow(getActivity());
    row.addView(date_button);
    num_eggs.setLayoutParams(
        new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
    row.addView(num_eggs);
    predatorSpinner.setLayoutParams(
        new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
    row2.addView(predatorSpinner);
    row3.addView(predator);
    if (showDelete) {
      row.addView(delete);
    }
    getTable(R.id.tablePredatitation).addView(row);
    getTable(R.id.tablePredatitation).addView(row2);
    getTable(R.id.tablePredatitation).addView(row3);

  }

  private int getPredatorIndex(String predator) {//this is ugly, but I am dumb
    int index = getResources().getStringArray(R.array.predator_array).length-1;
//    int index = 0;
    String[] testArray = getResources().getStringArray(R.array.predator_array);
    for (int i = 0; i < testArray.length;i++) {
      if (predator.compareTo(testArray[i]) == 0) index = i;
    }
    return index;
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

  private static class HandleSetProportionAll extends ClickHandler {
    protected HandleSetProportionAll () {
      super(R.id.fieldRecordedAll);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ReportMutations.
              PropEventsRecordedMutation(NestCondition.ProportionEventsRecorded.ALL));
    }
  }

  private static class HandleSetProportionMost extends ClickHandler {
    protected HandleSetProportionMost () {
      super(R.id.fieldRecordedMost);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ReportMutations.
              PropEventsRecordedMutation(NestCondition.ProportionEventsRecorded.MOST));
    }
  }

  private static class HandleSetProportionSome extends ClickHandler {
    protected HandleSetProportionSome () {
      super(R.id.fieldRecordedSome);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ReportMutations.
              PropEventsRecordedMutation(NestCondition.ProportionEventsRecorded.SOME));
    }
  }

  private static class HandleSetProportionFew extends ClickHandler {
    protected HandleSetProportionFew () {
      super(R.id.fieldRecordedFew);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ReportMutations.
              PropEventsRecordedMutation(NestCondition.ProportionEventsRecorded.FEW));
    }
  }

  private static class HandleSetPredatorDate extends DatePickerClickHandler {
    protected HandleSetPredatorDate() {
      super(R.id.buttonPredatorDate);
    }

    @Override
    public void onDateSet(DatePicker view, Optional<Date> maybeDate) {
      updateHandler.applyMutation(new PredationDateMutation(0,maybeDate));
    }
  }

  private static class HandleSetWashoutDate extends DatePickerClickHandler {
    protected HandleSetWashoutDate() {
      super(R.id.buttonWashOutDate);
    }

    @Override
    public void onDateSet(DatePicker view, Optional<Date> maybeDate) {
      updateHandler.applyMutation(new WashoutDateMutation(maybeDate));
    }
  }

  private static class HandleSetPartialWashoutDate extends DatePickerClickHandler {
    protected HandleSetPartialWashoutDate() {
      super(R.id.buttonPartialWashOutDate);
    }

    @Override
    public void onDateSet (DatePicker view, Optional<Date> maybeDate) {
      updateHandler.applyMutation(new PartialWashoutDateMutation(maybeDate));
    }
  }

  private static class HandleSetVandalizedDate extends DatePickerClickHandler {
    protected HandleSetVandalizedDate() {
      super(R.id.buttonDamageVandalizedDate);
    }

    @Override
    public void onDateSet(DatePicker view, Optional<Date> maybeDate) {
      updateHandler.applyMutation(new VandalizedDateMutation(maybeDate));
    }
  }
  private static class HandleSetPoachedDate extends DatePickerClickHandler {
    protected HandleSetPoachedDate() {
      super(R.id.buttonDamagePoachedDate);
    }

    @Override
    public void onDateSet(DatePicker view, Optional<Date> maybeDate) {
      updateHandler.applyMutation(new PoachedDateMutation(maybeDate));
    }
  }

//  private static class HandleSetNestInundatedDate extends DatePickerClickHandler {
//    protected HandleSetNestInundatedDate() {
//      super(R.id.buttonDamageNestInundatedDate);
//    }
//
//    @Override
//    public void onDateSet(DatePicker view, Optional<Date> maybeDate) {
//      updateHandler.applyMutation(new ReportMutations.NestInundatedDateMutation(maybeDate));
//    }
//  }
//  private static class HandleSetNestInundated extends ClickHandler {
//    protected HandleSetNestInundated() {
//      super(R.id.fieldDamageNestInundated);
//    }
//
//    @Override
//    public void handleClick(View view, DataUpdateHandler updateHandler) {
//      updateHandler.applyMutation(new ReportMutations.NestInundatedMutation(isChecked(view)));
//    }
//  }

//  private static class HandleSetEggsScatteredDate extends DatePickerClickHandler {
//    protected HandleSetEggsScatteredDate() { super(R.id.buttonDamageEggsScatteredDate); }
//
//    @Override
//    public void onDateSet(DatePicker view, Optional<Date> maybeDate) {
//      updateHandler.applyMutation(new EggsScatteredDateMutation(maybeDate));
//    }
//  }

  private static class HandleSetNestDugInto extends ClickHandler {
    protected HandleSetNestDugInto() {
      super(R.id.fieldNestDugInto);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ReportMutations.WasNestDugIntoMutation(isChecked(view)));
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

  private static class HandleSetPostHatchWashout extends ClickHandler {
    protected HandleSetPostHatchWashout() {
      super(R.id.fieldPostHatchWashout);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ReportMutations.WasPostHatchWashout(isChecked(view)));
    }
  }


  private static class HandleSetActivelyRecordPredationEvents extends ClickHandler {
    protected HandleSetActivelyRecordPredationEvents() {
      super(R.id.fieldActivelyRecordEvents);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ReportMutations.ActivelyRecordPredationMutation(isChecked(view)));
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
  private static class HandleSetPoachedEggsRemoved extends ClickHandler {
    protected HandleSetPoachedEggsRemoved() {
      super(R.id.fieldDamagePoachedEggsRemoved);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new PoachedEggsRemovedMutation(isChecked(view)));
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
//  private static class HandleSetEggsScattered extends ClickHandler {
//    protected HandleSetEggsScattered() {
//      super(R.id.fieldDamageEggsScattered);
//    }
//    @Override
//    public void handleClick(View view, DataUpdateHandler updateHandler) {
//      updateHandler.applyMutation(new EggsScatteredMutation(isChecked(view)));
//    }
//  }

  private static class HandleSetVandalismStakesRemoved extends ClickHandler {
    protected HandleSetVandalismStakesRemoved() {
      super(R.id.fieldVandalismStakesRemoved);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new VandalismTypeMutation(!isChecked(view) ?
          Optional.<NestCondition.VandalismType>absent() :
          Optional.of(NestCondition.VandalismType.STAKES_REMOVED)));
    }
  }
  private static class HandleSetVandalismDugInto extends ClickHandler {
    protected HandleSetVandalismDugInto() {
      super(R.id.fieldVandalismDugInto);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new VandalismTypeMutation(!isChecked(view) ?
          Optional.<NestCondition.VandalismType>absent() :
          Optional.of(NestCondition.VandalismType.NEST_DUG_INTO)));
    }
  }


  private static class HandleSetVandalismEggsAffected extends ClickHandler {
    protected HandleSetVandalismEggsAffected() {
      super(R.id.fieldVandalismEggsAffected);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new VandalismTypeMutation(!isChecked(view) ?
          Optional.<NestCondition.VandalismType>absent() :
          Optional.of(NestCondition.VandalismType.EGGS_AFFECTED)));
    }
  }

  private static class HandleSetNestDepredation extends ClickHandler {
    protected HandleSetNestDepredation() {
      super(R.id.fieldDamageNestDepredated);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ReportMutations.NestDepredatedMutation(isChecked(view)));
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
  private static class HandleSelectPredator extends OnItemSelectedHandler {

    protected HandleSelectPredator() {
      super(R.id.fieldPredatorSelect);
    }
    @Override
    public void handleItemSelected(String selectedPredator, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ReportMutations.PredationPredatorMutation(0,selectedPredator));
    }
  }

  private static class HandleUpdateEggsDamagedByAnotherTurtle extends ClickHandler {
    protected HandleUpdateEggsDamagedByAnotherTurtle() {
      super(R.id.fieldDamageEggsDamagedByAnotherTurtle);
    }
    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ReportMutations.EggsDamagedByAnotherTurtleMutation(isChecked(view)));
    }
  }


  private static class HandleUpdateNumEggs extends TextChangeHandler {
    protected HandleUpdateNumEggs() {
      super(R.id.fieldNumberEggs);
    }

    @Override
    public void handleTextChange(String newName, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ReportMutations.PredationNumEggsMutation(0,getInteger(newName)));
    }
  }
  private static class HandleDescribeControlMethods extends TextChangeHandler {
    protected HandleDescribeControlMethods() {
      super(R.id.fieldDescribeControlMethods);
    }

    @Override
    public void handleTextChange(String newName, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ReportMutations.ControlMethodDescriptionMutation(newName));
    }
  }

  private static class HandleUpdatePredatorOther extends TextChangeHandler {
    protected HandleUpdatePredatorOther() {
      super(R.id.fieldPredatorOther);
    }

    @Override
    public void handleTextChange(String newName, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ReportMutations.PredationPredatorMutation(0,newName));
    }
  }

  private static class HandleUpdatePartialWashoutStorm extends TextChangeHandler {
    protected HandleUpdatePartialWashoutStorm() {
      super(R.id.fieldPartialWashOutStormName);
    }

    @Override
    public void handleTextChange(String newName, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new PartialWashoutStormNameMutation(newName));
    }
  }

}
