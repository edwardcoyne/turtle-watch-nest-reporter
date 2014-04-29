package com.islandturtlewatch.nest.reporter.ui;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import lombok.Getter;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateHandler;
import com.islandturtlewatch.nest.reporter.R;
import com.islandturtlewatch.nest.reporter.util.ErrorUtil;

public class EditFragmentMedia extends EditFragment {
  private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

  private static final HandleCaptureImage CAPTURE_IMAGE_HANDLER = new HandleCaptureImage();
  private static final Map<Integer, ClickHandler> CLICK_HANDLERS = ClickHandler.toMap(
      CAPTURE_IMAGE_HANDLER);
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

  @Override
  protected void updateSection(Report report) {
  }

  @Override
  public void handleIntentResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
      if (resultCode == Activity.RESULT_OK) {
        // Image captured and saved to fileUri specified in the Intent
        ErrorUtil.showErrorMessage(this, "Image saved to:\n"
            + CAPTURE_IMAGE_HANDLER.getLastImagePath()
            + ((data == null) ? "null" : data));
      } else {
        ErrorUtil.showErrorMessage(this, "Image capture failed. code:" + resultCode);
      }
    } else {
      super.handleIntentResult(requestCode, resultCode, data);
    }
  }

  private static class HandleCaptureImage extends ClickHandler {
    @Getter
    private Uri lastImagePath;

    HandleCaptureImage() {
      super(R.id.buttonCaptureImage);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      Log.v(TAG, "Capture Image clicked.");
      Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      Activity activity = (Activity) view.getContext();
      lastImagePath = getOutputPath(view);
      intent.putExtra(MediaStore.EXTRA_OUTPUT, lastImagePath);
      activity.startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    /** Create a File for saving an image or video */
    private static Uri getOutputPath(View view){
        File mediaStorageDir = new File(view.getContext().getExternalFilesDir(
                  Environment.DIRECTORY_PICTURES), "IslandTurtleWatch");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            UUID.randomUUID() + ".jpg");

        return Uri.fromFile(mediaFile);
    }
  }
}
