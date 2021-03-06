package com.example.android.bandungflight;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.bandungflight.data.FlightContract;

/**
 * Created by Andry on 23/04/2016.
 */
public class FlightAdapter extends android.support.v4.widget.CursorAdapter {

    public FlightAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_item_flight_layout, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.flightNameView = (TextView) view.findViewById(R.id.list_item_flight_name);
        holder.routeView = (TextView) view.findViewById(R.id.list_item_flight_route);
        holder.departureTimeView = (TextView) view.findViewById(R.id.list_item_flight_departure_time);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.flightNameView.setText(context.getString(R.string.format_flight_name,
                cursor.getString(MainActivity.COL_CARRIER_CODE),
                cursor.getString(MainActivity.COL_FLIGHT_NUMBER),
                cursor.getString(MainActivity.COL_CARRIER_NAME)));

        holder.routeView.setText(context.getString(R.string.format_flight_route,
                cursor.getString(MainActivity.COL_DEPARTURE_CITY_NAME),
                cursor.getString(MainActivity.COL_ARRIVAL_CITY_NAME)));
        String departureHourMinute = cursor.getString(MainActivity.COL_DEPARTURE_TIMESTAMP).substring(11, 16);
        holder.departureTimeView.setText(context.getString(R.string.format_flight_departure, departureHourMinute));
    }

    public class ViewHolder {
        public TextView flightNameView;
        public TextView routeView;
        public TextView departureTimeView;
    }

}
