package dev.andk.intime;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;

public class RouteAdapter extends ArrayAdapter<Route> {

    public RouteAdapter(Context context, Route[] arr) {
        super(context, R.layout.adapter_item, arr);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Route route = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_item, null);
        }

        ((TextView) convertView.findViewById(R.id.textView)).setText(route.name);
        ((TextView) convertView.findViewById(R.id.dayText)).setText(route.days);

        Switch ch = (Switch) convertView.findViewById(R.id.switch1);
        ch.setChecked(route.is_active);
        ch.setOnClickListener(new SwitchListener(route));
        return convertView;
    }
}