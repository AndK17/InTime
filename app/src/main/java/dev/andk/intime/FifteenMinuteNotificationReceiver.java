package dev.andk.intime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class FifteenMinuteNotificationReceiver extends BroadcastReceiver {

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
        int i = (int)getBundle.getSerializable("timeReserve");
        startTime = (int)getBundle.getSerializable("startTime");

        get_cursor();

        NotificationCompat.Builder builder;
        NotificationManagerCompat notificationManager;

        Log.d("InTimeLog", "NotificationReceiver i = " + i + " id = " + getBundle.getSerializable("startTime"));
        builder = new NotificationCompat.Builder(context, "inTime")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Скоро выходить")
                .setContentText("Если вы выйдите сейчас по маршруту " + cursor.getString(
                        cursor.getColumnIndex(DBHelper.KEY_NAME)) + ", то придете за 15 минут до указаного времени")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());


        SimpleDateFormat hourFormatter = new SimpleDateFormat("HH");
        SimpleDateFormat minuteFormatter = new SimpleDateFormat("mm");
        SimpleDateFormat secondsFormatter = new SimpleDateFormat("ss");

        createAlarm(Integer.valueOf(hourFormatter.format(startTime)),
                Integer.valueOf(minuteFormatter.format(startTime)),
                Integer.valueOf(secondsFormatter.format(startTime)),
                (int) getBundle.getSerializable("id"),
                0);
    }

    void createAlarm(int hour, int minute, int seconds, int id, int timeReserve){
        Log.d("InTimeLog", "NotificationReceiver createAlarm i = " + timeReserve);
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ZeroMinuteNotificationReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("id", id);
        bundle.putSerializable("timeReserve", timeReserve);
        intent.putExtra("bundle", bundle);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, seconds);


        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
    }

    void get_cursor(){
        database = dbHelper.getWritableDatabase();
        cursor = database.query(DBHelper.TABLE_NAME, null, DBHelper.KEY_ID + "="+ getBundle.getSerializable("id"), null, null, null, null);
        cursor.moveToFirst();
    }
}
