package com.islandturtlewatch.nest.reporter.data;

import java.util.List;

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
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.ReportRef;
import com.islandturtlewatch.nest.data.ReportProto.ReportWrapper;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore.Column.Type;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore.StorageDefinition.ReportsTable;

public class LocalDataStore {
  private static final String TAG = LocalDataStore.class.getCanonicalName();
  private final StorageDefinition.DbHelper storageHelper;

  public LocalDataStore(Context context) {
    storageHelper = new StorageDefinition.DbHelper(context);
  }

  private Cursor getActiveReportsCursor() {
    SQLiteDatabase db = storageHelper.getReadableDatabase();

    return db.query(
        ReportsTable.TABLE_NAME, // table name
        CachedReportWrapper.requiredColumns, // cols to select
        isTrue(ReportsTable.COLUMN_ACTIVE), // where
        null, // don't need selection args
        null, // don't group
        null, // don't filter
        ReportsTable.COLUMN_TS_LOCAL_ADD.name // sort
        );
  }

  public int activeReportCount() {
    @Cleanup Cursor cursor = getActiveReportsCursor();
    return cursor.getCount();
  }

  public ImmutableList<CachedReportWrapper> listActiveReports() {
    @Cleanup Cursor cursor = getActiveReportsCursor();

    ImmutableList.Builder<CachedReportWrapper> output = ImmutableList.builder();
    while (cursor.moveToNext()) {
      output.add(CachedReportWrapper.from(cursor));
    }
    return output.build();
  }

  public CachedReportWrapper getReport(long localId) {
    SQLiteDatabase db = storageHelper.getReadableDatabase();

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
    SQLiteDatabase db = storageHelper.getWritableDatabase();

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
   * Saves updates from server.
   *
   * Requires InitReportId to have been called for existing report, else we will add a new row.
   */
  public void saveReport(ReportWrapper reportWrapper) {
    SQLiteDatabase db = storageHelper.getWritableDatabase();

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
   * Should be called after first checking in a new report so we can find it on future updates.
   */
  public void setServerSideData(long localId, ReportRef ref) {
    SQLiteDatabase db = storageHelper.getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(ReportsTable.COLUMN_REPORT_ID.name, ref.getReportId());
    values.put(ReportsTable.COLUMN_VERSION.name, ref.getVersion());

    int numberUpdated = db.update(ReportsTable.TABLE_NAME,
        values,
        ReportsTable.keyEquals(localId),
        null);
    Preconditions.checkArgument(numberUpdated == 1,
        "setServerSideData should update one row not " + numberUpdated);
  }

  /**
   * Creates empty report record and returns the local id.
   * @return local_id local identifier of a report, not the same as report_id on server.
   */
  public CachedReportWrapper createReport() {
    SQLiteDatabase db = storageHelper.getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(ReportsTable.COLUMN_REPORT.name, Report.getDefaultInstance().toByteArray());
    values.put(ReportsTable.COLUMN_ACTIVE.name, true);
    values.put(ReportsTable.COLUMN_TS_LOCAL_UPDATE.name, System.currentTimeMillis());
    values.put(ReportsTable.COLUMN_TS_LOCAL_ADD.name, System.currentTimeMillis());

    long localId = db.insert(ReportsTable.TABLE_NAME, null, values);
    Preconditions.checkArgument(localId != -1, "Failed to insert new row");
    return getReport(localId);
  }

  private static boolean getBool(Cursor cursor, Column column) {
    return cursor.getInt(cursor.getColumnIndexOrThrow(column.name)) == 1;
  }

  private static long getLong(Cursor cursor, Column column) {
    return cursor.getLong(cursor.getColumnIndexOrThrow(column.name));
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

  static <T extends Object> String whereEquals(Column column, T value) {
    return column.name + " = " + value.toString();
  }

  static String isTrue(Column column) {
    return column.name + " = 1";
  }

  @Data
  @Builder(fluent=false)
  public static class CachedReportWrapper {
    private long localId;
    private boolean synched;
    private boolean active;
    @NonNull private Report report;

    static final String [] requiredColumns = {
      ReportsTable.COLUMN_LOCAL_ID.name,
      ReportsTable.COLUMN_ACTIVE.name,
      ReportsTable.COLUMN_SYNCED.name,
      ReportsTable.COLUMN_REPORT.name};

    static CachedReportWrapper from(Cursor cursor) {
      CachedReportWrapperBuilder builder = CachedReportWrapper.builder()
          .setLocalId(getLong(cursor, ReportsTable.COLUMN_LOCAL_ID))
          .setActive(getBool(cursor, ReportsTable.COLUMN_ACTIVE))
          .setSynched(getBool(cursor, ReportsTable.COLUMN_SYNCED));
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
      static final Column COLUMN_LOCAL_ID = new Column("local_id", Type.LONG, true);
      static final Column COLUMN_REPORT_ID = new Column("report_id", Type.LONG);
      static final Column COLUMN_VERSION = new Column("version", Type.LONG);
      static final Column COLUMN_ACTIVE = new Column("active", Type.BOOLEAN);
      static final Column COLUMN_TS_LOCAL_ADD = new Column("local_add_timestamp", Type.LONG);
      static final Column COLUMN_TS_LOCAL_UPDATE =
          new Column("local_update_timestamp", Type.LONG);
      static final Column COLUMN_SYNCED = new Column("synced", Type.BOOLEAN);
      static final Column COLUMN_REPORT = new Column("report", Type.BLOB);

      static final List<Column> LAYOUT = ImmutableList.of(
          COLUMN_LOCAL_ID,
          COLUMN_REPORT_ID,
          COLUMN_VERSION,
          COLUMN_ACTIVE,
          COLUMN_TS_LOCAL_ADD,
          COLUMN_TS_LOCAL_UPDATE,
          COLUMN_SYNCED,
          COLUMN_REPORT);

      @Override public String getName() {
        return TABLE_NAME;
      }

      @Override public List<Column> getLayout() {
        return LAYOUT;
      }

      static String keyEquals(long value) {
        return whereEquals(ReportsTable.COLUMN_LOCAL_ID, value);
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
          sql.append(",");
        }
        sql.deleteCharAt(sql.length()-1); // last char is extra ','
        sql.append(")");
        return sql.toString();
      }

    }
  }

  static class Column {
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
    public final boolean primaryKey;
    public Column(String name, Type type) {
      this.name = name;
      this.type = type;
      this.primaryKey = false;
    }
    public Column(String name, Type type, boolean primaryKey) {
      this.name = name;
      this.type = type;
      this.primaryKey = primaryKey;
    }
  }
  interface Table {
    public String getName();
    public List<Column> getLayout();
  }

}
