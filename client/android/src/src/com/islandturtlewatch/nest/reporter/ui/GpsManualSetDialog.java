package com.islandturtlewatch.nest.reporter.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

import com.google.common.base.Optional;
import com.islandturtlewatch.nest.data.ReportProto.GpsCoordinates;
import com.islandturtlewatch.nest.reporter.R;

public class GpsManualSetDialog extends DialogFragment {
  private Optional<GpsLocationCallback> callback;
  private Optional<AlertDialog> currentDialog = Optional.absent();

  public void setCallback(GpsLocationCallback callback) {
    this.callback = Optional.of(callback);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    EditText latEdit = (EditText) getActivity().findViewById(R.id.fieldGpsLat);
    final String lat = String.valueOf(latEdit.getText());
    EditText lonEdit = (EditText) getActivity().findViewById(R.id.fieldGpsLon);
    final String lon = String.valueOf(lonEdit.getText());

    DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int whichButton) {
      }
    } ;
    DialogInterface.OnClickListener acceptListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int whichButton) {
          callback.get().location(GpsCoordinates.newBuilder()
                  .setLat(Double.parseDouble(lat))
                  .setLong(Double.parseDouble(lon))
                  .build());
      }
    } ;

    if (!isDouble(lat) || !isDouble(lon)) {
      currentDialog = Optional.of(new AlertDialog.Builder(getActivity())
              .setIcon(R.drawable.ic_launcher)
              .setTitle("Bad Location Data")
              .setMessage("Latitude and Longitude must be entered as decimals only")
              .setNegativeButton(R.string.cancel, cancelListener)
              .create());
    } else {
      currentDialog = Optional.of(new AlertDialog.Builder(getActivity())
              .setIcon(R.drawable.ic_launcher)
              .setTitle("Manually Set GPS Location")
              .setMessage("Tap Accept to set the current gps location for this report \n" +
                      "Latitude: " + lat + "\nLongitude: " + lon +
                      "\nOtherwise select Cancel to return.")
              .setPositiveButton(R.string.accept, acceptListener)
              .setNegativeButton(R.string.cancel, cancelListener)
              .create());
    }
    return currentDialog.get();
  }

  private boolean isDouble(String test) {
    try {
      Double.parseDouble(test);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public interface GpsLocationCallback {
    public void location(GpsCoordinates coordinates);
  }

  }


