package com.islandturtlewatch.nest.reporter.data;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.islandturtlewatch.nest.data.ReportProto.Image;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore.CachedReportWrapper;
import com.islandturtlewatch.nest.reporter.util.ImageUtil;

public class ReportsModel {
  private static final String TAG = ReportsModel.class.getSimpleName();
  private static final String PREFERENCE_KEY_ACTIVE_REPORT_ID = "model.active_report_id";

  private final LocalDataStore dataStore;
  private final Context context;
  private final SharedPreferences preferences;
  private final ActiveReportManager activeReport = new ActiveReportManager();
  private ReportsListAdapter adapter = new ReportsListAdapter();

  public ReportsModel(LocalDataStore dataStore, SharedPreferences preferences, Context context) {
    this.dataStore = dataStore;
    this.preferences = preferences;
    this.context = context;

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
   * Get report we are currently working on.
   */
  public void deleteActiveReport() {
    activeReport.delete();

    // Set next active report, if non we create new otherwise first on list.
    ImmutableList<CachedReportWrapper> activeReports = dataStore.listActiveReports();
    if (activeReports.isEmpty()) {
      createReport();
    } else {
      loadReport(activeReports.get(0).getLocalId());
    }

    adapter.notifyDataSetChanged();
  }

  /**
   * Set report we are currently working on.
   */
  public void setActiveReport(Report report) {
    activeReport.update(report);
    adapter.notifyDataSetChanged();
  }

  public void updateImages(Report report) {
    activeReport.updateImages(report.getImageList());
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
      addImages(report.getImageList());
      activeReport = report;
    }

    void updateImages(List<Image> images) {
      for (Image image : images) {
        long newTs = ImageUtil.getModifiedTime(context, image.getFileName());
        Optional<Long> oldTs = dataStore.getImageTimestamp(activeReportId, image.getFileName());
        if (!oldTs.isPresent()) {
          Log.d(TAG, "Adding new image record: " + image.getFileName() + " ts: " + newTs);
          dataStore.addImage(activeReportId, image.getFileName(), newTs);
        } else if (!oldTs.get().equals(newTs)) {
          Log.d(TAG, "Updating image: " + image.getFileName()
              + " oldts: " + oldTs.get() + " newTs:" + newTs);
          dataStore.touchImage(activeReportId, image.getFileName(), newTs);
        }
      }
    }

    // Don't need to do the expensive filesystem checks for the normal case,
    // only check for new images.
    void addImages(List<Image> images) {
      for (Image image : images) {
        if (!dataStore.getImageTimestamp(activeReportId, image.getFileName()).isPresent()) {
          long newTs = ImageUtil.getModifiedTime(context, image.getFileName());
          Log.d(TAG, "Adding new image record: " + image.getFileName() + " ts: " + newTs);
          dataStore.addImage(activeReportId, image.getFileName(), newTs);
        }
      }
    }

    void delete() {
      dataStore.deleteReport(activeReportId);
    }
  }

  public interface ReportsListItemViewFactory {
    View getView(Report report, Optional<View> oldView, ViewGroup parent);
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
      return dataStore.listActiveReports().get(position).getReport();
    }

    @Override
    public long getItemId(int position) {
      return dataStore.listActiveReports().get(position).getLocalId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      Preconditions.checkArgument(viewFactory.isPresent(),
          "Should not call getView on adapter without view factory.");
      return viewFactory.get().getView((Report)getItem(position),
          Optional.fromNullable(convertView),
          parent);
    }
  }
}
