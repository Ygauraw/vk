package com.gark.vk.utils;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;

public class PlayerUtils {

    private static final String SHUFFLE = "SHUFFLE";
    private static final String REPEAT = "REPEAT";
    public static final String PLAY_OPTIONS = "PLAY_OPTIONS";


    public static void setRepeat(Context context, boolean repeat) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PLAY_OPTIONS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(REPEAT, repeat);
        editor.commit();
    }

    public static boolean getRepeat(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PLAY_OPTIONS, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(REPEAT, false);
    }


    public static void setShuffle(Context context, boolean shuffle) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PLAY_OPTIONS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SHUFFLE, shuffle);
        editor.commit();
    }

    public static boolean getShuffle(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PLAY_OPTIONS, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(SHUFFLE, false);
    }
}
