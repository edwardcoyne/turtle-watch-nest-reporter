package com.islandturtlewatch.nest.reporter.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.common.base.Optional;
import com.islandturtlewatch.nest.data.ReportProto.GpsCoordinates;
import com.islandturtlewatch.nest.reporter.R;

public class GpsCoordinateDialog extends DialogFragment {
  private static final String TAG = GpsCoordinateDialog.class.getSimpleName();
  private static final float accuracyThresholdM = 20.0f;

  private LocationManager locationManager;
  private final Listener listener = new Listener();
  private Optional<GpsLocationCallback> callback;

  //Optional<GpsCoordinates> coordinates = Optional.absent();

  public void setCallback(GpsLocationCallback callback) {
    this.callback = Optional.of(callback);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    locationManager =
        (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);

    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);

    return new AlertDialog.Builder(getActivity())
      .setIcon(R.drawable.ic_launcher)
      .setTitle("Getting GPS coordinates, Please wait...")
      .setNegativeButton(R.string.cancel,
          new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int whichButton) {
                  // TODO(edcoyne): implement cancel.
                locationManager.removeUpdates(listener);
              }
          }
      )
      .create();
  }

  public interface GpsLocationCallback {
    public void location(GpsCoordinates coordinates);
  }

  private class Listener implements LocationListener {
    @Override
    public void onLocationChanged(Location newLocation) {
      Log.d(TAG, "Got new GPS Location: " + newLocation);
      if (!newLocation.hasAccuracy() || newLocation.getAccuracy() > accuracyThresholdM) {
        Log.d(TAG, "Not accurate enough");
        return;
      }

      callback.get().location(GpsCoordinates.newBuilder()
        .setLat(newLocation.getLatitude())
        .setLong(newLocation.getLongitude())
        .build());
      locationManager.removeUpdates(listener);
      GpsCoordinateDialog.this.dismiss();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
      Log.d(TAG, "Status changed " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
      Log.d(TAG, "Enabled ");
      }

    @Override
    public void onProviderDisabled(String provider) {
      Log.d(TAG, "Disabled ");
    }
  }

}
