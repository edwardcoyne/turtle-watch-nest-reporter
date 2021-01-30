package com.islandturtlewatch.nest.reporter.util;

import java.util.List;

import android.database.Cursor;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

public class Sql {
  private Sql() {} // static only

  public static ImmutableList<String> getAllString(Cursor cursor, Column column) {
    ImmutableList.Builder<String> output = ImmutableList.builder();
    while (cursor.moveToNext()) {
      output.add(getString(cursor, column));
    }
    return output.build();
  }

  public static boolean getBool(Cursor cursor, Column column) {
    return cursor.getInt(cursor.getColumnIndexOrThrow(column.name)) == 1;
  }

  public static int getInt(Cursor cursor, Column column) {
    return cursor.getInt(cursor.getColumnIndexOrThrow(column.name));
  }

  public static long getLong(Cursor cursor, Column column) {
    return cursor.getLong(cursor.getColumnIndexOrThrow(column.name));
  }

  public static Optional<Long> getOptLong(Cursor cursor, Column column) {
    if (cursor.isNull(cursor.getColumnIndexOrThrow(column.name))) {
      return Optional.absent();
    }
    return Optional.of(cursor.getLong(cursor.getColumnIndexOrThrow(column.name)));
  }

  public static String getString(Cursor cursor, Column column) {
    return cursor.getString(cursor.getColumnIndexOrThrow(column.name));
  }

  @SuppressWarnings("unchecked")
  public static <T extends MessageLite> Optional<T> getProto(Cursor cursor, Column column, T proto) {
    int index = cursor.getColumnIndexOrThrow(column.name);
    if (cursor.isNull(index)) {
      return Optional.absent();
    }
    try {
      return Optional.of((T)proto.newBuilderForType().mergeFrom(
          cursor.getBlob(index))
          .build());
    } catch (InvalidProtocolBufferException | IllegalArgumentException e) {
      throw new Error(e);
    }
  }

  public static String whereStringEquals(Column column, String value) {
    return column.name + " = \"" + value.toString() +"\"";
  }

  public static <T extends Object> String whereEquals(Column column, T value) {
    return column.name + " = " + value.toString();
  }

  public static String isTrue(Column column) {
    return column.name + " = 1";
  }

  public static String isFalse(Column column) {
    return column.name + " != 1";
  }

  public static String and(String cond1, String cond2) {
    return cond1 + " and " + cond2;
  }

  public static <T> String colIn(Column column, List<T> values) {
    StringBuilder in = new StringBuilder(column.name + " in (");
    for (T value : values) {
      in.append(value.toString()).append(',');
    }
    // Remove last ','
    in.setLength(in.length()-1);
    return in.toString();
  }


  public static String create(Table table) {
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

  public static class Column {
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

  public interface Table {
    public String getName();
    public List<Column> getLayout();
    public List<List<Column>> getIndices();
  }
}
