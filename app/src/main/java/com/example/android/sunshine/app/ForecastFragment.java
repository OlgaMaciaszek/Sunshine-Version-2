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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int FORECAST_LOADER = 0;

    private static final String SELECTED_KEY = "selected position";
    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();

    private ForecastAdapter forecastAdapter;

    private ListView listView;

    private int position = ListView.INVALID_POSITION;
    private boolean useTodayLaout;

    public void setUseTodayLaout(boolean useTodayLaout) {
        this.useTodayLaout = useTodayLaout;
        if (forecastAdapter != null) {
            forecastAdapter.setUseTodayLayout(useTodayLaout);
        }
    }

    public interface Callback {
        void onItemSelected(Uri dateUri);
    }

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        updateWeather();
//    }

    private void updateWeather() {
////        Intent serviceIntent = new Intent(getActivity(), SunshineService.class);
//        String location = Utility.getPreferredLocation(getActivity());
////        serviceIntent.putExtra(LOCATION_QUERY_EXTRA, location);
////        getActivity().startService(serviceIntent);
//        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
//        intent.putExtra(LOCATION_QUERY_EXTRA, location);
//        PendingIntent alarmIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, FLAG_ONE_SHOT);
////        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, Calendar.getInstance().getTimeInMillis(),
////                5 * 60 * 1000, alarmIntent);
//        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, alarmIntent);
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        forecastAdapter = new ForecastAdapter(getActivity(), null, 0);

//        forecastAdapter = new ArrayAdapter<>(
//                getActivity(),
//                R.layout.list_item_forecast,
//                R.id.list_item_forecast_textview,
//                new ArrayList<String>());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(forecastAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    ((Callback) getActivity())
                            .onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(ForecastProjection.COL_WEATHER_DATE)));
                }
                ForecastFragment.this.position = position;
            }
        });
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            this.position = savedInstanceState.getInt(SELECTED_KEY);
        }
        forecastAdapter.setUseTodayLayout(useTodayLaout);

//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
////                Toast toast = Toast.makeText(getActivity(), forecastAdapter.getItem(i), Toast.LENGTH_SHORT);
////                toast.show();
//                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
//                detailIntent.putExtra(Intent.EXTRA_TEXT, forecastAdapter.getItem(i));
//                startActivity(detailIntent);
//            }
//        });
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocation(locationSetting);
        return new CursorLoader(getActivity(), weatherForLocationUri, ForecastProjection.FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        forecastAdapter.swapCursor(cursor);
        if (position != ListView.INVALID_POSITION) {
            listView.smoothScrollToPosition(position);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        forecastAdapter.swapCursor(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (position != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, position);
        }
        super.onSaveInstanceState(outState);
    }

    public void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    private void openPreferredLocationInMap() {
        // Using the URI scheme for showing a location found on a map.  This super-handy
        // intent can is detailed in the "Common Intents" page of Android's developer site:
        // http://developer.android.com/guide/components/intents-common.html#Maps
        if ( null != forecastAdapter ) {
            Cursor cursor = forecastAdapter.getCursor();
            if ( null != cursor ) {
                cursor.moveToPosition(0);
                String posLat = cursor.getString(ForecastProjection.COL_COORD_LAT);
                String posLong = cursor.getString(ForecastProjection.COL_COORD_LONG);
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
                }
            }

        }
    }

}