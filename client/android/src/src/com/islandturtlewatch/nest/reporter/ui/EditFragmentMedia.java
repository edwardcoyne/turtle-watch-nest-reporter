package com.islandturtlewatch.nest.reporter.ui;

import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateHandler;
import com.islandturtlewatch.nest.reporter.R;

public class EditFragmentMedia extends EditFragment {
  private static final Map<Integer, ClickHandler> CLICK_HANDLERS = ClickHandler.toMap(
      new HandleCaptureImage());
  private static final String TAG = EditFragmentMedia.class.getSimpleName();

  @Override
  public Map<Integer, ClickHandler> getClickHandlers() {
    return CLICK_HANDLERS;
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.edit_fragment_media, container, false);
  }

  private static class HandleCaptureImage extends ClickHandler {
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    HandleCaptureImage() {
      super(R.id.buttonCaptureImage);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      Log.v(TAG, "Capture Image clicked.");
      Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      Activity activity = (Activity) view.getContext();
      activity.startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }
  }
}
