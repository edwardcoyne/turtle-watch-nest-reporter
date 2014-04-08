package com.islandturtlewatch.nest.reporter.data;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.common.base.Optional;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore.CachedReportWrapper;

public class ReportsModel {
  private static final String TAG = ReportsModel.class.getSimpleName();
  private static final String PREFERENCE_KEY_ACTIVE_REPORT_ID = "model.active_report_id";

  LocalDataStore dataStore;
  SharedPreferences preferences;
  ActiveReportManager activeReport = new ActiveReportManager();
  ReportsListAdapter adapter;

  public ReportsModel(LocalDataStore dataStore, SharedPreferences preferences) {
    this.dataStore = dataStore;
    this.preferences = preferences;

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

  /**
   * Save report we are currently working with to disk.
   */
  public void saveActiveReport() {
    throw new UnsupportedOperationException("Not Implemented");
  }

  /**
   * Submit report we are currently working with to cloud.
   */
  public void submitActiveReport() {
    throw new UnsupportedOperationException("Not Implemented");
  }

  /**
   * List all reports we have previously submitted.
   */
  public Iterable<Report> listReports() {
    throw new UnsupportedOperationException("Not Implemented");
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
  }

  public interface ReportsListItemViewFactory {
    View getView(Report report, Optional<View> oldView, ViewGroup parent);
  }

  private class ReportsListAdapter extends BaseAdapter {
    private final ReportsListItemViewFactory viewFactory;
    public ReportsListAdapter(ReportsListItemViewFactory viewFactory) {
      this.viewFactory = viewFactory;
    }

    @Override
    public int getCount() {
      return dataStore.activeReportCount();
    }

    //TODO(edcoyne): if performance is ever an issue, optimize this.
    @Override
    public Object getItem(int position) {
      return dataStore.listActiveReports().get(position).getReport();
    }

    @Override
    public long getItemId(int position) {
      return dataStore.listActiveReports().get(position).getLocalId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      return viewFactory.getView((Report)getItem(position),
          Optional.fromNullable(convertView),
          parent);
    }
  }
}
