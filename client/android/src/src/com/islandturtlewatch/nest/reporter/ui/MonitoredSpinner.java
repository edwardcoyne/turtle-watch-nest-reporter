package com.islandturtlewatch.nest.reporter.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.google.common.base.Optional;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by ReverendCode on 4/11/16.
 */
public class MonitoredSpinner extends Spinner {
    private Optional<String> itemSelectedMethodName = Optional.absent();
    public MonitoredSpinner(Context context, AttributeSet attributeSet) {
        super(context,attributeSet);
        for (int i=0; i < attributeSet.getAttributeCount(); i++) {
            String name = attributeSet.getAttributeName(i);
            if (name.equals("OnItemSelected")) {
                itemSelectedMethodName = Optional.of(attributeSet.getAttributeValue(i));
            }
        }

        setOnItemSelectedListener(new SelectionMonitor(this));
    }

    private class SelectionMonitor implements OnItemSelectedListener {
        private final View view;
        private Optional<Method> handler = Optional.absent();

        public SelectionMonitor(View view) {
            this.view = view;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String newText = parent.getItemAtPosition(position).toString();

            if (itemSelectedMethodName.isPresent()) {
                if (!handler.isPresent()) {
                    try {
                        handler = Optional.of(getContext().getClass().getMethod(itemSelectedMethodName.get(),
                                View.class, String.class));
                    } catch (NoSuchMethodException e) {
                        throw new IllegalStateException(e);
                    }
                }
                try {

                    handler.get().invoke(getContext(), parent, newText);
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
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
}
