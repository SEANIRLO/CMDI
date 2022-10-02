package com.tencent.yolov5ncnn;

import android.app.ActionBar;
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ListView;

import com.tencent.yolov5ncnn.utils.DatabaseHelper;
import com.tencent.yolov5ncnn.utils.HistoryItem;
import com.tencent.yolov5ncnn.utils.HistoryItemHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HistoryActivity extends Activity {

    private DatabaseHelper dbHelper = new DatabaseHelper(this, "HISTORY.db", null, 5);
    private List<HistoryItem> historyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBarColor();
        setContentView(R.layout.activity_history);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from history where date = ? ",
                new String[]{getCurrentDate()});
        if(cursor.moveToLast()){
            do{
                int ID = cursor.getInt(cursor.getColumnIndex("id"));
                String category = cursor.getString(cursor.getColumnIndex("category"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                float prob = cursor.getFloat(cursor.getColumnIndex("prob")) * 100;
                HistoryItem item = new HistoryItem(ID, category, time, prob);
                historyList.add(item);
            }while(cursor.moveToPrevious());
        }
        cursor.close();

        HistoryItemHelper helper = new HistoryItemHelper(HistoryActivity.this, R.layout.item_history, historyList);
        ListView listView = (ListView) findViewById(R.id.historyList);
        listView.setDivider(new ColorDrawable(getColor(R.color.grey)));
        listView.setDividerHeight(10);

        listView.setAdapter(helper);
    }

    private String getCurrentDate()
    {
        Calendar calendar = Calendar.getInstance();
        int curTime[] = {calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND)};
        String curDate = Integer.toString(curTime[0]) + "-" + Integer.toString(curTime[1]) + "-" + Integer.toString(curTime[2]);
        return curDate;
    }

    private void setBarColor()
    {
        getWindow().setNavigationBarColor(getColor(R.color.blue));
        getWindow().setStatusBarColor(getColor(R.color.blue));
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getColor(R.color.blue)));
    }
}