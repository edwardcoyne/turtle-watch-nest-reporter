package com.islandturtlewatch.nest.reporter.ui;

import android.content.Context;
import android.graphics.Rect;
import android.widget.EditText;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateHandler;
import com.islandturtlewatch.nest.reporter.ui.EditFragment.TextChangeHandlerSimple;

public class FocusMonitoredEditText extends EditText {
  private Optional<TextChangeHandlerSimple> handler = Optional.absent();
  private Optional<DataUpdateHandler> updateHandler = Optional.absent();

  public FocusMonitoredEditText(Context context) {
    super(context);
  }

  public void setTextChangeHandler(TextChangeHandlerSimple handler) {
    this.handler = Optional.of(handler);
  }

  public void setDataUpdateHandler(DataUpdateHandler handler) {
    this.updateHandler = Optional.of(handler);
  }

  @Override
  protected void onFocusChanged(boolean focused, int direction,
      Rect previouslyFocusedRect) {
    super.onFocusChanged(focused, direction, previouslyFocusedRect);
    if (focused) {
      return;
    }
    if (handler.isPresent()) {
      Preconditions.checkArgument(updateHandler.isPresent(), "must call setDataUpdateHandler()");
      handler.get().handleTextChange(getText().toString(), updateHandler.get());
    }
  }

  public void updateValues() {
    if (handler.isPresent()) {
      Preconditions.checkArgument(updateHandler.isPresent(), "must call setDataUpdateHandler()");
      handler.get().handleTextChange(getText().toString(),updateHandler.get());
    }
  }
}
