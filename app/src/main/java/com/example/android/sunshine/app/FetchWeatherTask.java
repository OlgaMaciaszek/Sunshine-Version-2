package com.example.android.sunshine.app;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchWeatherTask extends AsyncTask<Void, Void, Void> {

    private static final String URL = "http://api.openweathermap.org/data/2.5/forecast/daily?q=05230&units=metric&cnt=7&APPID="
            + BuildConfig.OPEN_WEATHER_MAP_API_KEY;
    private static final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    @Override
    protected Void doInBackground(Void... voids) {
        HttpURLConnection urlConnection = null;
        BufferedReader bufferedReader = null;
        String forecastJson = null;

        try {
            java.net.URL url = new URL(URL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();

            StringBuilder stringBuilder = new StringBuilder();

            if (inputStream == null) {
                return null;
            }

            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            if (stringBuilder.length() == 0) {
                return null;
            }
            forecastJson = stringBuilder.toString();

            Log.v(LOG_TAG, "Forecast JSON String: " + forecastJson);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error", e);
                }
            }
        }
        return null;
    }
}