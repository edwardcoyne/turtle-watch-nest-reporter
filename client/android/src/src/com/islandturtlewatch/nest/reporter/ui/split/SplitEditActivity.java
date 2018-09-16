package com.islandturtlewatch.nest.reporter.ui.split;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.reporter.EditPresenter;
import com.islandturtlewatch.nest.reporter.EditPresenter.DataUpdateHandler;
import com.islandturtlewatch.nest.reporter.R;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore;
import com.islandturtlewatch.nest.reporter.data.ReportsModel;
import com.islandturtlewatch.nest.reporter.data.ReportsModel.ReportsListItemViewFactory;
import com.islandturtlewatch.nest.reporter.service.SyncService;
import com.islandturtlewatch.nest.reporter.ui.ConfirmationDialogReportParser;
import com.islandturtlewatch.nest.reporter.ui.EditFragment;
import com.islandturtlewatch.nest.reporter.ui.EditFragment.ClickHandler;
import com.islandturtlewatch.nest.reporter.ui.EditFragment.ClickHandlerSimple;
import com.islandturtlewatch.nest.reporter.ui.EditFragment.TextChangeHandler;
import com.islandturtlewatch.nest.reporter.ui.EditFragment.TextChangeHandlerSimple;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentInfo;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentMedia;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentNestCare;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentNestCondition;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentNestLocation;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentNestResolution;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentNotes;
import com.islandturtlewatch.nest.reporter.ui.EditFragmentStorms;
import com.islandturtlewatch.nest.reporter.ui.EditView;
import com.islandturtlewatch.nest.reporter.ui.FocusMonitoredEditText;
import com.islandturtlewatch.nest.reporter.ui.ReportSection;
import com.islandturtlewatch.nest.reporter.ui.ReportSectionListFragment;
import com.islandturtlewatch.nest.reporter.ui.ReportSectionListFragment.EventHandler;
import com.islandturtlewatch.nest.reporter.util.AuthenticationUtil;
import com.islandturtlewatch.nest.reporter.util.DialogUtil;
import com.islandturtlewatch.nest.reporter.util.ReportUtil;
import com.islandturtlewatch.nest.reporter.util.SettingsUtil;

import java.util.List;
import java.util.Map;

public class SplitEditActivity extends FragmentActivity implements EditView {
  //private static final String TAG = SplitEditActivity.class.getSimpleName();
  private static final String KEY_SECTION = "Section";
  private static final List<String> PERMISSIONS = ImmutableList.of(
          Manifest.permission.GET_ACCOUNTS,
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.CAMERA);

  public static final int REQUEST_ACCOUNT_PICKER = 1;

  private static final ImmutableMap<ReportSection, EditFragment> FRAGMENT_MAP =
      ImmutableMap.<ReportSection, EditFragment>builder()
          .put(ReportSection.INFO, new EditFragmentInfo())
          .put(ReportSection.NEST_LOCATION, new EditFragmentNestLocation())
          .put(ReportSection.STORMS, new EditFragmentStorms())
          .put(ReportSection.NEST_CONDITION, new EditFragmentNestCondition())
          .put(ReportSection.NEST_CARE, new EditFragmentNestCare())
          .put(ReportSection.NEST_RESOLUTION, new EditFragmentNestResolution())
          .put(ReportSection.MEDIA, new EditFragmentMedia())
          .put(ReportSection.NOTES, new EditFragmentNotes())
          .build();

