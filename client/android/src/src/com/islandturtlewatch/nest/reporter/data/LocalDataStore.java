package com.islandturtlewatch.nest.reporter.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import lombok.Cleanup;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.util.Throwables;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.ReportRef;
import com.islandturtlewatch.nest.data.ReportProto.ReportWrapper;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore.Column.Type;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore.StorageDefinition.ImagesTable;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore.StorageDefinition.ReportsTable;

public class LocalDataStore {
  private static final String TAG = LocalDataStore.class.getCanonicalName();
  private final StorageDefinition.DbHelper storageHelper;

  public LocalDataStore(Context context) {
    storageHelper = new StorageDefinition.DbHelper(context);
  }

  private Cursor getActiveReportsCursor(SQLiteDatabase db) {

    return db.query(
        ReportsTable.TABLE_NAME, // table name
        CachedReportWrapper.requiredColumns, // cols to select
        and(isTrue(ReportsTable.COLUMN_ACTIVE), isFalse(ReportsTable.COLUMN_DELETED) ), // where
        null, // don't need selection args
        null, // don't group
        null, // don't filter
        ReportsTable.COLUMN_TS_LOCAL_ADD.name // sort
        );
  }

  public int activeReportCount() {
    @Cleanup SQLiteDatabase db = storageHelper.getReadableDatabase();
    @Cleanup Cursor cursor = getActiveReportsCursor(db);
    return cursor.getCount();
  }

  public ImmutableList<CachedReportWrapper> listActiveReports() {
    @Cleanup SQLiteDatabase db = storageHelper.getReadableDatabase();
    @Cleanup Cursor cursor = getActiveReportsCursor(db);

    ImmutableList.Builder<CachedReportWrapper> output = ImmutableList.builder();
    while (cursor.moveToNext()) {
      output.add(CachedReportWrapper.from(cursor));
    }
    return output.build();
  }

  public ImmutableList<CachedReportWrapper> listUnsyncedReports() {
    @Cleanup SQLiteDatabase db = storageHelper.getReadableDatabase();
    @Cleanup Cursor cursor = db.query(
        ReportsTable.TABLE_NAME, // table name
        CachedReportWrapper.requiredColumns, // cols to select
        and(isFalse(ReportsTable.COLUMN_SYNCED), isFalse(ReportsTable.COLUMN_DELETED)), // where
        null, // don't need selection args
        null, // don't group
        null, // don't filter
        null); // don't sort

    ImmutableList.Builder<CachedReportWrapper> output = ImmutableList.builder();
    while (cursor.moveToNext()) {
      output.add(CachedReportWrapper.from(cursor));
    }
    return output.build();
  }

  public Set<String> getUnsycnedImagesFileNames() {
    @Cleanup SQLiteDatabase db = storageHelper.getReadableDatabase();

    @Cleanup Cursor imageCursor = db.query(
        ImagesTable.TABLE_NAME, // table name
        new String[]{ImagesTable.COLUMN_LOCAL_REPORT_ID.name,
            ImagesTable.COLUMN_FILE_NAME.name}, // cols to select
        isFalse(ImagesTable.COLUMN_SYNCED), // where
        null, // don't need selection args
        null, // don't group
        null, // don't filter
        null); // don't sort

    ImmutableList<String> unsynchedReports =
        getAllString(imageCursor, ImagesTable.COLUMN_FILE_NAME);
    return ImmutableSet.copyOf(unsynchedReports);
  }

  public CachedReportWrapper getReport(long localId) {
    @Cleanup SQLiteDatabase db = storageHelper.getReadableDatabase();

    @Cleanup Cursor cursor = db.query(
        ReportsTable.TABLE_NAME,
        CachedReportWrapper.requiredColumns,
        ReportsTable.keyEquals(localId),
        null, // don't need selection args
        null, // don't group
        null, // don't filter
        null // don't sort
        );
    Preconditions.checkArgument(cursor.moveToFirst(), "Failed to find report for:" + localId);
    return CachedReportWrapper.from(cursor);
  }

  /**
   * Saves local changes to a report.
   */
  public void saveReport(long localId, Report report) {
    Log.d(TAG, "saving report " + localId);
    @Cleanup SQLiteDatabase db = storageHelper.getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(ReportsTable.COLUMN_REPORT.name, report.toByteArray());
    values.put(ReportsTable.COLUMN_SYNCED.name, false);
    values.put(ReportsTable.COLUMN_TS_LOCAL_UPDATE.name, System.currentTimeMillis());

    int numberUpdated = db.update(ReportsTable.TABLE_NAME,
        values,
        ReportsTable.keyEquals(localId),
        null);
    Preconditions.checkArgument(numberUpdated == 1,
        "Save should update one row not " + numberUpdated);
  }

