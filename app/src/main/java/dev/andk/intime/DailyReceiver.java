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

import androidx.annotation.RequiresApi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class DailyReceiver extends BroadcastReceiver {

    Context context;
    DBHelper dbHelper;
    AlarmManager alarmMgr;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context c, Intent intent) {
        context = c;
        dbHelper = new DBHelper(context);
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        get_data();
    }

    void createAlarm(int hour, int minute, int seconds, int id, int requestCode){
        Intent intent = new Intent(context, –°heckingReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("id", id);
        bundle.putSerializable("timeReserve", 123);
        intent.putExtra("bundle", bundle);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, seconds);


        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
    }

    void get_data() {
        int i = 0;
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        String[] strDays = new String[]{
                DBHelper.KEY_IS_SUNDAY,
                DBHelper.KEY_IS_MONDAY,
                DBHelper.KEY_IS_TUESDAY,
                DBHelper.KEY_IS_WEDNESDAY,
                DBHelper.KEY_IS_THURSDAY,
                DBHelper.KEY_IS_FRIDAY,
                DBHelper.KEY_IS_SATURDAY
        };
        Calendar calendar = Calendar.getInstance();
        String day = strDays[calendar.get(calendar.DAY_OF_WEEK)-1];
        Cursor cursor = database.query(DBHelper.TABLE_NAME, null, day + "=\"1\" and " + DBHelper.KEY_IS_ACTIVE + "=\"1\"", null, null, null, null);

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat hourFormatter = new SimpleDateFormat("HH");
        SimpleDateFormat minuteFormatter = new SimpleDateFormat("mm");
        SimpleDateFormat secondsFormatter = new SimpleDateFormat("ss");

        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            int timeToIndex = cursor.getColumnIndex(DBHelper.KEY_TIME_TO);
            int routeTimeIndex = cursor.getColumnIndex(DBHelper.KEY_ROUTE_TIME);
            do {
                int startTime = cursor.getInt(timeToIndex)-cursor.getInt(routeTimeIndex)-1800000;
                try {
                    long f = formatter.parse(formatter.format(calendar.getTime().getTime())).getTime();
                    if (f > startTime+1800000)
                        return;
                    else
                        createAlarm(Integer.valueOf(hourFormatter.format(startTime)),
                                Integer.valueOf(minuteFormatter.format(startTime)),
                                Integer.valueOf(secondsFormatter.format(startTime)), cursor.getInt(idIndex), i);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                i++;
            } while (cursor.moveToNext());
        }

        cursor.close();
    }
}
