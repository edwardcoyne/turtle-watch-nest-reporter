package com.islandturtlewatch.nest.reporter.ui;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateHandler;
import com.islandturtlewatch.nest.reporter.R;
import com.islandturtlewatch.nest.reporter.data.Date;
import com.islandturtlewatch.nest.reporter.ui.split.SplitEditActivity.ListenerProvider;
import com.islandturtlewatch.nest.reporter.util.DateUtil;

public abstract class EditFragment extends Fragment {
  private static final String TAG = EditFragment.class.getSimpleName();
  public static final int UNDEFINED_VIEW_ID = -1;

  protected Optional<Report> currentReport = Optional.absent();
  protected Map<Message, Map<String, FieldDescriptor>> descriptors =
      new HashMap<Message, Map<String, FieldDescriptor>>();
  protected ListenerProvider listenerProvider;

  /**
   * Will be called to update the display contents based on information in report.
   */
  public final void updateDisplay(Report report) {
    currentReport = Optional.of(report);

    if (isDetached() || getActivity() == null) {
      // Will be updated when attached.
      return;
    }

    updateSection(report);
  }

  // Persist any working state, will be called when tombstoning.
  public void saveState(Bundle bundle) {}

  // Restore any working state, will be called when returning from tombstone.
  public void restoreState(Bundle bundle) {}

  /**
   * Override to update fragment's display.
   */
  protected abstract void updateSection(Report report);

  /**
   * Handlers for all click events.
   */
  public Map<Integer, ClickHandler> getClickHandlers() {
    return Collections.emptyMap();
  }

  /**
   * Handlers for MonitoredEditText change events.
   */
  public  Map<Integer, TextChangeHandler> getTextChangeHandlers() {
    return Collections.emptyMap();
  }

  public void handleIntentResult(int requestCode, int resultCode, Intent data) {
    Log.e(TAG, "Unhandled IntentResult, req:" + requestCode + " res:" + resultCode
        + " data:" + data);
  }

  @Override
  public void onStart() {
    if (currentReport.isPresent()) {
      updateSection(currentReport.get());
    }
    super.onStart();
  }

  public void setListenerProvider(ListenerProvider listenerProvider) {
    this.listenerProvider = listenerProvider;
  }

  protected TextView getTextView(int id) {
    View view = getActivity().findViewById(id);
    if (view instanceof TextView) { // This includes buttons and TextEdits
      return ((TextView) view);
    } else {
      throw new UnsupportedOperationException("We don't support getTextView on " + view);
    }
  }

  protected void setText(int id, String value) {
    View view = getActivity().findViewById(id);
    if (view instanceof TextView) { // This includes buttons and TextEdits
      ((TextView) view).setTextKeepState(value);
    } else {
      throw new UnsupportedOperationException("We don't support setText on " + view);
    }
  }

  protected void setDate(int id, long timestampMs) {
    setText(id, DateUtil.getFormattedDate(timestampMs));
    ClickHandler clickHandler = getClickHandlers().get(id);
    if (clickHandler instanceof DatePickerClickHandler) {
      ((DatePickerClickHandler)clickHandler).setDate(timestampMs);
    } else {
      throw new UnsupportedOperationException("We don't support setDate on " + clickHandler);
    }
  }

  protected void clearDate(int id) {
    setText(id, getString(R.string.date_button));
    ClickHandler clickHandler = getClickHandlers().get(id);
    if (clickHandler instanceof DatePickerClickHandler) {
      ((DatePickerClickHandler)clickHandler).clearDate();
    } else {
      throw new UnsupportedOperationException("We don't support setDate on " + clickHandler);
    }
  }

  protected void setChecked(int id, boolean checked) {
    View view = getActivity().findViewById(id);
    if (view instanceof CompoundButton) { // This includes CheckBoxes and ToggleButtons.
      ((CompoundButton) view).setChecked(checked);
    } else {
      throw new UnsupportedOperationException("We don't support setChecked on " + view);
    }
  }

  protected void setVisible(int id, boolean visible) {
    View view = getActivity().findViewById(id);
    view.setVisibility((visible) ? View.VISIBLE : View.GONE);
  }

  protected void setVisible(boolean visible, Iterable<Integer> ids) {
    for (Integer id : ids) {
      setVisible(id, visible);
    }
  }

