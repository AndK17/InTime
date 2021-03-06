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
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    Button btn, testBtn;
    DBHelper dbHelper;
    ListView lv;

    private AlarmManager alarmMgr;
    

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btn = findViewById(R.id.addButton);
        btn.setOnClickListener(this::onClick);
        dbHelper = new DBHelper(this);

        alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);

        createAlarm();
        RouteAdapter adapter = new RouteAdapter(this, makeRoutes());
        lv = (ListView) findViewById(R.id.list);
        lv.setAdapter(adapter);
        lv.setDivider(getResources().getDrawable(android.R.color.transparent));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView v = view.findViewById(R.id.textView);
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("name", v.getText().toString());
                startActivity(intent);
            }
        });

        createNotificationChannel();
    }

    public void createAlarm(){
        Intent intent = new Intent(this, DailyReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.setTime(calendar.getTime());

        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);

        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 0);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);
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

        Route[] arr = new Route[k];
        String[] daysArr = {"????", "????", "????", "????", "????", "????", "????"};


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
        }

        cursor.close();

        return arr;
    }
}