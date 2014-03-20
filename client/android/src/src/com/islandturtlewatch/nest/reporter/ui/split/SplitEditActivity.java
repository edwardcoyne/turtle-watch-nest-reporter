package com.islandturtlewatch.nest.reporter.ui.split;

import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.EditPresenter;
import com.islandturtlewatch.nest.reporter.R;
import com.islandturtlewatch.nest.reporter.data.ReportsModel;
import com.islandturtlewatch.nest.reporter.ui.EditFragment;
import com.islandturtlewatch.nest.reporter.ui.EditFragment.ClickHandler;
import com.islandturtlewatch.nest.reporter.ui.EditFragment.TextChangeHandler;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentInfo;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentMedia;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentNestCare;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentNestCondition;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentNestLocation;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentNestResolution;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentNotes;
import com.islandturtlewatch.nest.reporter.ui.EditView;
import com.islandturtlewatch.nest.reporter.ui.ReportSection;
import com.islandturtlewatch.nest.reporter.ui.ReportSectionListFragment;
import com.islandturtlewatch.nest.reporter.ui.ReportSectionListFragment.EventHandler;

public class SplitEditActivity extends FragmentActivity implements EditView {
  //private static final String TAG = SplitEditActivity.class.getSimpleName();

  private static final String KEY_SECTION = "Section";

  private static final ImmutableMap<ReportSection, EditFragment> FRAGMENT_MAP =
      ImmutableMap.<ReportSection, EditFragment>builder()
          .put(ReportSection.INFO, new EditFragmentInfo())
          .put(ReportSection.NEST_LOCATION, new EditFragmentNestLocation())
          .put(ReportSection.NEST_CONDITION, new EditFragmentNestCondition())
          .put(ReportSection.NEST_CARE, new EditFragmentNestCare())
          .put(ReportSection.NEST_RESOLUTION, new EditFragmentNestResolution())
          .put(ReportSection.MEDIA, new EditFragmentMedia())
          .put(ReportSection.NOTES, new EditFragmentNotes())
          .build();

  private EditPresenter presenter;
  private SectionManager sectionManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.split_edit_activity);

    presenter = new EditPresenter(new ReportsModel(), this);
    sectionManager = new SectionManager();
  }

  @Override
  public void updateDisplay(Report report) {
    sectionManager.updateSections(report);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(KEY_SECTION, sectionManager.currentSection.name());
    presenter.persistToBundle(outState);
  }

  @Override
  protected void onRestoreInstanceState(Bundle inState) {
    super.onRestoreInstanceState(inState);
    presenter.restoreFromBundle(inState);
    if (inState.containsKey(KEY_SECTION)) {
      sectionManager = new SectionManager();
      sectionManager.setSection(ReportSection.valueOf(inState.getString(KEY_SECTION)));
      sectionManager.updateSections(presenter.getCurrentReport());
    }
  }

  public void handleClick(View view) {
    Map<Integer, ClickHandler> clickHandlers = sectionManager.getCurrentClickHandlers();
    Preconditions.checkArgument(clickHandlers.containsKey(view.getId()),
        "No click handler registered for %s", view.getId());

    clickHandlers.get(view.getId()).handleClick(view, presenter.getUpdateHandler());
  }

  public void handleTextChange(View view, String newText) {
    Map<Integer, TextChangeHandler> changeHandlers =
        sectionManager.getCurrentTextChangeHandlers();

    int id = view.getId();
    Preconditions.checkArgument(changeHandlers.containsKey(id),
        "Missing text change handler for " + id);

    changeHandlers.get(id).handleTextChange(newText.toString(), presenter.getUpdateHandler());
  }

  /**
   * Responsible for managing the current section and updating the display to change sections.
   */
  private class SectionManager {

    private ReportSection currentSection;
    private EditFragment currentFragment;
    private Optional<Report> currentReport = Optional.absent();

    SectionManager() {
      setSectionListEventHandler(new SectionListEventHandler());
    }

    void updateSections(Report report) {
      currentFragment.updateDisplay(report);
      currentReport = Optional.of(report);
    }

    Map<Integer, ClickHandler> getCurrentClickHandlers() {
      return currentFragment.getClickHandlers();
    }

    Map<Integer, TextChangeHandler> getCurrentTextChangeHandlers() {
      return currentFragment.getTextChangeHandlers();
    }

    private void setSection(ReportSection section) {
      if (currentSection == section) {
        return;
      }
      currentSection = section;

      EditFragment fragment = FRAGMENT_MAP.get(section);

      Bundle arguments = new Bundle();
      fragment.setArguments(arguments);
      setEditFragment(fragment);
      if (currentReport.isPresent()) {
        fragment.updateDisplay(currentReport.get());
      }

      presenter.updateView();
    }

    private void setEditFragment(EditFragment fragment) {
      getFragmentManager().beginTransaction()
          .replace(R.id.section_edit_container, fragment)
          .commit();
      currentFragment = fragment;
    }

    private void setSectionListEventHandler(EventHandler handler) {
      ReportSectionListFragment fragment = (ReportSectionListFragment) getFragmentManager()
          .findFragmentById(R.id.report_section_list);
      fragment.setEventHandler(handler);
    }

    private class SectionListEventHandler
        implements ReportSectionListFragment.EventHandler {
      @Override
      public void onSectionSelected(ReportSection section) {
        setSection(section);
      }
    }
  }
}