  /**
   * Saves local changes to a report.
   */
  public void setReportUnsynced(long localId) {
    @Cleanup SQLiteDatabase db = storageHelper.getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(ReportsTable.COLUMN_SYNCED.name, false);
    values.put(ReportsTable.COLUMN_TS_LOCAL_UPDATE.name, System.currentTimeMillis());

    int numberUpdated = db.update(ReportsTable.TABLE_NAME,
        values,
        ReportsTable.keyEquals(localId),
        null);
    Preconditions.checkArgument(numberUpdated == 1,
        "set unscyned should update one row not " + numberUpdated);
  }

  /**
   * Saves updates from server.
   *
   * Requires InitReportId to have been called for existing report, else we will add a new row.
   */
  public void updateFromServer(ReportWrapper reportWrapper) {
    @Cleanup SQLiteDatabase db = storageHelper.getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(ReportsTable.COLUMN_ACTIVE.name, reportWrapper.getActive());
    values.put(ReportsTable.COLUMN_REPORT_ID.name, reportWrapper.getRef().getReportId());
    values.put(ReportsTable.COLUMN_VERSION.name, reportWrapper.getRef().getVersion());
    values.put(ReportsTable.COLUMN_REPORT.name, reportWrapper.getReport().toByteArray());
    values.put(ReportsTable.COLUMN_SYNCED.name, true);

    int numberUpdated = db.update(ReportsTable.TABLE_NAME,
        values,
        whereEquals(ReportsTable.COLUMN_REPORT_ID, reportWrapper.getRef().getReportId()),
        null);
    if (numberUpdated < 1) {
      // We don't have this report yet, add it.
      values.put(ReportsTable.COLUMN_TS_LOCAL_ADD.name, System.currentTimeMillis());
      long result = db.insert(ReportsTable.TABLE_NAME, null, values);
      Preconditions.checkArgument(result != -1, "Failed to insert new row");
    }
  }

  /**
   * Should be called after communicating with the server to sync the local copies of server-side
   * information.
   */
  public void setServerSideData(long localId, ReportRef ref) {
    @Cleanup SQLiteDatabase db = storageHelper.getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(ReportsTable.COLUMN_REPORT_ID.name, ref.getReportId());
    values.put(ReportsTable.COLUMN_VERSION.name, ref.getVersion());
    values.put(ReportsTable.COLUMN_SYNCED.name, true);

    int numberUpdated = db.update(ReportsTable.TABLE_NAME,
        values,
        ReportsTable.keyEquals(localId),
        null);
    Preconditions.checkArgument(numberUpdated == 1,
        "setServerSideData should update one row not " + numberUpdated);
  }

  /**
   *  Mark all images for this
   */
  public void markAllImagesSynced(long localReportId, List<String> filenames) {
    @Cleanup SQLiteDatabase db = storageHelper.getWritableDatabase();

    for (String fileName : filenames) {
      ContentValues values = new ContentValues();
      values.put(ImagesTable.COLUMN_SYNCED.name, true);

      int numberUpdated = db.update(ImagesTable.TABLE_NAME,
          values,
          and(whereEquals(ImagesTable.COLUMN_LOCAL_REPORT_ID, localReportId),
              whereStringEquals(ImagesTable.COLUMN_FILE_NAME, fileName)),
          null);
      Preconditions.checkArgument(numberUpdated == 1,
          "Should update one row not " + numberUpdated);
    }
  }

  public void addImage(long localReportId, String fileName, long timestamp) {
    @Cleanup SQLiteDatabase db = storageHelper.getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(ImagesTable.COLUMN_LOCAL_REPORT_ID.name, localReportId);
    values.put(ImagesTable.COLUMN_FILE_NAME.name, fileName);
    values.put(ImagesTable.COLUMN_TS_LOCAL_UPDATE.name, timestamp);
    values.put(ImagesTable.COLUMN_TS_LOCAL_ADD.name, System.currentTimeMillis());

    long imageId = db.insert(ImagesTable.TABLE_NAME, null, values);
    Preconditions.checkArgument(imageId != -1, "Failed to insert new image");
    this.setReportUnsynced(localReportId);
  }

