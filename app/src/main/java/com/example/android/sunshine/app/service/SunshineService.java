//package com.example.android.sunshine.app.service;
//
//import android.app.IntentService;
//import android.content.BroadcastReceiver;
//import android.content.ContentUris;
//import android.content.ContentValues;
//import android.content.Context;
//import android.content.Intent;
//import android.database.Cursor;
//import android.net.Uri;
//import android.text.format.Time;
//import android.util.Log;
//
//import com.example.android.sunshine.app.BuildConfig;
//import com.example.android.sunshine.app.data.WeatherContract;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.Vector;
//
//public class SunshineService extends IntentService {
//
//    private static final String LOG_TAG = SunshineService.class.getSimpleName();
//
//    public static final String LOCATION_QUERY_EXTRA = "lqe";
//
//    private String[] results;
//
//    public SunshineService() {
//        super("Sunshine Service");
//    }
//
//    @Override
//    protected void onHandleIntent(Intent intent) {
//
//        String locationQuery = intent.getStringExtra(LOCATION_QUERY_EXTRA);
//
//        if (locationQuery == null) {
//            return;
//        }
//
//        HttpURLConnection urlConnection = null;
//        BufferedReader reader = null;
//
//        String forecastJsonStr = null;
//
//        String format = "json";
//        String units = "metric";
//        int numDays = 14;
//
//        try {
//            final String FORECAST_BASE_URL =
//                    "http://api.openweathermap.org/data/2.5/forecast/daily?";
//            final String QUERY_PARAM = "q";
//            final String FORMAT_PARAM = "mode";
//            final String UNITS_PARAM = "units";
//            final String DAYS_PARAM = "cnt";
//            final String APPID_PARAM = "APPID";
//
//            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
//                    .appendQueryParameter(QUERY_PARAM, locationQuery)
//                    .appendQueryParameter(FORMAT_PARAM, format)
//                    .appendQueryParameter(UNITS_PARAM, units)
//                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
//                    .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
//                    .build();
//
//            URL url = new URL(builtUri.toString());
//
//            urlConnection = (HttpURLConnection) url.openConnection();
//            urlConnection.setRequestMethod("GET");
//            urlConnection.connect();
//
//            InputStream inputStream = urlConnection.getInputStream();
//            StringBuffer buffer = new StringBuffer();
//            if (inputStream == null) {
//                results = null;
//                return;
//            }
//            reader = new BufferedReader(new InputStreamReader(inputStream));
//
//            String line;
//            while ((line = reader.readLine()) != null) {
//                buffer.append(line + "\n");
//            }
//
//            if (buffer.length() == 0) {
//                results = null;
//                return;
//            }
//            forecastJsonStr = buffer.toString();
//        } catch (IOException e) {
//            Log.e(LOG_TAG, "Error ", e);
//            results = null;
//            return;
//        } finally {
//            if (urlConnection != null) {
//                urlConnection.disconnect();
//            }
//            if (reader != null) {
//                try {
//                    reader.close();
//                } catch (final IOException e) {
//                    Log.e(LOG_TAG, "Error closing stream", e);
//                }
//            }
//        }
//
//        try {
//            results = getWeatherDataFromJson(forecastJsonStr, locationQuery);
//        } catch (JSONException e) {
//            Log.e(LOG_TAG, e.getMessage(), e);
//            e.printStackTrace();
//        }
//    }
//
//    private String[] getWeatherDataFromJson(String forecastJsonStr,
//                                            String locationSetting)
//            throws JSONException {
//
//        final String OWM_CITY = "city";
//        final String OWM_CITY_NAME = "name";
//        final String OWM_COORD = "coord";
//
//        final String OWM_LATITUDE = "lat";
//        final String OWM_LONGITUDE = "lon";
//
//        final String OWM_LIST = "list";
//
//        final String OWM_PRESSURE = "pressure";
//        final String OWM_HUMIDITY = "humidity";
//        final String OWM_WINDSPEED = "speed";
//        final String OWM_WIND_DIRECTION = "deg";
//
//        final String OWM_TEMPERATURE = "temp";
//        final String OWM_MAX = "max";
//        final String OWM_MIN = "min";
//
//        final String OWM_WEATHER = "weather";
//        final String OWM_DESCRIPTION = "main";
//        final String OWM_WEATHER_ID = "id";
//
//        try {
//            JSONObject forecastJson = new JSONObject(forecastJsonStr);
//            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
//
//            JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
//            String cityName = cityJson.getString(OWM_CITY_NAME);
//
//            JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
//            double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
//            double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);
//
//            long locationId = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);
//
//            Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());
//
//            Time dayTime = new Time();
//            dayTime.setToNow();
//
//            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
//
//            dayTime = new Time();
//
//            for(int i = 0; i < weatherArray.length(); i++) {
//                long dateTime;
//                double pressure;
//                int humidity;
//                double windSpeed;
//                double windDirection;
//
//                double high;
//                double low;
//
//                String description;
//                int weatherId;
//
//                JSONObject dayForecast = weatherArray.getJSONObject(i);
//
//                dateTime = dayTime.setJulianDay(julianStartDay+i);
//
//                pressure = dayForecast.getDouble(OWM_PRESSURE);
//                humidity = dayForecast.getInt(OWM_HUMIDITY);
//                windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
//                windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);
//
//                JSONObject weatherObject =
//                        dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
//                description = weatherObject.getString(OWM_DESCRIPTION);
//                weatherId = weatherObject.getInt(OWM_WEATHER_ID);
//
//                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
//                high = temperatureObject.getDouble(OWM_MAX);
//                low = temperatureObject.getDouble(OWM_MIN);
//
//                ContentValues weatherValues = new ContentValues();
//
//                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
//                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTime);
//                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
//                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
//                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
//                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
//                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
//                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
//                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
//                weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);
//
//                cVVector.add(weatherValues);
//            }
//
//            if ( cVVector.size() > 0 ) {
//                ContentValues[] contentValues = cVVector.toArray(new ContentValues[cVVector.size()]);
//                getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, contentValues);
//            }
//            return null;
//
//        } catch (JSONException e) {
//            Log.e(LOG_TAG, e.getMessage(), e);
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    long addLocation(String locationSetting, String cityName, double lat, double lon) {
//        long locationId;
//        Uri insertedUri;
//        Cursor locationCursor = getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI,
//                new String[]{WeatherContract.LocationEntry._ID},
//                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
//                new String[]{locationSetting},
//                null);
//        if (locationCursor.moveToFirst()) {
//            int locationIdIndex = locationCursor.getColumnIndex(WeatherContract.LocationEntry._ID);
//            locationId = locationCursor.getLong(locationIdIndex);
//            locationCursor.close();
//        }
//        else {
//            ContentValues locationValues = buildLocationValues(locationSetting, cityName, lat, lon);
//            insertedUri = getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI,
//                    locationValues);
//            locationId = ContentUris.parseId(insertedUri);
//        }
//        // Students: First, check if the location with this city name exists in the db
//        // If it exists, return the current ID
//        // Otherwise, insert it using the content resolver and the base URI
//        return locationId;
//    }
//
//    private ContentValues buildLocationValues(String locationSetting, String cityName, double lat, double lon) {
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
//        contentValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
//        contentValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
//        contentValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);
//        return contentValues;
//    }
//
//    static public class AlarmReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Intent sendIntent = new Intent(context, SunshineService.class);
//            sendIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, intent.getStringExtra(SunshineService.LOCATION_QUERY_EXTRA));
//            context.startService(sendIntent);
//
//        }
//    }
//}
