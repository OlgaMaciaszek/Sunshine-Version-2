package com.example.android.sunshine.app;


import com.example.android.sunshine.app.data.WeatherContract;

public class NotifyWeatherProjection {

    public static final String[] COLUMNS = new String[] {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
    };
    // these indices must match the projection
    public static final int INDEX_WEATHER_ID = 0;
    public static final int INDEX_MAX_TEMP = 1;
    public static final int INDEX_MIN_TEMP = 2;
    public static final int INDEX_SHORT_DESC = 3;
}