  public void touchImage(long localReportId, String fileName, long timestamp) {
    @Cleanup SQLiteDatabase db = storageHelper.getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(ImagesTable.COLUMN_TS_LOCAL_UPDATE.name, timestamp);
    values.put(ImagesTable.COLUMN_SYNCED.name, false);

    int numberUpdated = db.update(ImagesTable.TABLE_NAME,
        values,
        and(whereEquals(ImagesTable.COLUMN_LOCAL_REPORT_ID, localReportId),
            whereStringEquals(ImagesTable.COLUMN_FILE_NAME, fileName)),
        null);
    Preconditions.checkArgument(numberUpdated == 1,
        "Should update one row not " + numberUpdated);
    this.setReportUnsynced(localReportId);
  }

  public Optional<Long> getImageTimestamp(long localReportId, String fileName) {
    @Cleanup SQLiteDatabase db = storageHelper.getReadableDatabase();
    @Cleanup Cursor cursor = db.query(
        ImagesTable.TABLE_NAME,
        new String[]{ImagesTable.COLUMN_TS_LOCAL_UPDATE.name},
        and(whereEquals(ImagesTable.COLUMN_LOCAL_REPORT_ID, localReportId),
            whereStringEquals(ImagesTable.COLUMN_FILE_NAME, fileName)),
        null, // don't need selection args
        null, // don't group
        null, // don't filter
        null // don't sort
        );
    if (cursor.getCount() < 1) {
      return Optional.absent();
    }
    cursor.moveToFirst();
    return Optional.of(getLong(cursor, ImagesTable.COLUMN_TS_LOCAL_UPDATE));
  }

  /**
   * Creates empty report record and returns the local id.
   * @return local_id local identifier of a report, not the same as report_id on server.
   */
  public CachedReportWrapper createReport() {
    int nestNumber = getHighestNestNumber() + 1;
    @Cleanup SQLiteDatabase db = storageHelper.getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(ReportsTable.COLUMN_REPORT.name,
        Report.newBuilder().setNestNumber(nestNumber).build().toByteArray());
    values.put(ReportsTable.COLUMN_ACTIVE.name, true);
    values.put(ReportsTable.COLUMN_TS_LOCAL_UPDATE.name, System.currentTimeMillis());
    values.put(ReportsTable.COLUMN_TS_LOCAL_ADD.name, System.currentTimeMillis());

    long localId = db.insert(ReportsTable.TABLE_NAME, null, values);
    Preconditions.checkArgument(localId != -1, "Failed to insert new row");
    return getReport(localId);
  }

  /**
   * Delete report.
   */
  public void deleteReport(long localId) {
    Log.d(TAG, "deleting report " + localId);
    @Cleanup SQLiteDatabase db = storageHelper.getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(ReportsTable.COLUMN_DELETED.name, true);

    int numberUpdated = db.update(ReportsTable.TABLE_NAME,
        values,
        ReportsTable.keyEquals(localId),
        null);
    Preconditions.checkArgument(numberUpdated == 1,
        "delete should update one row not " + numberUpdated);
  }

  public int getHighestNestNumber() {
    int highestNestNumber = 0;
    ImmutableList<CachedReportWrapper> activeReports = listActiveReports();
    for (CachedReportWrapper wrapper : activeReports) {
      highestNestNumber = Math.max(highestNestNumber, wrapper.getReport().getNestNumber());
    }
    return highestNestNumber;
  }

  public int getHighestFalseCrawlNumber() {
    int highestFCNumber = 0;
    ImmutableList<CachedReportWrapper> activeReports = listActiveReports();
    for (CachedReportWrapper wrapper : activeReports) {
      highestFCNumber = Math.max(highestFCNumber, wrapper.getReport().getFalseCrawlNumber());
    }
    return highestFCNumber;
  }

  private static ImmutableList<String> getAllString(Cursor cursor, Column column) {
    ImmutableList.Builder<String> output = ImmutableList.builder();
    while (cursor.moveToNext()) {
      output.add(getString(cursor, column));
    }
    return output.build();
  }

  private static boolean getBool(Cursor cursor, Column column) {
    return cursor.getInt(cursor.getColumnIndexOrThrow(column.name)) == 1;
  }

  private static int getInt(Cursor cursor, Column column) {
    return cursor.getInt(cursor.getColumnIndexOrThrow(column.name));
  }

  private static long getLong(Cursor cursor, Column column) {
    return cursor.getLong(cursor.getColumnIndexOrThrow(column.name));
  }

  private static Optional<Long> getOptLong(Cursor cursor, Column column) {
    if (cursor.isNull(cursor.getColumnIndexOrThrow(column.name))) {
      return Optional.absent();
    }
    return Optional.of(cursor.getLong(cursor.getColumnIndexOrThrow(column.name)));
  }

  private static String getString(Cursor cursor, Column column) {
    return cursor.getString(cursor.getColumnIndexOrThrow(column.name));
  }

