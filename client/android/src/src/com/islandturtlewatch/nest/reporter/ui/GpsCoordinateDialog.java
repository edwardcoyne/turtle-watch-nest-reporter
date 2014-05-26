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
  private static final float accuracyThresholdM = 3f;

  private LocationManager locationManager;
  private final Listener listener = new Listener();
  private Optional<GpsLocationCallback> callback;
  private Optional<AlertDialog> currentDialog = Optional.absent();

  public void setCallback(GpsLocationCallback callback) {
    this.callback = Optional.of(callback);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    locationManager =
        (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);

    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);

    DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int whichButton) {
        locationManager.removeUpdates(listener);
      }
    } ;

    currentDialog = Optional.of(new AlertDialog.Builder(getActivity())
      .setIcon(R.drawable.ic_launcher)
      .setTitle("Getting GPS coordinates, Please wait...")
      .setMessage("Acquiring signal.")
      .setPositiveButton(R.string.accept, cancelListener)
      .setNegativeButton(R.string.cancel, cancelListener)
      .create());
    return currentDialog.get();
  }

  public interface GpsLocationCallback {
    public void location(GpsCoordinates coordinates);
  }

  private class Listener implements LocationListener {
    Optional<Location> bestLocation = Optional.absent();
    @Override
    public void onLocationChanged(Location newLocation) {
      Log.d(TAG, "Got new GPS Location: " + newLocation);
      if (!newLocation.hasAccuracy() || newLocation.getAccuracy() > accuracyThresholdM) {
        final Location bestLocation = updateBest(newLocation);
        currentDialog.get().setMessage("Current best accuracy is "
            + bestLocation.getAccuracy() + "m. Last was (" + newLocation.getAccuracy() +"m)\n"
            + "Our threshold is " + accuracyThresholdM + "m.\n"
            + "Hit Accept to use the best we have so far.");
        currentDialog.get().setButton(AlertDialog.BUTTON_POSITIVE, "Accept",
            new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            callback.get().location(GpsCoordinates.newBuilder()
              .setLat(bestLocation.getLatitude())
              .setLong(bestLocation.getLongitude())
              .build());
            locationManager.removeUpdates(listener);
            GpsCoordinateDialog.this.dismiss();
          }
        });
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

    private Location updateBest(Location location) {
      if (!bestLocation.isPresent()
          || bestLocation.get().getAccuracy() > location.getAccuracy()) {
        bestLocation = Optional.of(location);
      }
      return bestLocation.get();
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
