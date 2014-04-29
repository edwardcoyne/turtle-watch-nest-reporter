package com.islandturtlewatch.nest.reporter.ui;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.islandturtlewatch.nest.data.ReportProto.Image;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateHandler;
import com.islandturtlewatch.nest.reporter.R;
import com.islandturtlewatch.nest.reporter.util.ErrorUtil;
import com.islandturtlewatch.nest.reporter.util.ImageUtil;

public class EditFragmentMedia extends EditFragment {
  private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

  private static final HandleCaptureImage CAPTURE_IMAGE_HANDLER = new HandleCaptureImage();
  private static final Map<Integer, ClickHandler> CLICK_HANDLERS = ClickHandler.toMap(
      CAPTURE_IMAGE_HANDLER);
  private static final String TAG = EditFragmentMedia.class.getSimpleName();

  private final ImageAdapter imageAdapter = new ImageAdapter();

  @Override
  public Map<Integer, ClickHandler> getClickHandlers() {
    return CLICK_HANDLERS;
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.edit_fragment_media, container, false);
    return view;
  }

  @Override
  protected void updateSection(Report report) {
    GridView gridview = (GridView) getActivity().findViewById(R.id.galleryView);
    gridview.setAdapter(imageAdapter);
    imageAdapter.setImages(report.getImageList());
    imageAdapter.notifyDataSetChanged();
  }

  @Override
  public void handleIntentResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
      if (resultCode == Activity.RESULT_OK) {
        // Image captured and saved to fileUri specified in the Intent
        ErrorUtil.showErrorMessage(this, "Image saved :\n"
            + CAPTURE_IMAGE_HANDLER.getActiveImageFileName());
        listenerProvider.getUpdateHandler()
            .addPhoto(CAPTURE_IMAGE_HANDLER.getActiveImageFileName());

      } else {
        ErrorUtil.showErrorMessage(this, "Image capture failed. code:" + resultCode);
      }
    } else {
      super.handleIntentResult(requestCode, resultCode, data);
    }
  }

  private static class HandleCaptureImage extends ClickHandler {
    @Getter
    private String activeImageFileName;

    HandleCaptureImage() {
      super(R.id.buttonCaptureImage);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      Log.v(TAG, "Capture Image clicked.");
      Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      Activity activity = (Activity) view.getContext();
      activeImageFileName = ImageUtil.createNewFileName();
      intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageUtil.getImagePath(view, activeImageFileName));
      activity.startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }
  }

  private static class ImageAdapter extends BaseAdapter {
    @Setter
    private List<Image> images = Collections.emptyList();

    @Override
    public int getCount() {
      return images.size();
    }

    @Override
    public Object getItem(int position) {
      return images.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      ImageView imageView;
      if (convertView == null) {  // if it's not recycled, initialize some attributes
          imageView = new ImageView(parent.getContext());
          imageView.setLayoutParams(new GridView.LayoutParams(500, 500));
          imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
          imageView.setPadding(8, 8, 8, 8);
      } else {
          imageView = (ImageView) convertView;
      }

      imageView.setImageURI(ImageUtil.getImagePath(parent, images.get(position).getFileName()));
      return imageView;
    }
  }
}
