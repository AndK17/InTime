package dev.andk.intime;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = "InTimeLog";
    
    Button btn, workerBtn;
    DBHelper dbHelper;
    ListView lv;
    boolean is_on = false;

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        PeriodicWorkRequest simpleRequest = new PeriodicWorkRequest.Builder(MyWorker.class, 1, TimeUnit.SECONDS).build();

        btn = findViewById(R.id.button);
        btn.setOnClickListener(this::onClick);
        dbHelper = new DBHelper(this);

        createAlarm();

//        startService(new Intent(this, NotificationService.class));

//        SimpleDateFormat formater1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        SimpleDateFormat formater = new SimpleDateFormat("HH:mm");
//        String strD = "0:45";
//
//        try {
//            Date date = formater.parse(strD);
//            Log.d(LOG_TAG, String.valueOf(date.getTime()+10800000));
//            Log.d(LOG_TAG, String.valueOf(formater1.format(date)));
//        }
//        catch (ParseException e) {
//            e.printStackTrace();
//        }


        RouteAdapter adapter = new RouteAdapter(this, makeRoutes());
        lv = (ListView) findViewById(R.id.list);
        lv.setAdapter(adapter);
        lv.setDivider(getResources().getDrawable(android.R.color.transparent));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView v = view.findViewById(R.id.textView);
                Log.d(LOG_TAG, v.getText().toString());
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("name", v.getText().toString());
                startActivity(intent);
            }
        });

        createNotificationChannel();
    }

    public void createAlarm(){
        Log.d("InTimeLog", "Create ALARM");
        alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, DailyReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.setTime(calendar.getTime());
        Log.d("InTimeLog", String.valueOf(calendar.getTime()));

        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);

        Log.d("InTimeLog", "Create ALARM1");
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 0);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);
        Log.d("InTimeLog", "Create ALARM2");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("inTime", name, importance);
            Uri ringURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            channel.enableVibration(true);
            channel.setSound(ringURI, Notification.AUDIO_ATTRIBUTES_DEFAULT);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        RouteAdapter adapter = new RouteAdapter(this, makeRoutes());
        lv.setAdapter(adapter);
        try {
            Intent intent = getIntent();
            if (intent.getStringExtra("createAlarm") == "true")
                createAlarm();
        }catch (NullPointerException e){
            return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, AddActivity.class);
        startActivity(intent);
    }

    Route[] makeRoutes() {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        int k = 0;

        Cursor cursor = database.query(DBHelper.TABLE_NAME, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                k++;
            } while (cursor.moveToNext());
        }
        else
            Log.d(LOG_TAG, "0 rows");

        Route[] arr = new Route[k];
        String[] daysArr = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};


        if (cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(DBHelper.KEY_NAME);
            int is_activeIndex = cursor.getColumnIndex(DBHelper.KEY_IS_ACTIVE);
            for (int i = 0; i < k; i++) {
                Route route = new Route();
                route.name = cursor.getString(nameIndex);
                if (cursor.getInt(is_activeIndex) == 1) route.is_active = true;
                else route.is_active = false;
                arr[i] = route;
                for (int d = 0; d < 7; d++){
                    if (cursor.getInt(d + 10) == 1)
                        route.days+=daysArr[d] + ", ";
                }
                route.days = route.days.substring(0, route.days.length()-2);
                cursor.moveToNext();
            }
        } else
            Log.d(LOG_TAG, "0 rows");

        cursor.close();

        return arr;
    }
}