  @SuppressWarnings("unchecked")
  private static <T extends MessageLite> Optional<T> getProto(Cursor cursor, Column column, T proto) {
    int index = cursor.getColumnIndexOrThrow(column.name);
    if (cursor.isNull(index)) {
      return Optional.absent();
    }
    try {
      return Optional.of((T)proto.newBuilderForType().mergeFrom(
          cursor.getBlob(index))
          .build());
    } catch (InvalidProtocolBufferException | IllegalArgumentException e) {
      throw Throwables.propagate(e);
    }
  }

  static String whereStringEquals(Column column, String value) {
    return column.name + " = \"" + value.toString() +"\"";
  }

  static <T extends Object> String whereEquals(Column column, T value) {
    return column.name + " = " + value.toString();
  }

  static String isTrue(Column column) {
    return column.name + " = 1";
  }

  static String isFalse(Column column) {
    return column.name + " != 1";
  }

  static String and(String cond1, String cond2) {
    return cond1 + " and " + cond2;
  }

  static <T> String colIn(Column column, List<T> values) {
    StringBuilder in = new StringBuilder(column.name + " in (");
    for (T value : values) {
      in.append(value.toString()).append(',');
    }
    // Remove last ','
    in.setLength(in.length()-1);
    return in.toString();
  }

  @Data
  @Builder(fluent=false)
  public static class CachedReportWrapper {
    private int localId;
    private Optional<Long> reportId;
    private Optional<Long> version;
    private boolean synched;
    private boolean active;
    private long lastUpdatedTimestamp;
    @NonNull private Report report;
    private List<String> unsynchedImageFileNames;

    static final String [] requiredColumns = {
      ReportsTable.COLUMN_LOCAL_ID.name,
      ReportsTable.COLUMN_ACTIVE.name,
      ReportsTable.COLUMN_SYNCED.name,
      ReportsTable.COLUMN_REPORT.name,
      ReportsTable.COLUMN_TS_LOCAL_UPDATE.name,
      ReportsTable.COLUMN_REPORT_ID.name,
      ReportsTable.COLUMN_VERSION.name};

    static CachedReportWrapper from(Cursor cursor) {
      CachedReportWrapperBuilder builder = CachedReportWrapper.builder()
          .setLocalId(getInt(cursor, ReportsTable.COLUMN_LOCAL_ID))
          .setReportId(getOptLong(cursor, ReportsTable.COLUMN_REPORT_ID))
          .setVersion(getOptLong(cursor, ReportsTable.COLUMN_VERSION))
          .setActive(getBool(cursor, ReportsTable.COLUMN_ACTIVE))
          .setSynched(getBool(cursor, ReportsTable.COLUMN_SYNCED))
          .setLastUpdatedTimestamp(getLong(cursor, ReportsTable.COLUMN_TS_LOCAL_UPDATE))
          .setUnsynchedImageFileNames(new ArrayList<String>());
      Optional<Report> proto =
          getProto(cursor, ReportsTable.COLUMN_REPORT, Report.getDefaultInstance());
      if (proto.isPresent()) {
        builder.setReport(proto.get());
      }
      return builder.build();
    }
  }

  final static class StorageDefinition {
    static final String DATABASE_NAME = "report_storage.db";
    static final int SCHEMA_VERSION = 1; // MUST INCREMENT if you change anything in this class.
    private StorageDefinition() {}

    static class ReportsTable implements BaseColumns, Table {
      static final String TABLE_NAME = "reports";
      static final Column COLUMN_LOCAL_ID = new Column("local_id", Type.INTEGER, Column.PRIMARY);
      static final Column COLUMN_REPORT_ID = new Column("report_id", Type.LONG);
      static final Column COLUMN_TS_LOCAL_ADD = new Column("local_add_timestamp", Type.LONG);
      static final Column COLUMN_TS_LOCAL_UPDATE =
          new Column("local_update_timestamp", Type.LONG);
      static final Column COLUMN_VERSION = new Column("version", Type.LONG);
      static final Column COLUMN_ACTIVE = new Column("active", Type.BOOLEAN, "1");
      static final Column COLUMN_DELETED = new Column("deleted", Type.BOOLEAN, "0");
      static final Column COLUMN_SYNCED = new Column("synced", Type.BOOLEAN, "0");
      static final Column COLUMN_REPORT = new Column("report", Type.BLOB);

      static final List<Column> LAYOUT = ImmutableList.of(
          COLUMN_LOCAL_ID,
          COLUMN_REPORT_ID,
          COLUMN_TS_LOCAL_ADD,
          COLUMN_TS_LOCAL_UPDATE,
          COLUMN_VERSION,
          COLUMN_ACTIVE,
          COLUMN_DELETED,
          COLUMN_SYNCED,
          COLUMN_REPORT);

