package com.islandturtlewatch.nest.reporter.ui;

import java.util.Map;

import android.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateHandler;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateResult;

public class EditFragment extends Fragment {
  private static final String TAG = EditFragment.class.getSimpleName();

  protected Optional<Report> currentReport = Optional.absent();

  /**
   * Will be called to update the display contents based on information in report.
   */
  public final void updateDisplay(Report report) {
    currentReport = Optional.of(report);
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

  protected void setText(int id, String value) {
    View view = getActivity().findViewById(id);
    if (view instanceof TextView) {
      ((TextView) view).setText(value);
    } else {
      throw new UnsupportedOperationException("We don't support setText on " + view);
    }
  }

  protected void setChecked(int id, boolean checked) {
    View view = getActivity().findViewById(id);
    if (view instanceof CheckBox) {
      ((CheckBox) view).setChecked(checked);
    } else {
      throw new UnsupportedOperationException("We don't support setChecked on " + view);
    }
  }

  public static abstract class ClickHandler {
    private final int resourceId;
    protected ClickHandler(int resourceId) {
      this.resourceId = resourceId;
    }

    int getResourceId() {
      return resourceId;
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
}
