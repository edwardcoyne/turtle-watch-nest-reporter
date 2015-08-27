package com.islandturtlewatch.nest.reporter.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.islandturtlewatch.nest.data.ReportProto.GpsCoordinates;
import com.islandturtlewatch.nest.data.ReportProto.NestLocation;
import com.islandturtlewatch.nest.data.ReportProto.NestLocation.City;
import com.islandturtlewatch.nest.data.ReportProto.NestLocation.Placement;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateHandler;
import com.islandturtlewatch.nest.reporter.R;
import com.islandturtlewatch.nest.reporter.data.ReportMutations;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.ApexToBarrierFtMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.ApexToBarrierInMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.CityMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.DetailsMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.GpsMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.ObstructionsEscarpmentMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.ObstructionsFurnitureMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.ObstructionsOtherMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.ObstructionsSeawallRocksMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.PlacementMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.StreetAddressMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.WaterToApexFtMutation;
import com.islandturtlewatch.nest.reporter.data.ReportMutations.WaterToApexInMutation;
import com.islandturtlewatch.nest.reporter.ui.GpsCoordinateDialog.GpsLocationCallback;
import com.islandturtlewatch.nest.reporter.util.GpsUtil;

import java.util.Map;

public class EditFragmentNestLocation extends EditFragment {
  //private static final String TAG = EditFragmentNestLocation.class.getSimpleName();

  private static final Map<Integer, ClickHandler> CLICK_HANDLERS =
      ClickHandler.toMap(
          new HandleSetOpenBeach(),
          new HandleSetInVegitation(),
          new HandleSetAtVegitation(),
          new HandleSetAtEscarpment(),
          new HandleSetOnEscarpment(),
          new HandleSetSeawallRocks(),
          new HandleSetFurniture(),
          new HandleSetObstructionsEscarpment(),
          new HandleSetCityAM(),
          new HandleSetCityHB(),
          new HandleSetCityBB(),
          new HandleSetEscarpmentOver18Inches(),
          new HandleSetGps());

  private static final Map<Integer, TextChangeHandler> TEXT_CHANGE_HANDLERS =
      TextChangeHandler.toMap(
          new HandleUpdateAddress(),
          new HandleUpdateDetails(),
          new HandleUpdateApexToBarrierFt(),
          new HandleUpdateApexToBarrierIn(),
          new HandleUpdateWaterToApexFt(),
          new HandleUpdateWaterToApexIn(),
          new HandleUpdateObstructionsOther());

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

    if (report.getLocation().hasCoordinates()) {
      setText(R.id.buttonGps, GpsUtil.format(report.getLocation().getCoordinates()));
    } else {
      setText(R.id.buttonGps, getString(R.string.edit_nest_location_button_gps));
    }

    setText(R.id.fieldAddress, location.hasStreetAddress() ?
        location.getStreetAddress() : "");
    setChecked(R.id.fieldLocationAM, location.getCity() == City.AM);
    setChecked(R.id.fieldLocationHB, location.getCity() == City.HB);
    setChecked(R.id.fieldLocationBB, location.getCity() == City.BB);

    setText(R.id.fieldDetails, location.hasDetails() ?
            location.getDetails() : "");

    setText(R.id.fieldApexToBarrier_ft, location.hasApexToBarrierFt() ?
        Integer.toString(location.getApexToBarrierFt()) : "");
    setText(R.id.fieldApexToBarrier_in, location.hasApexToBarrierIn() ?
        Integer.toString(location.getApexToBarrierIn()) : "");

    setText(R.id.fieldWaterToApex_ft, location.hasWaterToApexFt() ?
        Integer.toString(location.getWaterToApexFt()) : "");
    setText(R.id.fieldWaterToApex_in, location.hasWaterToApexIn() ?
        Integer.toString(location.getWaterToApexIn()) : "");

    setChecked(R.id.fieldLocationOpenBeach, location.getPlacement() == Placement.OPEN_BEACH);
    setChecked(R.id.fieldLocationInVegitation, location.getPlacement() == Placement.IN_VEGITATION);
    setChecked(R.id.fieldLocationAtVegitation, location.getPlacement() == Placement.AT_VEGITATION);
    setChecked(R.id.fieldLocationAtEscarpment, location.getPlacement() == Placement.AT_ESCARPMENT);
    setChecked(R.id.fieldLocationOnEscarpment, location.getPlacement() == Placement.ON_ESCARPMENT);
    setChecked(R.id.fieldLocationEscarpmentOver18Inches,location.getEscarpmentOver18Inches());

