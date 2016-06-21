package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.EXTRA_TEXT;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String DETAIL_URI = "URI";
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final int DETAIL_LOADER = 0;
    private static final String FORECAST_HASHTAG = "#SunshineApp";
    private ShareActionProvider shareActionProvider;

    private String forecastStr;

    private Uri uri;

    private ImageView iconView;
    private TextView friendlyDateView;
    private TextView dateView;
    private TextView descriptionView;
    private TextView highTempView;
    private TextView lowTempView;
    private TextView humidityView;
    private TextView windView;
    private TextView pressureView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //            Intent detailIntent = getActivity().getIntent();
//            if (detailIntent != null ) {
//                forecastStr = detailIntent.getDataString();
//                ((TextView) rootView.findViewById(R.id.detail_text))
//                        .setText(forecastStr);
//            }
        Bundle arguments = getArguments();
        if (arguments != null) {
            uri = arguments.getParcelable(DETAIL_URI);
        }
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        iconView = (ImageView) view.findViewById(R.id.detail_icon);
        friendlyDateView = (TextView) view.findViewById(R.id.detail_day_textview);
        dateView = (TextView) view.findViewById(R.id.detail_date_textview);
        descriptionView = (TextView) view.findViewById(R.id.detail_forecast_textview);
        highTempView = (TextView) view.findViewById(R.id.detail_high_textview);
        lowTempView = (TextView) view.findViewById(R.id.detail_low_textview);
        humidityView = (TextView) view.findViewById(R.id.detail_humidity_textview);
        windView = (TextView) view.findViewById(R.id.detail_wind_textview);
        pressureView = (TextView) view.findViewById(R.id.detail_pressure_textview);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_fragment, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if (forecastStr != null) {
            shareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(ACTION_SEND);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(EXTRA_TEXT, forecastStr + FORECAST_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");

        if (null != uri) {
            return new CursorLoader(getActivity(),
                    uri, DetailProjection.DETAIL_COLUMNS,
                    null,
                    null,
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) {
            return;
        }
        int weatherId = data.getInt(DetailProjection.COL_WEATHER_CONDITION_ID);
        iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));

        long dateInMillis = data.getLong(DetailProjection.COL_WEATHER_DATE);
        friendlyDateView.setText(Utility.getFriendlyDayString(getActivity(), dateInMillis));
        String dateText = Utility.getFormattedMonthDay(getActivity(), dateInMillis);
        dateView.setText(dateText);

        String description = data.getString(DetailProjection.COL_WEATHER_DESC);
        descriptionView.setText(description);

        boolean isMetric = Utility.isMetric(getActivity());

        Double highTemp = data.getDouble(DetailProjection.COL_WEATHER_MAX_TEMP);
        highTempView.setText(Utility.formatTemperature(getActivity(), highTemp, isMetric));

        Double lowTemp = data.getDouble(DetailProjection.COL_WEATHER_MIN_TEMP);
        lowTempView.setText(Utility.formatTemperature(getActivity(), lowTemp, isMetric));

        float humidity = data.getFloat(DetailProjection.COL_WEATHER_HUMIDITY);
        humidityView.setText(getString(R.string.format_humidity, humidity));

        float windSpeed = data.getFloat(DetailProjection.COL_WEATHER_WIND_SPEED);
        float windDir = data.getFloat(DetailProjection.COL_WEATHER_DEGREES);
        windView.setText(Utility.getFormattedWind(getActivity(), windSpeed, windDir));

        float pressure = data.getFloat(DetailProjection.COL_WEATHER_PRESSURE);
        pressureView.setText(getString(R.string.format_pressure, pressure));

        forecastStr = String.format("%s - %s - %s/%s", dateText, description, highTemp, lowTemp);

        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    void onLocationChanged(String newLocation) {
        Uri uri = this.uri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            this.uri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }
}

