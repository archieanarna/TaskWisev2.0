package com.example.taskwiserebirth.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AssistiveModeHelper {
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String STATUS_KEY = "assistive_mode";

    public static boolean isAssistiveModeEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(STATUS_KEY, false);
    }

    public static void setAssistiveMode(Context context, boolean enabled) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(STATUS_KEY, enabled);
        editor.apply();
    }
}
