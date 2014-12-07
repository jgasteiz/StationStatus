package com.fuzzingtheweb.stationstatus.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;


public class PreferencesHandler {

    private static final String STATION = "station";
    private static final String LINE = "line";

    public static String getStation(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getString(STATION, null);
    }

    public static String getLine(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getString(LINE, null);
    }

    public static void setStation(Context context, String station) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings.edit().putString(STATION, station).apply();
    }

    public static void setLine(Context context, String line) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings.edit().putString(LINE, line).apply();
    }

}
