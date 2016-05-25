package com.example.android.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

public class ActivityUtils {

    @NonNull
    public static String getPreferredLocation(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.pref_location_key), context.getString(R.string.pref_location_default));
    }

    public static void openPreferredLocationInMap(Context context, String logTag) {
        String location = getPreferredLocation(context);
        Uri geolocation = Uri.parse("geo:0.0?").buildUpon()
                .appendQueryParameter("q", location).build();
        Intent locationIntent = new Intent();
        locationIntent.setAction(Intent.ACTION_VIEW);
        locationIntent.setData(geolocation);
        if (locationIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(locationIntent);
        } else {
            Log.d(logTag, "Couldn't call " + location + ", no receiving apps installed");
        }
    }
}
