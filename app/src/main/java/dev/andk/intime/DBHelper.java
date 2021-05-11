package dev.andk.intime;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.security.keystore.KeyNotYetValidException;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "InTime";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "Routes";

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_START_NAME = "start_name";
    public static final String KEY_FINISH_NAME = "finish_name";
    public static final String KEY_START_LAT = "start_x";
    public static final String KEY_START_LNG = "start_y";
    public static final String KEY_FINISH_LAT = "finish_x";
    public static final String KEY_FINISH_LNG = "finish_y";
    public static final String KEY_TIME_TO = "time_to";
    public static final String KEY_ROUTE_TIME = "route_time";
    public static final String KEY_IS_MONDAY = "is_Monday";
    public static final String KEY_IS_TUESDAY = "is_Tuesday";
    public static final String KEY_IS_WEDNESDAY = "is_Wednesday";
    public static final String KEY_IS_THURSDAY = "is_Thursday";
    public static final String KEY_IS_FRIDAY = "is_Friday";
    public static final String KEY_IS_SATURDAY = "is_Saturday";
    public static final String KEY_IS_SUNDAY = "is_Sunday";
    public static final String KEY_IS_ACTIVE = "is_active";

    public DBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION); }
        @Override
        public void onCreate(SQLiteDatabase db) {
            String query = "CREATE TABLE " + TABLE_NAME + " (" + KEY_ID +
                    " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_NAME +  " TEXT, "  +
                    KEY_START_NAME + " TEXT, " +
                    KEY_FINISH_NAME + " TEXT, " +
                    KEY_START_LAT + " REAL, " +
                    KEY_START_LNG + " REAL, " +
                    KEY_FINISH_LAT + " REAL, " +
                    KEY_FINISH_LNG + " REAL, " +
                    KEY_TIME_TO + " INTEGER, " +
                    KEY_ROUTE_TIME + " INTEGER, " +
                    KEY_IS_MONDAY + " INTEGER, " +
                    KEY_IS_TUESDAY + " INTEGER, " +
                    KEY_IS_WEDNESDAY + " INTEGER, " +
                    KEY_IS_THURSDAY + " INTEGER, " +
                    KEY_IS_FRIDAY + " INTEGER, " +
                    KEY_IS_SATURDAY + " INTEGER, " +
                    KEY_IS_SUNDAY + " INTEGER, " +
                    KEY_IS_ACTIVE + " INTEGER);";
            db.execSQL(query);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
    }

}


//id
//        координаты начала
//        название координат начала
//        координаты конца
//        название координат конца
//        время
//        в понедельник
//        в овторник
//        в среду
//        в четверг
//        в пятницу
//        в субботу
//        в воскресенье