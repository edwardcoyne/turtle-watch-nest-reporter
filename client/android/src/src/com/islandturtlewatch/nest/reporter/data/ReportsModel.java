package com.islandturtlewatch.nest.reporter.data;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.islandturtlewatch.nest.data.ReportProto.Image;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.ReportRestorer;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore.CachedReportWrapper;

import java.util.List;

public class ReportsModel {
  private static final String TAG = ReportsModel.class.getSimpleName();
  private static final String PREFERENCE_KEY_ACTIVE_REPORT_ID = "model.active_report_id";

  private final LocalDataStore dataStore;
  private final Activity activity;
  private final SharedPreferences preferences;
  private final ActiveReportManager activeReport = new ActiveReportManager();
  private ReportsListAdapter adapter = new ReportsListAdapter();

  public ReportsModel(LocalDataStore dataStore, SharedPreferences preferences, Activity activity) {
    this.dataStore = dataStore;
    this.preferences = preferences;
    this.activity = activity;

    Optional<Long> lastActiveReportId = getLastActiveReportId();
    if (lastActiveReportId.isPresent()) {
      loadReport(lastActiveReportId.get());
    } else {
      Log.d(TAG, "Previous report id not found, creating new.");
      createReport();
    }
  }

  public void persistToBundle(Bundle outState) {
    saveLastActiveReportId(activeReport.getId());
  }

  public void restoreFromBundle(Bundle inState) {
    Optional<Long> idOpt = getLastActiveReportId();
    Preconditions.checkArgument(idOpt.isPresent(), "Missing saved id on restore.");
    Preconditions.checkArgument(loadReport(idOpt.get()), "Failed to load id on restore");
  }

  public void restoreDataFromServer() {
    new ReportRestorer(activity)
        .restoreReports(new Runnable(){
          @Override
          public void run() {
            activity.runOnUiThread(new Runnable(){
              @Override
              public void run() {
                adapter.notifyDataSetChanged();
              }});
          }});
  }

  /**
   * Creates a new report and sets it active.
   *
   * <p>
   * The currently active report should be submitted or abandoned.
   */
  public void startNewActiveReport() {
    createReport();
  }

  /**
   * Get report we are currently working on.
   */
  public Report getActiveReport() {
    return activeReport.get();
  }

  public long getActiveReportId() { return activeReport.getId();}

  /**
   * Get report we are currently working on.
   */
  public void deleteActiveReport() {
    activeReport.delete();

    // Set next active report, if non we create new otherwise first on list.
    ImmutableList<CachedReportWrapper> activeReports = dataStore.listActiveReportsWithDuplicates();
    if (activeReports.isEmpty()) {
      createReport();
    } else {
      loadReport(activeReports.get(0).getLocalId());
    }

    adapter.notifyDataSetChanged();
  }

  public int getHighestNestNumber() {
    return dataStore.getHighestNestNumber();
  }

  public int getHighestPossibleFalseCrawlNumber() {
    return dataStore.getHighestPossibleFalseCrawlNumber();
  }

  public int getHighestFalseCrawlNumber() {

    return dataStore.getHighestFalseCrawlNumber();
  }

  /**
   * Set report we are currently working on.
   */
  public void setActiveReport(Report report) {
    activeReport.update(report);
    adapter.notifyDataSetChanged();
  }

  public void switchActiveReport(long reportId) {
    loadReport(reportId);
  }

  public ListAdapter getReportsListAdapter(ReportsListItemViewFactory viewFactory) {
    adapter = new ReportsListAdapter(viewFactory);
    return adapter;
  }

  private void createReport() {
    CachedReportWrapper wrapper = dataStore.createReport();
    activeReport.setFrom(wrapper);
    adapter.notifyDataSetChanged();
  }

  private boolean loadReport(long id) {
    CachedReportWrapper wrapper = dataStore.getReport(id);
    if (!wrapper.isActive()) {
      Log.e(TAG, "Cannot load non-active report.");
      return false;
    }
    activeReport.setFrom(wrapper);
    return true;
  }

  private Optional<Long> getLastActiveReportId() {
    if (!preferences.contains(PREFERENCE_KEY_ACTIVE_REPORT_ID)) {
      return Optional.absent();
    }
    return Optional.of(preferences.getLong(PREFERENCE_KEY_ACTIVE_REPORT_ID, -1));
  }

  private void saveLastActiveReportId(long currentReport) {
    preferences.edit().putLong(PREFERENCE_KEY_ACTIVE_REPORT_ID, currentReport).commit();
  }

  private class ActiveReportManager {
    private long activeReportId;
    private Report activeReport;

    void setFrom(CachedReportWrapper wrapper) {
      set(wrapper.getLocalId(), wrapper.getReport());
    }

    void set(long id, Report report) {
      saveLastActiveReportId(id);
      activeReportId = id;
      activeReport = report;
    }

    long getId() {
      return activeReportId;
    }

    Report get() {
      return activeReport;
    }

    void update(Report report) {
      if (report.equals(activeReport)) {
        return;
      }
      dataStore.saveReport(activeReportId, report);
      activeReport = report;
    }

    void delete() {
      dataStore.deleteReport(activeReportId);
    }
  }

  public interface ReportsListItemViewFactory {
    View getView(CachedReportWrapper report, Optional<View> oldView, ViewGroup parent);
  }

  private class ReportsListAdapter extends BaseAdapter {
    private final Optional<ReportsListItemViewFactory> viewFactory;

    public ReportsListAdapter(ReportsListItemViewFactory viewFactory) {
      this.viewFactory = Optional.of(viewFactory);
    }

    public ReportsListAdapter() {
      this.viewFactory = Optional.absent();
    }

    @Override
    public int getCount() {

      return dataStore.activeReportCount();
    }

    //TODO(edcoyne): if performance is ever an issue, optimize this, don't get all reports tp find
    // position.
    @Override
    public Object getItem(int position) {
      return dataStore.listActiveReportsWithDuplicates().get(position);
    }

    @Override
    public long getItemId(int position) {
      return dataStore.listActiveReportsWithDuplicates().get(position).getLocalId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      Preconditions.checkArgument(viewFactory.isPresent(),
          "Should not call getView on adapter without view factory.");
      CachedReportWrapper wrapper = (CachedReportWrapper) getItem(position);
      Report report = wrapper.getReport();
      return viewFactory.get().getView(wrapper,
          Optional.fromNullable(convertView),
          parent);
    }
  }
}
