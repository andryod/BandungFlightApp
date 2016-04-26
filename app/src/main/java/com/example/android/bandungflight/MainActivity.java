package com.example.android.bandungflight;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.example.android.bandungflight.data.FlightContract;
import com.example.android.bandungflight.data.FlightDbHelper;

public class MainActivity extends AppCompatActivity {

    // Specify the columns we need.
    private static final String[] FLIGHT_COLUMNS = {
            FlightContract.FlightEntry._ID,
            FlightContract.FlightEntry.COLUMN_FLIGHT_ID,
            FlightContract.FlightEntry.COLUMN_CARRIER_CODE,
            FlightContract.FlightEntry.COLUMN_FLIGHT_NUMBER,
            FlightContract.FlightEntry.COLUMN_CARRIER_NAME,
            FlightContract.FlightEntry.COLUMN_DEPARTURE_CITY_NAME,
            FlightContract.FlightEntry.COLUMN_ARRIVAL_CITY_NAME,
            FlightContract.FlightEntry.COLUMN_DEPARTURE_TIMESTAMP
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_FLIGHT_INT_ID = 0;
    static final int COL_FLIGHT_ID = 1;
    static final int COL_CARRIER_CODE = 2;
    static final int COL_FLIGHT_NUMBER = 3;
    static final int COL_CARRIER_NAME = 4;
    static final int COL_DEPARTURE_CITY_NAME = 5;
    static final int COL_ARRIVAL_CITY_NAME = 6;
    static final int COL_DEPARTURE_TIMESTAMP = 7;

    private FlightAdapter mFlightAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ListView listView = (ListView) findViewById(android.R.id.list);
            listView.setNestedScrollingEnabled(true);
        }
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        collapsingToolbarLayout.setExpandedTitleColor(ContextCompat.getColor(this, android.R.color.transparent));


        FetchFlightTask fetchDataTask = new FetchFlightTask(this);
        fetchDataTask.execute();
        ReadFlightDataTask readDataTask = new ReadFlightDataTask();
        readDataTask.execute();
    }

    private class ReadFlightDataTask extends AsyncTask<Void, Void, Cursor> {
        @Override
        protected Cursor doInBackground(Void... params) {
            FlightDbHelper dbHelper = new FlightDbHelper(MainActivity.this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            return db.query(FlightContract.FlightEntry.TABLE_NAME,
                    FLIGHT_COLUMNS, null, null, null, null, FlightContract.FlightEntry.COLUMN_DEPARTURE_TIMESTAMP);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            if (cursor != null) {
                ListView listView = (ListView) findViewById(android.R.id.list);
                mFlightAdapter = new FlightAdapter(MainActivity.this, cursor, 0);
                listView.setAdapter(mFlightAdapter);
            }
        }
    }

}
