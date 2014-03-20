package com.islandturtlewatch.nest.reporter.ui;

import java.util.HashMap;
import java.util.Map;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateHandler;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateResult;

public class EditFragment extends Fragment {
  private static final String TAG = EditFragment.class.getSimpleName();

  protected Optional<Report> currentReport = Optional.absent();
  protected Map<Message, Map<String, FieldDescriptor>> descriptors =
      new HashMap<Message, Map<String, FieldDescriptor>>();

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

  /**
   * Override to update fragment's display.
   */
  // TODO(edcoyne): make abstract once we are no longer using generic EditFragments.
  protected void updateSection(Report report) {

  }


  @Override
  public void onStart() {
    if (currentReport.isPresent()) {
      updateSection(currentReport.get());
    }
    super.onStart();
  }

  // TODO(edcoyne): make abstract once we are no longer using generic EditFragments.
  public Map<Integer, ClickHandler> getClickHandlers() {
    return null;
  }

//TODO(edcoyne): make abstract once we are no longer using generic EditFragments.
 public Map<Integer, TextChangeHandler> getTextChangeHandlers() {
   return null;
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

  public static abstract class ClickHandler {
    protected final int resourceId;
    protected ClickHandler(int resourceId) {
      this.resourceId = resourceId;
    }

    protected void displayResult(DataUpdateResult result) {
      if (result.isSuccess()) {
        Log.d(TAG, "Update successful");
      } else {
        Log.e(TAG, "Update failed: " + ((result.hasErrorMessage()) ? result.getErrorMessage() : ""));
        //TODO (edcoyne): add dialog with error message for user.
      }
    }

    static Map<Integer, ClickHandler> toMap(ClickHandler... clickHandlers) {
      ImmutableMap.Builder<Integer, ClickHandler> builder = ImmutableMap.builder();
      for (ClickHandler handler : clickHandlers) {
        builder.put(handler.resourceId, handler);
      }
      return builder.build();
    }

    public abstract void handleClick(View view, DataUpdateHandler updateHandler);
  }

  public abstract static class DatePickerClickHandler extends ClickHandler
      implements DatePickerDialog.OnDateSetListener{
    protected DataUpdateHandler updateHandler;
    public DatePickerClickHandler(int viewId) {
      super(viewId);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      this.updateHandler = updateHandler;
      CurrentDatePicker.showOnView(view, this);
    }
  }

  public abstract static class TextChangeHandler{
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

    public abstract void handleTextChange(String newText, DataUpdateHandler updateHandler);
  }
}