  protected void setEnabled(int id, boolean enabled) {
    View view = getActivity().findViewById(id);
    if (view instanceof TextView) {
      ((TextView)view).setEnabled(enabled);
    } else {
      throw new UnsupportedOperationException("We don't support setEnabled on " + view);
    }
  }

  protected boolean isChecked(int id) {
    View view = getActivity().findViewById(id);
    return isChecked(view);
  }

  protected static boolean isChecked(View view) {
    if (view instanceof CompoundButton) {
      return ((CompoundButton) view).isChecked();
    } else {
      throw new UnsupportedOperationException("We don't support isChecked on " + view);
    }
  }

  protected static String getText(View view) {
    if (view instanceof TextView) {
      return ((TextView) view).getText().toString();
    } else {
      throw new UnsupportedOperationException("We don't support getText on " + view);
    }
  }

  protected static Optional<Integer> getInteger(String text) {
    if (text.isEmpty()) {
      return Optional.<Integer>absent();
    }
    try {
      int newValue = Integer.parseInt(text);
      return Optional.of(newValue);
    } catch (NumberFormatException ex) {
      //ErrorUtil.showErrorMessage(context, "Value is not a number: " + text);
      return Optional.absent();
    }
  }

  public static abstract class ClickHandler implements ClickHandlerSimple {
    protected final int resourceId;
    protected ClickHandler(int resourceId) {
      this.resourceId = resourceId;
    }

    static Map<Integer, ClickHandler> toMap(ClickHandler... clickHandlers) {
      ImmutableMap.Builder<Integer, ClickHandler> builder = ImmutableMap.builder();
      for (ClickHandler handler : clickHandlers) {
        builder.put(handler.resourceId, handler);
      }
      return builder.build();
    }
  }

  public interface ClickHandlerSimple {
    void handleClick(View view, DataUpdateHandler updateHandler);
  }

  public abstract static class DatePickerClickHandler extends ClickHandler
      implements ClearableDatePickerDialog.OnDateSetListener {
    protected DataUpdateHandler updateHandler;
    private Optional<Integer> year = Optional.absent();
    private Optional<Integer> month = Optional.absent();
    private Optional<Integer> day = Optional.absent();

    public DatePickerClickHandler(int viewId) {
      super(viewId);
    }

    public void setDate(long timstampMs) {
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(timstampMs);
      int year = this.year.or(cal.get(Calendar.YEAR));
      int month = this.month.or(cal.get(Calendar.MONTH));
      int day = this.day.or(cal.get(Calendar.DAY_OF_MONTH));
      setDate(year, month, day);
    }

    public void setDate(int year, int month, int day) {
      this.year = Optional.of(year);
      this.month = Optional.of(month);
      this.day = Optional.of(day);
    }

    public void clearDate() {
      this.year = Optional.absent();
      this.month = Optional.absent();
      this.day = Optional.absent();
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      this.updateHandler = updateHandler;
      if (this.year.isPresent() && this.month.isPresent() && this.day.isPresent()) {
        CurrentDatePicker.showOnView(view, new DateSetListener(),
            this.year.get(), this.month.get(), this.day.get());
      } else {
        CurrentDatePicker.showOnView(view, new DateSetListener());
      }
    }

    // Hide the stupid implementation detail of months being '0' indexed.
    private class DateSetListener implements ClearableDatePickerDialog.OnDateSetListener {
      @Override
      public void onDateSet(DatePicker view, Optional<Date> maybeDate) {
        DatePickerClickHandler.this.onDateSet(view, maybeDate);
      }
    }
  }

  // Should only be used in places that don't care about view_id, e.g. dynamically generated.
  public abstract static class SimpleDatePickerClickHandler extends DatePickerClickHandler {
    public SimpleDatePickerClickHandler() {
      super(UNDEFINED_VIEW_ID);
    }
  }

  public abstract static class TextChangeHandler implements TextChangeHandlerSimple {
    protected final int resourceId;

    protected TextChangeHandler(int resourceId) {
      this.resourceId = resourceId;
    }

    static Map<Integer, TextChangeHandler> toMap(TextChangeHandler... handlers) {
      ImmutableMap.Builder<Integer, TextChangeHandler> builder = ImmutableMap.builder();
      for (TextChangeHandler handler : handlers) {
        builder.put(handler.resourceId, handler);
      }
      return builder.build();
    }
  }

  public interface TextChangeHandlerSimple {
    void handleTextChange(String newText, DataUpdateHandler updateHandler);
  }
}
