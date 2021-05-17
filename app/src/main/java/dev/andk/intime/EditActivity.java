package dev.andk.intime;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class EditActivity extends Activity {

    Button backBtn, addBtn, timeBtn, delBtn;
    TextView currentDateTime;
    EditText nameText, fromText, toText;
    Calendar dateAndTime = Calendar.getInstance();
    DBHelper dbHelper;
    ContentValues values;
    SQLiteDatabase database;
    Cursor cursor;
    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit);

        backBtn = findViewById(R.id.backButton);
        backBtn.setOnClickListener(this::backOnClick);
        addBtn = findViewById(R.id.addButton);
        addBtn.setOnClickListener(this::addOnclick);
        timeBtn = findViewById(R.id.timeButton);
        timeBtn.setOnClickListener(this::setTime);
        delBtn = findViewById(R.id.delButton);
        delBtn.setOnClickListener(this::delOnClick);

        nameText = findViewById(R.id.nameText);
        fromText = findViewById(R.id.fromText);
        toText = findViewById(R.id.toText);

        currentDateTime = findViewById(R.id.timeText);

        Intent intent = getIntent();

        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();
        cursor = database.query(DBHelper.TABLE_NAME, null, DBHelper.KEY_NAME + "=\"" + intent.getStringExtra("name") + "\"", null, null, null, null);
        cursor.moveToFirst();
        id = cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_ID));

        setStartValues();
    }

    boolean is_bool(int a){
        if (a == 0)
            return false;
        else
            return true;
    }

    void setStartValues(){
        nameText.setText(cursor.getString(cursor.getColumnIndex(DBHelper.KEY_NAME)));
        fromText.setText(cursor.getString(cursor.getColumnIndex(DBHelper.KEY_START_NAME)));
        toText.setText(cursor.getString(cursor.getColumnIndex(DBHelper.KEY_FINISH_NAME)));

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
//        String time = String.valueOf(formatter.format(cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_TIME_TO))));
//        currentDateTime.setText(time);

        try {
            dateAndTime.setTime(formatter.parse(formatter.format(cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_TIME_TO)))));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        setInitialDateTime();

        Switch s = findViewById(R.id.mondaySwitch);
        s.setChecked(is_bool(cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_IS_MONDAY))));
        s = findViewById(R.id.tuesdaySwitch);
        s.setChecked(is_bool(cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_IS_TUESDAY))));
        s = findViewById(R.id.wednesdaySwitch);
        s.setChecked(is_bool(cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_IS_WEDNESDAY))));
        s = findViewById(R.id.thursdaySwitch);
        s.setChecked(is_bool(cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_IS_THURSDAY))));
        s = findViewById(R.id.fridaySwitch);
        s.setChecked(is_bool(cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_IS_FRIDAY))));
        s = findViewById(R.id.saturdaySwitch);
        s.setChecked(is_bool(cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_IS_SATURDAY))));
        s = findViewById(R.id.sundaySwitch);
        s.setChecked(is_bool(cursor.getInt(cursor.getColumnIndex(DBHelper.KEY_IS_SUNDAY))));
    }

    public void delOnClick(View v){
        String where = DBHelper.KEY_ID + "=\"" + id + "\"";
        database.delete(DBHelper.TABLE_NAME, where, null);
        Intent intent = new Intent(EditActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void addOnclick(View v) {
        if (nameText.getText().toString().equals("") || fromText.getText().toString().equals("") || toText.getText().toString().equals("")) {
            Toast.makeText(this, "Заполните вcе поля", Toast.LENGTH_SHORT).show();
        } else {
            addBtn.setEnabled(false);
            values = new ContentValues();
            Geocoder geocoder = new Geocoder(this);
            List<Address> formAddresList = new ArrayList<>();
            LatLng from, to;

            try {
                formAddresList = geocoder.getFromLocationName(fromText.getText().toString(), 1);
            } catch (IOException e) {}
            if (formAddresList.size() > 0) {
                Address address = formAddresList.get(0);
                values.put(DBHelper.KEY_START_NAME, fromText.getText().toString());
                values.put(DBHelper.KEY_START_LAT, address.getLatitude());
                values.put(DBHelper.KEY_START_LNG, address.getLongitude());
                from = new LatLng(address.getLatitude(), address.getLongitude());
            } else {
                addBtn.setEnabled(true);
                Toast.makeText(this, "Неверно указан адрес отправления", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                formAddresList = geocoder.getFromLocationName(toText.getText().toString(), 1);
            } catch (IOException e) {}
            if (formAddresList.size() > 0) {
                Address address = formAddresList.get(0);
                values.put(DBHelper.KEY_FINISH_NAME, toText.getText().toString());
                values.put(DBHelper.KEY_FINISH_LAT, address.getLatitude());
                values.put(DBHelper.KEY_FINISH_LNG, address.getLongitude());
                to = new LatLng(address.getLatitude(), address.getLongitude());
            } else {
                addBtn.setEnabled(true);
                Toast.makeText(this, "Неверно указан адрес прибытия", Toast.LENGTH_SHORT).show();
                return;
            }

            values.put(DBHelper.KEY_NAME, nameText.getText().toString());

            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            String time = currentDateTime.getText().toString();
            try {
                int t = (int) (formatter.parse(time).getTime());
                values.put(DBHelper.KEY_TIME_TO, t);
            } catch (ParseException e) {
                e.printStackTrace();
            }


            new TaskDirectionRequest().execute(getRequestedUrl(from, to));
//            values.put(DBHelper.KEY_ROUTE_TIME, 2700000);


            Switch s = findViewById(R.id.mondaySwitch);
            values.put(DBHelper.KEY_IS_MONDAY, s.isChecked()? 1 : 0);
            s = findViewById(R.id.tuesdaySwitch);
            values.put(DBHelper.KEY_IS_TUESDAY, s.isChecked()? 1 : 0);
            s = findViewById(R.id.wednesdaySwitch);
            values.put(DBHelper.KEY_IS_WEDNESDAY, s.isChecked()? 1 : 0);
            s = findViewById(R.id.thursdaySwitch);
            values.put(DBHelper.KEY_IS_THURSDAY, s.isChecked()? 1 : 0);
            s = findViewById(R.id.fridaySwitch);
            values.put(DBHelper.KEY_IS_FRIDAY, s.isChecked()? 1 : 0);
            s = findViewById(R.id.saturdaySwitch);
            values.put(DBHelper.KEY_IS_SATURDAY, s.isChecked()? 1 : 0);
            s = findViewById(R.id.sundaySwitch);
            values.put(DBHelper.KEY_IS_SUNDAY, s.isChecked()? 1 : 0);
            values.put(DBHelper.KEY_IS_ACTIVE, Boolean.TRUE);
        }
    }


    public void backOnClick(View v) {
        Intent intent = new Intent(EditActivity.this, MainActivity.class);
        intent.putExtra("createAlarm", "false");
        startActivity(intent);
    }

    // отображаем диалоговое окно для выбора времени
    public void setTime(View v) {
        new TimePickerDialog(EditActivity.this, t,
                dateAndTime.get(Calendar.HOUR_OF_DAY),
                dateAndTime.get(Calendar.MINUTE), true)
                .show();
    }


    private void setInitialDateTime() {

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        String time = String.valueOf(formatter.format(dateAndTime.getTimeInMillis()));
        currentDateTime.setText(time);
    }


    // установка обработчика выбора времени
    TimePickerDialog.OnTimeSetListener t = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            dateAndTime.set(Calendar.MINUTE, minute);
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
            setInitialDateTime();
        }
    };


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
                database = dbHelper.getWritableDatabase();
                values.put(DBHelper.KEY_ROUTE_TIME, (int) ((JSONObject) ((JSONObject) (((JSONObject) jTime.get(0))
                        .getJSONArray("legs").get(0))).get("duration")).get("value")*1000);
                String where = DBHelper.KEY_ID + "=\"" + id + "\"";
                database.update(DBHelper.TABLE_NAME, values, where, null);
                Intent intent = new Intent(EditActivity.this, MainActivity.class);
                intent.putExtra("createAlarm", "true");
                startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return routes;
        }

    }
}