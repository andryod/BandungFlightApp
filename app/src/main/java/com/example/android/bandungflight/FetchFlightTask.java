package com.example.android.bandungflight;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.android.bandungflight.data.FlightContract;
import com.example.android.bandungflight.data.FlightContract.FlightEntry;
import com.example.android.bandungflight.data.FlightDbHelper;

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
import java.util.Locale;

/**
 * Created by Andry on 23/04/2016.
 */
public class FetchFlightTask extends AsyncTask<String, Void, Void> {
    private static final String LOG_TAG = FetchFlightTask.class.getSimpleName();

    private static final String FLIGHT_URL = "http://andryod.com/androidtraining/flights/departures";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String LAST_UPDATE_DAY_PREF = "last_update_pref";

    private Context mContext;
    private SimpleDateFormat sdf;

    public FetchFlightTask(Context context) {
        mContext = context;
        sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
    }

    @Override
    protected Void doInBackground(String... params) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String lastUpdateDate = pref.getString(LAST_UPDATE_DAY_PREF, "");
        String todayDateString = sdf.format(Calendar.getInstance().getTime());

        if (!todayDateString.equals(lastUpdateDate)) {
            // Only fetch data once per day
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String flightJsonStr = null;
            try {

                URL url = new URL(FLIGHT_URL);
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                flightJsonStr = buffer.toString();
                getFlightDataFromJson(flightJsonStr);
                // Update last data sync date
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(LAST_UPDATE_DAY_PREF, todayDateString);
                editor.commit();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error ", e);
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
        }
        return null;
    }

    private void getFlightDataFromJson(String flightJsonStr) throws  JSONException {
        final String FLIGHT_ID = "id";
        final String FLIGHT_CARRIER_CODE    = "carrierCode";
        final String FLIGHT_CARRIER_NAME    = "carrierName";
        final String FLIGHT_NUMBER          = "flightNumber";
        final String FLIGHT_DURATIONS       = "flightDurations";
        final String FLIGHT_EQUIPMENT       = "flightEquipment";

        final String FLIGHT_DEPARTURE_AIRPORT = "departureAirport";
        final String FLIGHT_DEPARTURE_TIME  = "departureTimestamp";

        final String FLIGHT_ARRIVAL_AIRPORT = "arrivalAirport";
        final String FLIGHT_ARRIVAL_TIME    = "arrivalTimestamp";

        final String FLIGHT_AIRPORT_CITY  = "city";
        final String FLIGHT_AIRPORT_NAME  = "name";

        JSONArray flightArray = new JSONArray(flightJsonStr);
        FlightDbHelper dbHelper = new FlightDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            // Delete old data first
            db.delete(FlightEntry.TABLE_NAME, null, null);
            for (int i = 0; i < flightArray.length(); i++) {
                JSONObject flightItem = flightArray.getJSONObject(i);
                JSONObject flightDepartureAirport = flightItem.getJSONObject(FLIGHT_DEPARTURE_AIRPORT);
                JSONObject flightArrivalAirport = flightItem.getJSONObject(FLIGHT_ARRIVAL_AIRPORT);

                ContentValues flightValues = new ContentValues();
                flightValues.put(FlightEntry.COLUMN_FLIGHT_ID, flightItem.getString(FLIGHT_ID));
                flightValues.put(FlightEntry.COLUMN_CARRIER_CODE, flightItem.getString(FLIGHT_CARRIER_CODE));
                flightValues.put(FlightEntry.COLUMN_CARRIER_NAME, flightItem.getString(FLIGHT_CARRIER_NAME));
                flightValues.put(FlightEntry.COLUMN_FLIGHT_NUMBER, flightItem.getString(FLIGHT_NUMBER));
                flightValues.put(FlightEntry.COLUMN_FLIGHT_DURATION, flightItem.getInt(FLIGHT_DURATIONS));
                flightValues.put(FlightEntry.COLUMN_PLANE_MODEL, flightItem.getString(FLIGHT_EQUIPMENT));
                flightValues.put(FlightEntry.COLUMN_DEPARTURE_AIRPORT_NAME, flightDepartureAirport.getString(FLIGHT_AIRPORT_NAME));
                flightValues.put(FlightEntry.COLUMN_DEPARTURE_CITY_NAME, flightDepartureAirport.getString(FLIGHT_AIRPORT_CITY));
                flightValues.put(FlightEntry.COLUMN_DEPARTURE_TIMESTAMP, flightItem.getString(FLIGHT_DEPARTURE_TIME));
                flightValues.put(FlightEntry.COLUMN_ARRIVAL_AIRPORT_NAME, flightArrivalAirport.getString(FLIGHT_AIRPORT_NAME));
                flightValues.put(FlightEntry.COLUMN_ARRIVAL_CITY_NAME, flightArrivalAirport.getString(FLIGHT_AIRPORT_CITY));
                flightValues.put(FlightEntry.COLUMN_ARRIVAL_TIMESTAMP, flightItem.getString(FLIGHT_ARRIVAL_TIME));

                db.insert(FlightEntry.TABLE_NAME, null, flightValues);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

    }
}
