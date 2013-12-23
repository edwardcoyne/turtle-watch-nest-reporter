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
 * {@link EditText} that has a xml attribute of onTextChangeHandler and will notify that method
 * when there are changes made to the text.
 */
public class MonitoredEditText extends EditText {
  private Optional<String> textListenerMethodName = Optional.absent();

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

  private class TextMonitor implements TextWatcher {
    private final View view;
    private Optional<Method> handler = Optional.absent();

    public TextMonitor(View view) {
      this.view = view;
    }

    @Override
    public void afterTextChanged(Editable newText) {
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
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
  }
}
