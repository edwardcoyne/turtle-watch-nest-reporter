package com.islandturtlewatch.nest.reporter.ui;

import java.util.Map;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.google.common.base.Optional;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateHandler;
import com.islandturtlewatch.nest.reporter.R;
import com.islandturtlewatch.nest.reporter.util.DateUtil;

public class EditFragmentInfo extends EditFragment {
  private static final Map<Integer, ClickHandler> CLICK_HANDLERS =
      ClickHandler.toMap(
          new HandleSetInfoDate(),
          new HandleSetObervers(),
          new HandleSetNestVerified(),
          new HandleSetNestNotVerified(),
          new HandleSetNestRelocated(),
          new HandleSetFalseCrawl(),
          new HandleSetAbandonedBodyPits(),
          new HandleSetAbandonedEggCavities());

  private static final Map<Integer, TextChangeHandler> TEXT_CHANGE_HANDLERS =
      TextChangeHandler.toMap(
          new HandleUpdateNestNumber(),
          new HandleUpdateObservers());

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
    return inflater.inflate(R.layout.edit_fragment_info, container, false);
  }

  @Override
  public void updateSection(Report report) {
    setText(R.id.fieldNestNumber, Integer.toString(report.getNestNumber()));

    if (report.hasTimestampFoundMs()) {
      setDate(R.id.buttonDateFound, report.getTimestampFoundMs());
      setText(R.id.labelIncubationDate,
          DateUtil.getFormattedDate(DateUtil.plusDays(report.getTimestampFoundMs(), 55)));
    } else {
      clearDate(R.id.buttonDateFound);
      setText(R.id.labelIncubationDate, "");
    }

    setText(R.id.fieldObservers, report.hasObservers() ? report.getObservers() : "");

    setChecked(R.id.fieldNestVerified, report.getActivity().getNestVerified());
    setChecked(R.id.fieldNestNotVerified, report.getActivity().getNestNotVerified());
    setChecked(R.id.fieldNestRelocated, report.getActivity().getNestRelocated());
    setChecked(R.id.fieldFalseCrawl, report.getActivity().getFalseCrawl());
    setChecked(R.id.fieldAbandonedBodyPits, report.getActivity().getAbandonedBodyPits());

    setChecked(R.id.fieldAbandonedEggCavities, report.getActivity().getAbandonedEggCavities());
  }

  private static class HandleUpdateNestNumber extends TextChangeHandler {
    protected HandleUpdateNestNumber() {
      super(R.id.fieldNestNumber);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      if (newText.isEmpty()) {
        updateHandler.updateNestNumber(Optional.<Integer>absent());
      } else {
        int newValue = Integer.parseInt(newText);
        //TODO(edcoyne): display error if not parsable
        updateHandler.updateNestNumber(Optional.of(newValue));
      }
    }
  }

  private static class HandleUpdateObservers extends TextChangeHandler {
    protected HandleUpdateObservers() {
      super(R.id.fieldObservers);
    }

    @Override
    public void handleTextChange(String newText, DataUpdateHandler updateHandler) {
      updateHandler.updateObservers(newText);
    }
  }

  private static class HandleSetInfoDate extends DatePickerClickHandler {
    protected HandleSetInfoDate() {
      super(R.id.buttonDateFound);
    }
    @Override
    public void onDateSet(DatePicker view,
        int year,
        int month,
        int day) {
      Log.d(EditFragmentInfo.class.getSimpleName(),
          "Set date to " + year + "/" + month + "/" + day);
      displayResult(updateHandler.updateDateFound(year, month, day));
    }
  }

  private static class HandleSetObervers extends ClickHandler {
    protected HandleSetObervers() {
      super(R.id.fieldObservers);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateObservers(getText(view));
    }
  }

  private static class HandleSetNestVerified extends ClickHandler {
    protected HandleSetNestVerified() {
      super(R.id.fieldNestVerified);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateNestVerified(isChecked(view));
    }
  }

  private static class HandleSetNestNotVerified extends ClickHandler {
    protected HandleSetNestNotVerified() {
      super(R.id.fieldNestNotVerified);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateNestNotVerified(isChecked(view));
    }
  }

  private static class HandleSetNestRelocated extends ClickHandler {
    protected HandleSetNestRelocated() {
      super(R.id.fieldNestRelocated);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateNestRelocated(isChecked(view));
    }
  }

  private static class HandleSetFalseCrawl extends ClickHandler {
    protected HandleSetFalseCrawl() {
      super(R.id.fieldFalseCrawl);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateFalseCrawl(isChecked(view));
    }
  }

  private static class HandleSetAbandonedBodyPits extends ClickHandler {
    protected HandleSetAbandonedBodyPits() {
      super(R.id.fieldAbandonedBodyPits);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateAbandonedBodyPits(isChecked(view));
    }
  }

  private static class HandleSetAbandonedEggCavities extends ClickHandler {
    protected HandleSetAbandonedEggCavities() {
      super(R.id.fieldAbandonedEggCavities);
    }

    @Override
    public void handleClick(View view, DataUpdateHandler updateHandler) {
       updateHandler.updateAbandonedEggCavities(isChecked(view));
    }
  }
}