  private SharedPreferences settings;
  private EditPresenter presenter;
  private ReportsModel model;
  private SectionManager sectionManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.split_edit_activity);
    settings = getSharedPreferences(SettingsUtil.SETTINGS_ID, MODE_PRIVATE);
    ensureUsernameSet();
    ensureCorePermissions();
    AuthenticationUtil.checkGooglePlayServicesAvailable(this);

    model = new ReportsModel(new LocalDataStore(this),
        getPreferences(Context.MODE_PRIVATE),
        this);
    presenter = new EditPresenter(model, this);
    sectionManager = new SectionManager();
    sectionManager.setSection(ReportSection.INFO);

    ListView drawerList = (ListView) findViewById(R.id.reports_drawer);

    //TODO(edcoyne): Messy, cleanup.
    drawerList.setAdapter(model.getReportsListAdapter(new ReportsListItemViewFactory(){
      @Override
      public View getView(LocalDataStore.CachedReportWrapper report,
          Optional<View> possibleConvertableView, ViewGroup parent) {
        LayoutInflater inflator =
            (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView view =
            (TextView) inflator.inflate(R.layout.reports_list_item, parent, false);
        view.setText(ReportUtil.getShortName(report));

        return view;
      }}));

    drawerList.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent,
          View view,
          int position,
          long id) {
        long oldId = model.getActiveReportId();
        model.switchActiveReport(id);
        Report newReport = model.getActiveReport();
        confirmSwitchReport(newReport, oldId);
        DrawerLayout drawer = ((DrawerLayout) findViewById(R.id.drawer_layout));
        drawer.closeDrawers();
      }
    });

  }

  private void confirmSwitchReport(Report newReport, final long id) {
    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    dialog.setTitle(R.string.confirmChangeReport);
    dialog.setMessage(ConfirmationDialogReportParser.parseReport(newReport));
    dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
//        model.switchActiveReport(id);
        updateDisplay(model.getActiveReport());
      }
    });
    dialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        model.switchActiveReport(id);
        updateDisplay(model.getActiveReport());
      }
    });
    dialog.show();
  }
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu items for use in the action bar
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.split_edit_activity_menu, menu);
    SyncService.start(this);

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    //TODO(edcoyne): move this logic out of the view.
    // Handle presses on the action bar items
    switch (item.getItemId()) {
        case R.id.action_change_reports:
          DrawerLayout drawer = ((DrawerLayout) findViewById(R.id.drawer_layout));
          drawer.openDrawer(Gravity.LEFT);
          return true;
        case R.id.action_new_report:
          model.startNewActiveReport();
          updateDisplay(model.getActiveReport());

          return true;
        case R.id.action_delete_report:
          DialogUtil.confirm(this, "Are you sure you want to delete this report?",
              new Runnable() {
                @Override
                public void run() {
                  model.deleteActiveReport();
                  updateDisplay(model.getActiveReport());
                }});
          return true;
        case R.id.action_restore_from_server:
          DialogUtil.confirm(this,
              "Ensure you have no pending uploads, they will be overwritten.\nContinue?",
              new Runnable() {
                @Override
                public void run() {
                  model.restoreDataFromServer();
                }});
          return true;
        default:
            return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void updateDisplay(Report report) {
    LocalDataStore.CachedReportWrapper wrapper = new LocalDataStore.CachedReportWrapper();
    wrapper.setReport(report);
    setTitle(ReportUtil.getShortName(wrapper));
    sectionManager.updateSections(report);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(KEY_SECTION, sectionManager.currentSection.name());
    presenter.persistToBundle(outState);
    sectionManager.saveState(outState);
  }

  @Override
  protected void onRestoreInstanceState(Bundle inState) {
    super.onRestoreInstanceState(inState);
    presenter.restoreFromBundle(inState);
    if (inState.containsKey(KEY_SECTION)) {
      sectionManager = new SectionManager();
      sectionManager.setSection(ReportSection.valueOf(inState.getString(KEY_SECTION)));
      sectionManager.updateSections(presenter.getCurrentReport());
      sectionManager.restoreState(inState);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
   super.onActivityResult(requestCode, resultCode, data);
   switch (requestCode) {
     case REQUEST_ACCOUNT_PICKER:
       if (data != null && data.getExtras() != null) {
         String accountName =
             data.getExtras().getString(
                 AccountManager.KEY_ACCOUNT_NAME);
         if (accountName != null) {
           SharedPreferences.Editor editor = settings.edit();
           editor.putString(SettingsUtil.KEY_USERNAME, accountName);
           editor.commit();
         }
       }
       break;
     default:
       sectionManager.handleIntentResult(requestCode, resultCode, data);
       break;
   }
  }

  public void handleItemSelected(View view, String selectedText) {
    Map<Integer,EditFragment.OnItemSelectedHandler> itemSelectedHandlers =
            sectionManager.getCurrentOnItemSelectedHandlers();
    Preconditions.checkArgument(itemSelectedHandlers.containsKey(view.getId()),
            "No On Item Selected handler registered for %s", view.getId());
      itemSelectedHandlers.get(view.getId()).handleItemSelected(selectedText, presenter.getUpdateHandler());

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

  private void ensureUsernameSet() {
    if (!settings.contains(SettingsUtil.KEY_USERNAME)) {
      startActivityForResult(AuthenticationUtil.getCredential(this, "DUMMY").newChooseAccountIntent(),
          REQUEST_ACCOUNT_PICKER);
    }
  }

  private void ensureCorePermissions() {
    for (String permisson : PERMISSIONS) {
      if (ContextCompat.checkSelfPermission(this, permisson)
              != PackageManager.PERMISSION_GRANTED) {
        // No explanation needed; request the permission
        ActivityCompat.requestPermissions(this,
                PERMISSIONS.toArray(new String[]{}),
                1);
      }
    }
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

    void saveState(Bundle bundle) {
      currentFragment.saveState(bundle);
    }

    void restoreState(Bundle bundle) {
      currentFragment.restoreState(bundle);
    }

    void updateSections(Report report) {
      currentFragment.updateDisplay(report);
      currentReport = Optional.of(report);
    }

    Map<Integer, EditFragment.OnItemSelectedHandler> getCurrentOnItemSelectedHandlers() {
      return currentFragment.getOnItemSelectedHandlers();
    }

    Map<Integer, ClickHandler> getCurrentClickHandlers() {
      return currentFragment.getClickHandlers();
    }

    Map<Integer, TextChangeHandler> getCurrentTextChangeHandlers() {
      return currentFragment.getTextChangeHandlers();
    }

    void handleIntentResult(int requestCode, int resultCode, Intent data) {
      currentFragment.handleIntentResult(requestCode, resultCode, data);
    }

    private void setSection(ReportSection section) {
      if (currentSection == section) {
        return;
      }
      currentSection = section;

      EditFragment fragment = FRAGMENT_MAP.get(section);

      setEditFragment(fragment);
      if (currentReport.isPresent()) {
        fragment.updateDisplay(currentReport.get());
      }
      fragment.setListenerProvider(new ListenerProvider());

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

  public class ListenerProvider {
    private ListenerProvider() {};

    public OnClickListener getOnClickListener(final ClickHandlerSimple handler) {
      return new OnClickListener() {
        @Override public void onClick(View view) {
           handler.handleClick(view, presenter.getUpdateHandler());
        }
      };
    }

    public TextWatcher getTextChangedListener(final TextChangeHandlerSimple handler) {
      return new TextWatcher() {
        @Override
        public void afterTextChanged(Editable newText) {
          handler.handleTextChange(newText.toString(), presenter.getUpdateHandler());
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
      };
    }

    public DataUpdateHandler getUpdateHandler() {
      return presenter.getUpdateHandler();
    }



    public void setFocusLossListener(FocusMonitoredEditText editText,
        TextChangeHandlerSimple handler) {
      editText.setTextChangeHandler(handler);
      editText.setDataUpdateHandler(presenter.getUpdateHandler());
    }
  }
}
