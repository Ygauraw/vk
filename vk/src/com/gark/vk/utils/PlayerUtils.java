package com.gark.vk.utils;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.RemoteException;
import android.widget.Toast;

import com.gark.vk.R;
import com.gark.vk.db.BlockedTokensColumns;
import com.gark.vk.db.BlockedTokensQuery;
import com.gark.vk.db.MusicColumns;
import com.gark.vk.db.VKDBSchema;
import com.gark.vk.model.BlockedTokensObject;
import com.gark.vk.model.MusicObject;

import java.util.ArrayList;
import java.util.Calendar;

public class PlayerUtils {

    private static final String SHUFFLE = "SHUFFLE";
    private static final String REPEAT = "REPEAT";
    private static final String LAST_POSITION = "LAST_POSITION";
    public static final String PLAY_OPTIONS = "PLAY_OPTIONS";


    public static void setLastPosition(Context context, int position) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PLAY_OPTIONS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(LAST_POSITION, position);
        editor.commit();
    }

    public static int getLastPosition(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PLAY_OPTIONS, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(LAST_POSITION, 0);
    }


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

    public static void notifyIfNoInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isInternetPresent = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        if (!isInternetPresent) {
            Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
        }
    }

    public static void manageBlockToken(Context context) {

        final long delta = 1500 * 60 * 60 * 24;
//        final long delta = 1000;
        ArrayList<String> list = new ArrayList<String>();

        Cursor cursor = context.getContentResolver().query(BlockedTokensObject.CONTENT_URI, BlockedTokensQuery.PROJECTION, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long time = cursor.getLong(cursor.getColumnIndex(BlockedTokensColumns.BLOCKED_TIME.getName()));
                String id = cursor.getString(cursor.getColumnIndex(BlockedTokensColumns._ID.getName()));
                if (Calendar.getInstance().getTimeInMillis() - time > delta) {
                    list.add(id);
                }
            }
            while (cursor.moveToNext());
        }

        if (cursor != null)
            cursor.close();

        if (!list.isEmpty()) {
            final ArrayList<ContentProviderOperation> deleteOperations = new ArrayList<ContentProviderOperation>();
            for (String item : list) {
                deleteOperations.add(
                        ContentProviderOperation.newDelete(BlockedTokensObject.CONTENT_URI)
                                .withSelection(BlockedTokensColumns._ID.getName() + " =? ", new String[]{item})
                                .build());
            }
            try {
                context.getContentResolver().applyBatch(VKDBSchema.CONTENT_AUTHORITY, deleteOperations);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            }
        }
    }
}
