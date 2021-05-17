package dev.andk.intime;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.Switch;

public class SwitchListener implements View.OnClickListener {
    Route route;
    public SwitchListener(Route route) {
        this.route = route;
    }

    @Override
    public void onClick(View v) {
        route.is_active = ((Switch) v).isChecked();
        ContentValues values = new ContentValues();
        DBHelper dbHelper = new DBHelper(((Switch)v).getContext());
        values.put(DBHelper.KEY_IS_ACTIVE, route.is_active);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String where = DBHelper.KEY_NAME + "=\"" + route.name + "\"";
        db.update(DBHelper.TABLE_NAME, values, where, null);
    }
}