    setChecked(R.id.fieldObstructionsSeawallRocks, location.getObstructions().getSeawallRocks());
    setChecked(R.id.fieldObstructionsFurniture, location.getObstructions().getFurniture());
    setChecked(R.id.fieldObstructionsEscarpment, location.getObstructions().getEscarpment());
    setText(R.id.fieldObstructionsOther, location.getObstructions().hasOther() ?
        location.getObstructions().getOther() : "");
  }

  private static class HandleUpdateAddress extends TextChangeHandler {
    protected HandleUpdateAddress() {
      super(R.id.fieldAddress);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new StreetAddressMutation(newText));
    }
  }

  private static class HandleUpdateDetails extends TextChangeHandler {
    protected HandleUpdateDetails() {
      super(R.id.fieldDetails);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new DetailsMutation(newText));
    }
  }

  private static class HandleUpdateApexToBarrierFt extends TextChangeHandler {
    protected HandleUpdateApexToBarrierFt() {
      super(R.id.fieldApexToBarrier_ft);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ApexToBarrierFtMutation(getInteger(newText)));
    }
  }

  private static class HandleUpdateApexToBarrierIn extends TextChangeHandler {
    protected HandleUpdateApexToBarrierIn() {
      super(R.id.fieldApexToBarrier_in);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ApexToBarrierInMutation(getInteger(newText)));
    }
  }

  private static class HandleUpdateWaterToApexFt extends TextChangeHandler {
    protected HandleUpdateWaterToApexFt() {
      super(R.id.fieldWaterToApex_ft);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new WaterToApexFtMutation(getInteger(newText)));
    }
  }

  private static class HandleUpdateWaterToApexIn extends TextChangeHandler {
    protected HandleUpdateWaterToApexIn() {
      super(R.id.fieldWaterToApex_in);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new WaterToApexInMutation(getInteger(newText)));
    }
  }

  private static class HandleSetCityAM extends ClickHandler {
    protected HandleSetCityAM() {
      super(R.id.fieldLocationAM);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new CityMutation(City.AM));
    }
  }

  private static class HandleSetCityHB extends ClickHandler {
    protected HandleSetCityHB() {
      super(R.id.fieldLocationHB);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new CityMutation(City.HB));
    }
  }

  private static class HandleSetCityBB extends ClickHandler {
    protected HandleSetCityBB() {
      super(R.id.fieldLocationBB);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new CityMutation(City.BB));
    }
  }

  private static class HandleSetOpenBeach extends ClickHandler {
    protected HandleSetOpenBeach() {
      super(R.id.fieldLocationOpenBeach);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new PlacementMutation(Placement.OPEN_BEACH));
    }
  }

  private static class HandleSetInVegitation extends ClickHandler {
    protected HandleSetInVegitation() {
      super(R.id.fieldLocationInVegitation);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new PlacementMutation(Placement.IN_VEGITATION));
    }
  }

  private static class HandleSetAtVegitation extends ClickHandler {
    protected HandleSetAtVegitation() {
      super(R.id.fieldLocationAtVegitation);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new PlacementMutation(Placement.AT_VEGITATION));
    }
  }

  private static class HandleSetAtEscarpment extends ClickHandler {
    protected HandleSetAtEscarpment() {
      super(R.id.fieldLocationAtEscarpment);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new PlacementMutation(Placement.AT_ESCARPMENT));
    }
  }

  private static class HandleSetEscarpmentOver18Inches extends ClickHandler {
    protected HandleSetEscarpmentOver18Inches() {
      super(R.id.fieldLocationEscarpmentOver18Inches);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(
              new ReportMutations.EscarpmentOver18InchesMutation(isChecked(view)));
    }
  }

  private static class HandleSetOnEscarpment extends ClickHandler {
    protected HandleSetOnEscarpment() {
      super(R.id.fieldLocationOnEscarpment);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new PlacementMutation(Placement.ON_ESCARPMENT));
    }
  }

  private static class HandleSetSeawallRocks extends ClickHandler {
    protected HandleSetSeawallRocks() {
      super(R.id.fieldObstructionsSeawallRocks);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ObstructionsSeawallRocksMutation(isChecked(view)));
    }
  }

  private static class HandleSetFurniture extends ClickHandler {
    protected HandleSetFurniture() {
      super(R.id.fieldObstructionsFurniture);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ObstructionsFurnitureMutation(isChecked(view)));
    }
  }

  private static class HandleSetObstructionsEscarpment extends ClickHandler {
    protected HandleSetObstructionsEscarpment() {
      super(R.id.fieldObstructionsEscarpment);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ObstructionsEscarpmentMutation(isChecked(view)));
    }
  }

  private static class HandleUpdateObstructionsOther extends TextChangeHandler {
    protected HandleUpdateObstructionsOther() {
      super(R.id.fieldObstructionsOther);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.applyMutation(new ObstructionsOtherMutation(
          newText.isEmpty() ? Optional.<String>absent() : Optional.of(newText)));
    }
  }

  private static class HandleSetGps extends ClickHandler {
    protected HandleSetGps() {
      super(R.id.buttonGps);
    }

    @Override
    public void handleClick(View view, final DataUpdateHandler updateHandler) {
      GpsCoordinateDialog dialog = new GpsCoordinateDialog();
      Preconditions.checkArgument(view.getContext() instanceof Activity);
      dialog.setCallback(new GpsLocationCallback() {
        @Override
        public void location(GpsCoordinates coordinates) {
          updateHandler.applyMutation(new GpsMutation(coordinates));
        }
      });

      dialog.show(((Activity)view.getContext()).getFragmentManager(), "GPS");

    }
  }
}
