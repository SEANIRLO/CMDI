package com.tencent.yolov5ncnn.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String CREATE_HISTORY = "create table history ("
            + "id integer primary key autoincrement, "
            + "category text, "
            + "prob real, "
            + "date text, "
            + "time text, "
            + "longitude real, "
            + "latitude real)";
    private Context mContext;

    public DatabaseHelper(Context context, String name,
                          SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
        mContext = context;
    }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_HISTORY);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists history");
            onCreate(db);
        }
}
