package com.islandturtlewatch.nest.reporter.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.islandturtlewatch.nest.data.ReportProto;
import com.islandturtlewatch.nest.reporter.EditPresenter;
import com.islandturtlewatch.nest.reporter.R;
import com.islandturtlewatch.nest.reporter.data.Date;
import com.islandturtlewatch.nest.reporter.data.ReportMutations;
import com.islandturtlewatch.nest.reporter.util.DateUtil;

import java.util.Map;

/**
 * Created by ReverendCode on 4/21/16.
 */
public class EditFragmentStorms extends EditFragment {

    private static final Map<Integer, ClickHandler> CLICK_HANDLERS =
            ClickHandler.toMap(
                    new HandleSetPostHatchWashout(),
                    new HandleSetPartialWashoutDate(),
                    new HandleAddAccretionRow(),
                    new HandleSetStormImpactDate(),
                    new HandleSetWashoutDate());

    private static final Map<Integer, TextChangeHandler> TEXT_CHANGE_HANDLERS =
            TextChangeHandler.toMap(
                    new HandleUpdateWashoutStorm(),
                    new HandleUpdateStormImpactStormName(),
                    new HandleUpdateStormImpactOtherImpact(),
                    new HandleUpdatePartialWashoutStorm());
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
        return inflater.inflate(R.layout.edit_fragment_storms, container, false);

    }


    @Override
    public void updateSection(ReportProto.Report report) {
        final ReportProto.NestCondition condition = report.getCondition();
        setText(R.id.fieldWashOutStormName, condition.getWashOut().getStormName());
        setText(R.id.fieldPartialWashOutStormName, condition.getPartialWashout().getStormName());
        if (condition.getStormImpact().hasTimestampMs()) {
            setDate(R.id.buttonOtherStormImpactDate, condition.getStormImpact().getTimestampMs());
        } else {
            clearDate(R.id.buttonOtherStormImpactDate);
        }

        setText(R.id.fieldOtherStormImpactStormName,condition.getStormImpact().getStormName());
        setText(R.id.fieldOtherStormImpactOtherImpact, condition.getStormImpact().getOtherImpact());
        setVisible(R.id.fieldOtherStormImpactOtherImpact,condition.getStormImpact().hasTimestampMs());

        setEnabled(R.id.fieldPostHatchWashout, (
                condition.getPartialWashout().hasTimestampMs() ||
                        condition.getWashOut().hasTimestampMs()));

        setChecked(R.id.fieldPostHatchWashout, condition.getPostHatchWashout() &&
                (condition.getWashOut().hasTimestampMs() ||
                        condition.getPartialWashout().hasTimestampMs()));




        clearTable(R.id.tableWashOver);
        for (int i = 0; i < condition.getWashOverCount(); i++) {
            addWashOverRow(i, condition.getWashOver(i), true);
        }
        final TextView addWashOverRowText = (TextView) getView().findViewById(R.id.addWashOverRow);
        addWashOverRowText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addWashOverRow(condition.getWashOverCount(), ReportProto.NestCondition.WashEvent.getDefaultInstance(), false);
            }
        });


        clearTable(R.id.tableAccretionEvent);
        for (int i = 0; i < condition.getAccretionCount(); i++) {
            addAccretionRow(i,condition.getAccretion(i),true);
        }

        final TextView addAccretionRow = (TextView) getView().findViewById(R.id.fieldAddAccretionRow);
        addAccretionRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReportProto.NestCondition.WashEvent.Builder builder = ReportProto.NestCondition.WashEvent.newBuilder();
                builder.setStormName("");
                addAccretionRow(condition.getAccretionCount(),builder.build(),false);
            }
        });

        clearTable(R.id.tableInundatedEvent);
        for (int i = 0; i < condition.getInundatedEventCount();i++) {
            //add each inundatedEvent already in the Report
            addInundatedEventRow(i,condition.getInundatedEvent(i),true);
        }
        final TextView addInundationRow = (TextView) getView().findViewById(R.id.addInundationEventRow);
        addInundationRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addInundatedEventRow(condition.getInundatedEventCount(), ReportProto.NestCondition.WashEvent.getDefaultInstance(),false);

            }
        });



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
    }
    private void clearTable(int viewId) {
        TableLayout table = getTable(viewId);
        // since we use focus listeners, MUST ensure no focus before deletion.
        removeFocus(table);
        table.removeAllViews();
    }


    private static class HandleSetPostHatchWashout extends ClickHandler {
        protected HandleSetPostHatchWashout() {
            super(R.id.fieldPostHatchWashout);
        }
        @Override
        public void handleClick(View view, EditPresenter.DataUpdateHandler updateHandler) {
            updateHandler.applyMutation(new ReportMutations.WasPostHatchWashout(isChecked(view)));
        }
    }


    private static class HandleUpdatePartialWashoutStorm extends TextChangeHandler {
        protected HandleUpdatePartialWashoutStorm() {
            super(R.id.fieldPartialWashOutStormName);
        }

        @Override
        public void handleTextChange(String newName, EditPresenter.DataUpdateHandler updateHandler) {
            updateHandler.applyMutation(new ReportMutations.PartialWashoutStormNameMutation(newName));
        }
    }


    private static class HandleSetWashoutDate extends DatePickerClickHandler {
        protected HandleSetWashoutDate() {
            super(R.id.buttonWashOutDate);
        }

        @Override
        public void onDateSet(DatePicker view, Optional<Date> maybeDate) {
            updateHandler.applyMutation(new ReportMutations.WashoutDateMutation(maybeDate));
        }
    }

    private static class HandleSetPartialWashoutDate extends DatePickerClickHandler {
        protected HandleSetPartialWashoutDate() {
            super(R.id.buttonPartialWashOutDate);
        }

        @Override
        public void onDateSet (DatePicker view, Optional<Date> maybeDate) {
            updateHandler.applyMutation(new ReportMutations.PartialWashoutDateMutation(maybeDate));
        }
    }


    private static class HandleAddAccretionRow extends ClickHandler {
        protected HandleAddAccretionRow () {
            super(R.id.fieldAddAccretionRow);
        }
        @Override
        public void handleClick(View view, EditPresenter.DataUpdateHandler updateHandler) {

        }
    }


    private static class HandleSetStormImpactDate extends DatePickerClickHandler {
        protected HandleSetStormImpactDate() {
            super(R.id.buttonOtherStormImpactDate);
        }

        @Override
        public void onDateSet(DatePicker view, Optional<Date> maybeDate) {
            updateHandler.applyMutation(new ReportMutations.OtherImpactDateMutation(maybeDate));
        }
    }

    private static class HandleUpdateStormImpactStormName extends TextChangeHandler {
        protected HandleUpdateStormImpactStormName() {
            super(R.id.fieldOtherStormImpactStormName);
        }

        @Override
        public void handleTextChange(String newName, EditPresenter.DataUpdateHandler updateHandler) {
            updateHandler.applyMutation(new ReportMutations.OtherImpactStormNameMutation(newName));
        }
    }

    private static class HandleUpdateStormImpactOtherImpact extends TextChangeHandler {
        protected HandleUpdateStormImpactOtherImpact() {
            super(R.id.fieldOtherStormImpactOtherImpact);
        }

        @Override
        public void handleTextChange(String newName, EditPresenter.DataUpdateHandler updateHandler) {
            updateHandler.applyMutation(new ReportMutations.OtherImpactDetailsMutation(newName));
        }
    }


    private static class HandleUpdateWashoutStorm extends TextChangeHandler {
        protected HandleUpdateWashoutStorm() {
            super(R.id.fieldWashOutStormName);
        }

        @Override
        public void handleTextChange(String newText, EditPresenter.DataUpdateHandler updateHandler) {
            updateHandler.applyMutation(new ReportMutations.WashoutStormNameMutation(newText));
        }
    }


    private void addAccretionRow(final int ordinal, ReportProto.NestCondition.WashEvent accretion, boolean showDelete) {
        Button date_button = new Button(getActivity());
        if (accretion.hasTimestampMs()) {
            date_button.setText(DateUtil.getFormattedDate(accretion.getTimestampMs()));
        } else {
            date_button.setText(R.string.date_button);
        }

        SimpleDatePickerClickHandler clickHandler = new SimpleDatePickerClickHandler(){
            @Override
            public void onDateSet(DatePicker view, Optional<Date> maybeDate) {
                updateHandler.applyMutation(new ReportMutations.AccretionDateMutation(ordinal, maybeDate));
            }};
        if (accretion.hasTimestampMs()) {
            clickHandler.setDate(accretion.getTimestampMs());
        } else {
            clickHandler.setDate(System.currentTimeMillis());
        }
        date_button.setOnClickListener(listenerProvider.getOnClickListener(clickHandler));

        FocusMonitoredEditText storm_name = new FocusMonitoredEditText(getActivity());
        storm_name.setHint(R.string.edit_nest_condition_storm_name);
        storm_name.setText(accretion.getStormName());
        listenerProvider.setFocusLossListener(storm_name, new TextChangeHandlerSimple() {
            @Override
            public void handleTextChange(String newText, EditPresenter.DataUpdateHandler updateHandler) {
                updateHandler.applyMutation(new ReportMutations.AccretionStormNameMutation(ordinal, newText));
            }
        });

        Button delete = new Button(getActivity());
        delete.setText("X");
        delete.setOnClickListener(listenerProvider.getOnClickListener(new ClickHandlerSimple() {
            @Override
            public void handleClick(View view, EditPresenter.DataUpdateHandler updateHandler) {
                updateHandler.applyMutation(new ReportMutations.DeleteAccretionMutation(ordinal));
            }
        }));

        TableRow row = new TableRow(getActivity());
        row.setId(ordinal);
        row.addView(date_button);
        storm_name.setLayoutParams(
                new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(storm_name);
        if (showDelete) {
            row.addView(delete);
        }
        getTable(R.id.tableAccretionEvent).addView(row);

    }

    private void addWashOverRow(final int ordinal, ReportProto.NestCondition.WashEvent event, boolean showDelete) {
        Button date_button = new Button(getActivity());
        if (event.hasTimestampMs()) {
            date_button.setText(DateUtil.getFormattedDate(event.getTimestampMs()));
        } else {
            date_button.setText(R.string.date_button);
        }

        SimpleDatePickerClickHandler clickHandler = new SimpleDatePickerClickHandler(){
            @Override
            public void onDateSet(DatePicker view, Optional<Date> maybeDate) {
                updateHandler.applyMutation(new ReportMutations.WashoverDateMutation(ordinal, maybeDate));
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
            public void handleTextChange(String newText, EditPresenter.DataUpdateHandler updateHandler) {
                updateHandler.applyMutation(new ReportMutations.WashoverStormNameMutation(ordinal, newText));
            }
        });

        Button delete = new Button(getActivity());
        delete.setText("X");
        delete.setOnClickListener(listenerProvider.getOnClickListener(new ClickHandlerSimple() {
            @Override
            public void handleClick(View view, EditPresenter.DataUpdateHandler updateHandler) {
                updateHandler.applyMutation(new ReportMutations.DeleteWashOverMutation(ordinal));
            }
        }));

        TableRow row = new TableRow(getActivity());
        row.setId(ordinal);
        row.addView(date_button);
        storm_name.setLayoutParams(
                new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(storm_name);
        if (showDelete) {
            row.addView(delete);
        }
        getTable(R.id.tableWashOver).addView(row);
    }

    private void addInundatedEventRow (final int ordinal, ReportProto.NestCondition.WashEvent event, boolean showDelete) {
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
            public void handleTextChange(String newText, EditPresenter.DataUpdateHandler updateHandler) {
                updateHandler.applyMutation(new ReportMutations.InundatedEventStormNameMutation(ordinal, newText));
            }
        });

        Button delete = new Button(getActivity());
        delete.setText("X");
        delete.setOnClickListener(listenerProvider.getOnClickListener(new ClickHandlerSimple() {
            @Override
            public void handleClick(View view, EditPresenter.DataUpdateHandler updateHandler) {
                updateHandler.applyMutation(new ReportMutations.DeleteInundatedEventMutation(ordinal));
            }
        }));

        TableRow row = new TableRow(getActivity());
        row.setId(ordinal);
        row.addView(date_button);
        storm_name.setLayoutParams(
                new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(storm_name);
        if (showDelete) {
            row.addView(delete);
        }
        getTable(R.id.tableInundatedEvent).addView(row);
    }
    private TableLayout getTable(int viewId) {
        View view = getActivity().findViewById(viewId);
        Preconditions.checkArgument(view instanceof TableLayout);
        return (TableLayout)view;
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
}


