package dev.andk.intime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class СheckingReceiver extends BroadcastReceiver {


    Context context;
    DBHelper dbHelper;
    SQLiteDatabase database;
    Cursor cursor;
    Bundle getBundle;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context c, Intent intent) {
        context = c;
        dbHelper = new DBHelper(context);
        getBundle = intent.getBundleExtra("bundle");

        get_cursor();
        LatLng from = new LatLng(cursor.getFloat(cursor.getColumnIndex(DBHelper.KEY_START_LAT)), cursor.getFloat(cursor.getColumnIndex(DBHelper.KEY_START_LNG)));
        LatLng to = new LatLng(cursor.getFloat(cursor.getColumnIndex(DBHelper.KEY_FINISH_LAT)), cursor.getFloat(cursor.getColumnIndex(DBHelper.KEY_FINISH_LNG)));
        new TaskDirectionRequest().execute(getRequestedUrl(from, to));
    }


    void createAlarm(int hour, int minute, int seconds, int id, int timeReserve, int startTime){
        int requestCode = id+timeReserve;

        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, NotificationReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("id", id);
        bundle.putSerializable("startTime", startTime);
        bundle.putSerializable("timeReserve", timeReserve);
        intent.putExtra("bundle", bundle);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, requestCode, intent, 0);

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

    private String getRequestedUrl(LatLng origin, LatLng destination) {
        String strOrigin = "origin=" + origin.latitude + "," + origin.longitude;
        String strDestination = "destination=" + destination.latitude + "," + destination.longitude;
        String sensor = "sensor=false";
        String mode = "mode=transit";

        String param = strOrigin + "&" + strDestination + "&" + sensor + "&" + mode;
        String APIKEY = "AIzaSyCxZDoItXyfsie7K68vMPJviYXCCkpxS5A";

        String url = "https://maps.googleapis.com/maps/api/directions/json?" + param + "&key=" + APIKEY;
        return url;
    }

    private String requestDirection(String requestedUrl) {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(requestedUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            inputStream = httpURLConnection.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            responseString = stringBuffer.toString();
            bufferedReader.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        httpURLConnection.disconnect();
        return responseString;
    }


    public class TaskDirectionRequest extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try {
                responseString = requestDirection(strings[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String responseString) {
            super.onPostExecute(responseString);
            //Json object parsing
            TaskParseDirection parseResult = new TaskParseDirection();
            parseResult.execute(responseString);
        }
    }

    public class TaskParseDirection extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonString) {
            List<List<HashMap<String, String>>> routes = null;
            JSONObject jsonObject = null;
            JSONArray jTime = null;

            try {
                jsonObject = new JSONObject(jsonString[0]);
                DirectionParser parser = new DirectionParser();
                routes = parser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                jTime = jsonObject.getJSONArray("routes");
                int time = (int) ((JSONObject) ((JSONObject) (((JSONObject) jTime.get(0))
                        .getJSONArray("legs").get(0))).get("duration")).get("value")*1000;
                int startTime = cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_TIME_TO)) - time - 900000;

                SimpleDateFormat hourFormatter = new SimpleDateFormat("HH");
                SimpleDateFormat minuteFormatter = new SimpleDateFormat("mm");
                SimpleDateFormat secondsFormatter = new SimpleDateFormat("ss");

                createAlarm(Integer.valueOf(hourFormatter.format(startTime)),
                        Integer.valueOf(minuteFormatter.format(startTime)),
                        Integer.valueOf(secondsFormatter.format(startTime)),
                        (int) getBundle.getSerializable("id"),
                        15,
                        startTime+900000);

                startTime += 900000;

                createAlarm(Integer.valueOf(hourFormatter.format(startTime)),
                        Integer.valueOf(minuteFormatter.format(startTime)),
                        Integer.valueOf(secondsFormatter.format(startTime)),
                        (int) getBundle.getSerializable("id"),
                        0,
                        startTime+900000);
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return routes;
        }

    }
}