      @Override public String getName() {
        return TABLE_NAME;
      }

      @Override public List<Column> getLayout() {
        return LAYOUT;
      }

      @Override public List<List<Column>> getIndices() {
        return Collections.emptyList();
      }

      static String keyEquals(long value) {
        return whereEquals(ReportsTable.COLUMN_LOCAL_ID, value);
      }
    }

    static class ImagesTable implements BaseColumns, Table {
      static final String TABLE_NAME = "images";
      static final Column COLUMN_IMAGE_ID = new Column("image_id", Type.INTEGER, Column.PRIMARY);
      static final Column COLUMN_LOCAL_REPORT_ID = new Column("local_report_id", Type.LONG);
      static final Column COLUMN_FILE_NAME = new Column("path", Type.TEXT);
      static final Column COLUMN_TS_LOCAL_ADD = new Column("local_add_timestamp", Type.LONG);
      static final Column COLUMN_TS_LOCAL_UPDATE =
          new Column("local_update_timestamp", Type.LONG);
      static final Column COLUMN_SYNCED = new Column("synced", Type.BOOLEAN, "0");

      static final List<Column> LAYOUT = ImmutableList.of(
          COLUMN_IMAGE_ID,
          COLUMN_LOCAL_REPORT_ID,
          COLUMN_FILE_NAME,
          COLUMN_TS_LOCAL_ADD,
          COLUMN_TS_LOCAL_UPDATE,
          COLUMN_SYNCED);

      // Implement Indices.
      static final List<List<Column>> INDICES = ImmutableList.of(
          Arrays.asList(COLUMN_FILE_NAME, ImagesTable.COLUMN_LOCAL_REPORT_ID));

      @Override public String getName() {
        return TABLE_NAME;
      }

      @Override public List<Column> getLayout() {
        return LAYOUT;
      }

      @Override public List<List<Column>> getIndices() {
        return INDICES;
      }

      static String keyEquals(long value) {
        return whereEquals(ImagesTable.COLUMN_IMAGE_ID, value);
      }
    }

    static class DbHelper extends SQLiteOpenHelper {
      // If you increment this must change onUpgrade to upgrade old versions up.
      static final int SCHEMA_VERSION = 1;
      DbHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA_VERSION);
      }

      @Override
      public void onCreate(SQLiteDatabase db) {
        db.execSQL(getCreate(new ReportsTable()));
        db.execSQL(getCreate(new ImagesTable()));
      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Preconditions.checkArgument(newVersion <= SCHEMA_VERSION,
            "Upgrading to invalid version:" + newVersion
            + " we only support up to:" + SCHEMA_VERSION);
        // TODO: First time we implement upgrading, need to support some UI telling user to wait.
      }

      private String getCreate(Table table) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(table.getName()).append(" (");
        for (Column column : table.getLayout()) {
          sql.append(column.name).append(" ").append(column.type.sqlType);
          if (column.primaryKey) {
            sql.append(" PRIMARY KEY ");
          }
          if (column.defaultValue.isPresent()) {
            sql.append(" DEFAULT " + column.defaultValue.get());
          }
          sql.append(",");
        }
        sql.deleteCharAt(sql.length()-1); // last char is extra ','
        sql.append(")");
        return sql.toString();
      }

    }
  }

  static class Column {
    public static final boolean PRIMARY = true;
    public enum Type {
      INTEGER("INTEGER"),
      LONG("INTEGER"),
      TEXT("TEXT"),
      BOOLEAN("INTEGER"),
      BLOB("BLOB");
      public String sqlType;
      private Type(String sqlType) {
        this.sqlType = sqlType;
      }
    }
    public final String name;
    public final Type type;
    public final Optional<String> defaultValue;
    public final boolean primaryKey;

    public Column(String name, Type type) {
      this.name = name;
      this.type = type;
      this.defaultValue = Optional.absent();
      this.primaryKey = false;
    }

    public Column(String name, Type type, String defaultValue) {
      this.name = name;
      this.type = type;
      this.defaultValue = Optional.of(defaultValue);
      this.primaryKey = false;
    }

    public Column(String name, Type type, boolean primaryKey) {
      this.name = name;
      this.type = type;
      this.defaultValue = Optional.absent();
      this.primaryKey = primaryKey;
    }
  }
  interface Table {
    public String getName();
    public List<Column> getLayout();
    public List<List<Column>> getIndices();
  }

}
