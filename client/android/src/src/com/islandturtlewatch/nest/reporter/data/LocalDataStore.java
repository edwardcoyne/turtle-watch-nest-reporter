package com.islandturtlewatch.nest.reporter.data;

import java.util.List;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.util.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.islandturtlewatch.nest.data.ReportProto.Report;
import com.islandturtlewatch.nest.data.ReportProto.ReportWrapper;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore.StorageDefinition.Column.Type;
import com.islandturtlewatch.nest.reporter.data.LocalDataStore.StorageDefinition.ReportsTable;

public class LocalDataStore {
  private final StorageDefinition.DbHelper storageHelper;

  public LocalDataStore(Context context) {
    storageHelper = new StorageDefinition.DbHelper(context);
  }

  public CachedReportWrapper getReport(long localId) {
    SQLiteDatabase db = storageHelper.getReadableDatabase();

    String[] returnColumns = {
      ReportsTable.COLUMN_ACTIVE.name,
      ReportsTable.COLUMN_SYNCED.name,
      ReportsTable.COLUMN_REPORT.name};

    Cursor cursor = db.query(
        ReportsTable.TABLE_NAME,
        returnColumns,
        ReportsTable.keyEquals(localId),
        null, // don't need selection args
        null, // don't group
        null, // don't filter
        null // don't sort
        );
    Preconditions.checkArgument(cursor.moveToFirst(), "Failed to find report for:" + localId);
    return CachedReportWrapper.builder()
      .setActive(getBool(cursor, ReportsTable.COLUMN_ACTIVE))
      .setSynched(getBool(cursor, ReportsTable.COLUMN_SYNCED))
      .setReport(getProto(cursor, ReportsTable.COLUMN_REPORT, Report.getDefaultInstance()))
      .build();
  }

  // Saves changes from sync.
  public void saveReport(ReportWrapper reportWrapper) {

  }

  // Saves local changes to report.
  public void saveReport(long localId, Report report) {
    SQLiteDatabase db = storageHelper.getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(ReportsTable.COLUMN_REPORT.name, report.toByteArray());
    values.put(ReportsTable.COLUMN_SYNCED.name, false);
    values.put(ReportsTable.COLUMN_TS_LOCAL_UPDATE.name, System.currentTimeMillis());

    db.update(ReportsTable.TABLE_NAME,
        values,
        StorageDefinition.whereEquals(ReportsTable.COLUMN_LOCAL_ID, localId),
        null);
  }

  /**
   * Creates empty report record and returns the local id.
   * @return local_id
   */
  public long createReport() {
    SQLiteDatabase db = storageHelper.getWritableDatabase();

    ContentValues values = new ContentValues();
    values.put(ReportsTable.COLUMN_ACTIVE.name, true);
    values.put(ReportsTable.COLUMN_TS_LOCAL_UPDATE.name, System.currentTimeMillis());
    values.put(ReportsTable.COLUMN_TS_LOCAL_ADD.name, System.currentTimeMillis());

    return db.insert(ReportsTable.TABLE_NAME, null, values);
  }

  private boolean getBool(Cursor cursor, StorageDefinition.Column column) {
    return cursor.getInt(cursor.getColumnIndexOrThrow(column.name)) == 1;
  }

  @SuppressWarnings("unchecked")
  private <T extends MessageLite> T getProto(
      Cursor cursor, StorageDefinition.Column column, T proto) {
    try {
      return (T)proto.newBuilderForType().mergeFrom(
          cursor.getBlob(cursor.getColumnIndexOrThrow(column.name)))
          .build();
    } catch (InvalidProtocolBufferException | IllegalArgumentException e) {
      throw Throwables.propagate(e);
    }
  }

  @Data
  @Builder(fluent=false)
  public static class CachedReportWrapper {
    private boolean synched;
    private boolean active;
    @NonNull private Report report;
  }

  final static class StorageDefinition {
    static final String DATABASE_NAME = "report_storage.db";
    static final int SCHEMA_VERSION = 1; // MUST INCREMENT if you change anything in this class.
    private StorageDefinition() {}

    static class ReportsTable implements BaseColumns, Table {
      static final String TABLE_NAME = "reports";
      static final Column COLUMN_LOCAL_ID = new Column("local_id", Type.INTEGER, true);
      static final Column COLUMN_REPORT_ID = new Column("report_id", Type.INTEGER);
      static final Column COLUMN_VERSION = new Column("version", Type.INTEGER);
      static final Column COLUMN_ACTIVE = new Column("active", Type.BOOLEAN);
      static final Column COLUMN_TS_LOCAL_ADD = new Column("local_add_timestamp", Type.INTEGER);
      static final Column COLUMN_TS_LOCAL_UPDATE =
          new Column("local_update_timestamp", Type.INTEGER);
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

    static <T extends Object> String whereEquals(StorageDefinition.Column column, T value) {
      return column.name + " = " + value.toString();
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
        Preconditions.checkArgument(newVersion == 1, "Upgrading to invalid version:" + newVersion);
        // There is no old version yet.
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

}
