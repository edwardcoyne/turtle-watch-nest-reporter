package com.islandturtlewatch.nest.reporter.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.common.base.Optional;
import com.islandturtlewatch.nest.data.ReportProto.Image;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateHandler;
import com.islandturtlewatch.nest.reporter.R;
import com.islandturtlewatch.nest.reporter.util.ErrorUtil;
import com.islandturtlewatch.nest.reporter.util.ImageUtil;

public class EditFragmentMedia extends EditFragment {
  private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 400;
  private static final int EDIT_IMAGE_ACTIVITY_REQUEST_CODE = 401;

  private static final int THUMBNAIL_WIDTH = 600;
  private static final int THUMBNAIL_HEIGHT = 450;

  private static final String KEY_CURRENT_FILE_PATH = "Media.CurrentFilePath";

  private static final HandleCaptureImage CAPTURE_IMAGE_HANDLER = new HandleCaptureImage();
  private static final Map<Integer, ClickHandler> CLICK_HANDLERS = ClickHandler.toMap(
      CAPTURE_IMAGE_HANDLER);
  private static final String TAG = EditFragmentMedia.class.getSimpleName();

  private Optional<Uri> currentlyEditingFileUri = Optional.absent();

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
    gridview.setAdapter(new ImageAdapter(report.getImageList()));
  }

  @Override
  public void saveState(Bundle bundle) {
    if (currentlyEditingFileUri.isPresent()) {
      bundle.putString(KEY_CURRENT_FILE_PATH, currentlyEditingFileUri.get().toString());
    }
  }

  @Override
  public void restoreState(Bundle bundle) {
    Optional<String> value = Optional.fromNullable(bundle.getString(KEY_CURRENT_FILE_PATH));
    if (value.isPresent()) {
      currentlyEditingFileUri = Optional.of(Uri.parse(value.get()));
    }
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
    } else if (requestCode == EDIT_IMAGE_ACTIVITY_REQUEST_CODE) {
      Log.d(TAG, "Edit Response: " + data);
      if (currentlyEditingFileUri.isPresent() && data != null) {
        try {
          ImageUtil.copyFromContentUri(
              this.getActivity(), data.getData(), currentlyEditingFileUri.get());
          listenerProvider.getUpdateHandler()
              .updatePhoto(ImageUtil.getFileName(currentlyEditingFileUri.get()));
        } catch (IOException e) {
          ErrorUtil.showErrorMessage(this, "Failed to save changes: " + e.getMessage());
          Log.e(TAG, "IOException while copying edited file: ", e);
        }
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

  private class ImageAdapter extends BaseAdapter {
    private final Bitmap dummyBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    private final List<Image> images;
    private final List<Bitmap> thumbnails = new ArrayList<>();

    public ImageAdapter(List<Image> images) {
      this.images = images;
      for (int i=0; i < images.size(); i++) {
        thumbnails.add(dummyBitmap);
      }
      new Thread(new Thumbnailer()).start();
    }

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

    public class Thumbnailer implements Runnable {
      @Override
      public void run() {
        for (int i = 0; i < images.size(); i++) {
          final Uri imagePath = ImageUtil.getImagePath(EditFragmentMedia.this.getActivity(),
              images.get(i).getFileName());
          Bitmap bitmap = BitmapFactory.decodeFile(imagePath.getPath());
          thumbnails.set(i,
              ThumbnailUtils.extractThumbnail(bitmap, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT));
        }
        EditFragmentMedia.this.getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            notifyDataSetChanged();
          }
        });
      }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      ImageButton imageView;
      if (convertView == null) {  // if it's not recycled, initialize some attributes
          imageView = new ImageButton(parent.getContext());
          imageView.setLayoutParams(new GridView.LayoutParams(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT));
          imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
          imageView.setPadding(8, 8, 8, 8);
      } else {
          imageView = (ImageButton) convertView;
      }

      final Activity activity = (Activity) parent.getContext();
      final String fileName = images.get(position).getFileName();
      final Uri imagePath = ImageUtil.getImagePath(parent, fileName);
      imageView.setImageBitmap(thumbnails.get(position));
      imageView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intent = new Intent();
          intent.setDataAndType(imagePath, "image/jpg");
          intent.setAction(Intent.ACTION_EDIT);
          currentlyEditingFileUri = Optional.of(imagePath);
          activity.startActivityForResult(intent, EDIT_IMAGE_ACTIVITY_REQUEST_CODE);
        }
      });
      return imageView;
    }
  }
}
