package com.islandturtlewatch.nest.reporter.ui;

import java.util.Map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.islandturtlewatch.nest.data.ReportProto.NestLocation;
import com.islandturtlewatch.nest.data.ReportProto.NestLocation.Placement;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateHandler;
import com.islandturtlewatch.nest.reporter.R;

public class EditFragmentNestLocation extends EditFragment {
  private static final String TAG = EditFragmentNestLocation.class.getSimpleName();

  private static final Map<Integer, ClickHandler> CLICK_HANDLERS =
      ClickHandler.toMap();

  private static final Map<Integer, TextChangeHandler> TEXT_CHANGE_HANDLERS =
      TextChangeHandler.toMap(
          new HandleUpdateAddress(),
          new HandleUpdateSectionNumber(),
          new HandleUpdateDetails(),
          new HandleUpdateApexToBarrier(),
          new HandleUpdateWaterToApex());

  @Override
  public Map<Integer, ClickHandler> getClickHandlers() {
    return CLICK_HANDLERS;
  }

  @Override
  public Map<Integer, TextChangeHandler> getTextChangeHandlers() {
    return TEXT_CHANGE_HANDLERS;
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
  		ViewGroup container,
      Bundle savedInstanceState) {
      return inflater.inflate(R.layout.edit_fragment_nest_location, container, false);
  }

  @Override
  public void updateSection(Report report) {
    NestLocation location = report.getLocation();

    setTextIfPresent(R.id.fieldAddress, location, "street_address");
    setTextIfPresent(R.id.fieldSectionNumber, location, "section");
    setTextIfPresent(R.id.fieldDetails, location, "details");
    setTextIfPresent(R.id.fieldApexToBarrier, location, "apex_to_barrier_ft");
    setTextIfPresent(R.id.fieldWaterToApex, location, "water_to_apex_ft");

    setChecked(R.id.fieldLocationOpenBeach, location.getPlacement() == Placement.OPEN_BEACH);

  }

  private static class HandleUpdateAddress extends TextChangeHandler {
    protected HandleUpdateAddress() {
      super(R.id.fieldAddress);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.updateStreetAddress(newText);
    }
  }

  private static class HandleUpdateSectionNumber extends TextChangeHandler {
    protected HandleUpdateSectionNumber() {
      super(R.id.fieldSectionNumber);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      int newValue = Integer.parseInt(newText);
      //TODO(edcoyne): display error if not parsable
      updateHandler.updateSectionNumber(newValue);
    }
  }

  private static class HandleUpdateDetails extends TextChangeHandler {
    protected HandleUpdateDetails() {
      super(R.id.fieldDetails);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.updateDetails(newText);
    }
  }

  private static class HandleUpdateApexToBarrier extends TextChangeHandler {
    protected HandleUpdateApexToBarrier() {
      super(R.id.fieldApexToBarrier);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      float newValue = Float.parseFloat(newText);
      //TODO(edcoyne): display error if not parsable
      updateHandler.updateApexToBarrier(newValue);
    }
  }

  private static class HandleUpdateWaterToApex extends TextChangeHandler {
    protected HandleUpdateWaterToApex() {
      super(R.id.fieldWaterToApex);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      float newValue = Float.parseFloat(newText);
      //TODO(edcoyne): display error if not parsable
      updateHandler.updateWaterToApex(newValue);
    }
  }
}
