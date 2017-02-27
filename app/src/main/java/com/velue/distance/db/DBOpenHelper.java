package com.velue.distance.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DB_NAME = "compute_distance.db";

    public DBOpenHelper(Context context){
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS milestone(id INTEGER PRIMARY KEY AUTOINCREMENT, distance INTEGER,longitude DOUBLE, latitude DOUBLE )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table milestone");
        db.execSQL("CREATE TABLE IF NOT EXISTS milestone(id INTEGER PRIMARY KEY AUTOINCREMENT, distance INTEGER,longitude FLOAT, latitude FLOAT )");
    }

}
