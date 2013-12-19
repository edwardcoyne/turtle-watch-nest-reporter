package com.islandturtlewatch.nest.reporter.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.ImmutableMap;
import com.islandturtlewatch.nest.reporter.R;

public class EditFragmentMedia extends EditFragment {
	private static final String TAG = EditFragmentMedia.class.getSimpleName();
	private static final ImmutableMap<Integer, ClickHandler> clickHandlerMap = ClickHandler.toMap(
			new HandleCaptureImage());

	@Override
	public View onCreateView(LayoutInflater inflater,
			ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.edit_fragment_media, container, false);
	}

	@Override
  public ImmutableMap<Integer, ClickHandler> getClickHandlers() {
		return clickHandlerMap;
  }

	private static class HandleCaptureImage extends ClickHandler {
		HandleCaptureImage() {
			super(R.id.buttonCaptureImage);
		}

		@Override
    public void handleClick(View view) {
			Log.w(TAG, "Take picture");
    }
	}
}
