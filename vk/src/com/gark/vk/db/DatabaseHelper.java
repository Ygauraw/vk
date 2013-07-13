package com.gark.vk.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "vk.android.db";
    private Context context;
    private static final int DATABASE_VERSION = 4;

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuilder sql = new StringBuilder(1024);
        sql.append("CREATE TABLE ").append(VKDBSchema.Tables.MUSIC).append(" (");
        for (MusicColumns column : MusicColumns.values()) {
            if (MusicColumns._ID.equals(column)) {
                sql.append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
            } else {
                sql.append(column.getName()).append(column.getType().getName());
            }
        }
        sql.append("UNIQUE (").append(MusicColumns.AID.getName()).append(") ON CONFLICT REPLACE)");
        db.execSQL(sql.toString());
        sql.setLength(0);

        sql.append("CREATE TABLE ").append(VKDBSchema.Tables.SUGGESTION).append(" (");
        for (SuggestionColumns column : SuggestionColumns.values()) {
            if (SuggestionColumns._ID.equals(column)) {
                sql.append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
            } else {
                sql.append(column.getName()).append(column.getType().getName());
            }
        }
        sql.append("UNIQUE (").append(SuggestionColumns.TEXT.getName()).append(") ON CONFLICT REPLACE)");
        db.execSQL(sql.toString());
        sql.setLength(0);


        sql.append("CREATE TABLE ").append(VKDBSchema.Tables.VIDEO).append(" (");
        for (VideoColumns column : VideoColumns.values()) {
            if (VideoColumns._ID.equals(column)) {
                sql.append(BaseColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
            } else {
                sql.append(column.getName()).append(column.getType().getName());
            }
        }
        sql.append("UNIQUE (").append(VideoColumns.ID.getName()).append(") ON CONFLICT REPLACE)");
        db.execSQL(sql.toString());
        sql.setLength(0);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(DatabaseHelper.class.getSimpleName(), "Upgrading database from v." + oldVersion + " to v." + newVersion);

        // drop all tables
        final String tables[] = {VKDBSchema.Tables.MUSIC, VKDBSchema.Tables.SUGGESTION, VKDBSchema.Tables.VIDEO};
        for (final String table : tables)
            db.execSQL("DROP TABLE IF EXISTS " + table);

        onCreate(db);
    }


}