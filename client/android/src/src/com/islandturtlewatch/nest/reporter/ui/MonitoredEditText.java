package com.islandturtlewatch.nest.reporter.ui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.common.base.Optional;

/**
 * {@link EditText} that has a xml attribute of onTextChangeHandler and will be notified when there
 * are changes made to the value.
 *
 * <p> Programmatic changes through setText* will not be reported to the listener.
 */
public class MonitoredEditText extends EditText {
  private Optional<String> textListenerMethodName = Optional.absent();
  private boolean isUpdating = false;

  public MonitoredEditText(Context context, AttributeSet attrs) {
    super(context, attrs);
    for (int i=0; i < attrs.getAttributeCount(); i++) {
      String name = attrs.getAttributeName(i);
      Log.d(MonitoredEditText.class.getSimpleName(), name);
      if (name.equals("onTextChangeHandler")) {
        textListenerMethodName = Optional.of(attrs.getAttributeValue(i));
      }
    }
    addTextChangedListener(new TextMonitor(this));
  }


  @Override
  public void setText(CharSequence text, BufferType type) {
    isUpdating = true;
    super.setText(text, type);
    isUpdating = false;
  }

  private class TextMonitor implements TextWatcher {
    private final View view;
    private Optional<Method> handler = Optional.absent();

    public TextMonitor(View view) {
      this.view = view;
    }

    @Override
    public void afterTextChanged(Editable newText) {
      if (isUpdating) {
        return;
      }

      if (textListenerMethodName.isPresent()) {
        if (!handler.isPresent()) {
          try {
            handler = Optional.of(getContext().getClass().getMethod(textListenerMethodName.get(),
                View.class, String.class));
          } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
          }
        }

        try {
          handler.get().invoke(getContext(), view, newText.toString());
        } catch (IllegalAccessException e) {
          throw new IllegalStateException("Could not execute non "
              + "public method of the activity", e);
        } catch (InvocationTargetException e) {
          throw new IllegalStateException("Could not execute "
              + "method of the activity", e);
        }
      }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}
  }
}
