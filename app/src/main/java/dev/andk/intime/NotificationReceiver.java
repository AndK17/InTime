package dev.andk.intime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class NotificationReceiver extends BroadcastReceiver {

    Context context;
    DBHelper dbHelper;
    SQLiteDatabase database;
    Cursor cursor;
    Bundle getBundle;
    int startTime;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context c, Intent intent) {
        context = c;
        dbHelper = new DBHelper(context);
        getBundle = intent.getBundleExtra("bundle");
        int timeReserve = (int)getBundle.getSerializable("timeReserve");
        startTime = (int)getBundle.getSerializable("startTime");

        get_cursor();

        NotificationCompat.Builder builder = null;
        NotificationManagerCompat notificationManager;

        switch (timeReserve){
            case 15:
                builder = new NotificationCompat.Builder(context, "inTime")
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("Скоро выходить")
                        .setContentText("Если вы выйдите сейчас по маршруту " + cursor.getString(
                                cursor.getColumnIndex(DBHelper.KEY_NAME)) + ", то придете за 15 минут до указаного времени")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                break;
            case 0:
                builder = new NotificationCompat.Builder(context, "inTime")
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("Скоро выходить")
                        .setContentText("Если вы выйдите сейчас по маршруту " + cursor.getString(
                                cursor.getColumnIndex(DBHelper.KEY_NAME)) + ", то придете ровно к указаному времени")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                break;
        }
        notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify((int)getBundle.getSerializable("id"), builder.build());
    }

    void get_cursor(){
        database = dbHelper.getWritableDatabase();
        cursor = database.query(DBHelper.TABLE_NAME, null, DBHelper.KEY_ID + "="+ getBundle.getSerializable("id"), null, null, null, null);
        cursor.moveToFirst();
    }
}
