package com.gark.vk.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Toast;

import com.gark.vk.R;
import com.gark.vk.network.ApiHelper;
import com.the111min.android.api.response.ResponseReceiver;

import java.util.Calendar;
import java.util.List;

public class StorageUtils {

    private static final String SHUFFLE = "SHUFFLE";
    private static final String REPEAT = "REPEAT";
    private static final String LAST_POSITION = "LAST_POSITION";
    public static final String PLAY_OPTIONS = "PLAY_OPTIONS";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String LAST_TOKEN_UPDATE = "last_update";
    public static final String IS_FIRST_LAUNCH = "first_launch_1";
    public static final String USER_ID = "user_id";


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


    ///////////////////////////////////////////////////////
    public static void saveUserID(Context context, String userID) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PLAY_OPTIONS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_ID, userID);
        editor.commit();
    }

    public static void eraseUserID(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PLAY_OPTIONS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(USER_ID);
        editor.commit();
    }

    public static String getUserId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PLAY_OPTIONS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(USER_ID, null);
    }


    ////////////////////////////////
    public static void saveToken(Context context, String access_token) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PLAY_OPTIONS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ACCESS_TOKEN, access_token);
        editor.putLong(LAST_TOKEN_UPDATE, Calendar.getInstance().getTimeInMillis());
        editor.commit();
    }


    public static String restoreToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PLAY_OPTIONS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(ACCESS_TOKEN, "f2e4309ccffc48a4a6a3941aa808ce55ba0499542a3f5e155182fe7ecd029467540e5437483c7dba11703");
    }

    public static long getLastTimeTokenUpdate(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PLAY_OPTIONS, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(LAST_TOKEN_UPDATE, Calendar.getInstance().getTimeInMillis());
    }


    public static int getLaunchCount(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PLAY_OPTIONS, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(IS_FIRST_LAUNCH, 0);
    }


    public static void setLaunchCount(Context context, int count) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PLAY_OPTIONS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(IS_FIRST_LAUNCH, count);
        editor.commit();
    }


    public static void notifyIfNoInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isInternetPresent = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        if (!isInternetPresent) {
            Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
        }
    }

//    public static void manageBlockToken(Context context) {
//
//        final long delta = 1500 * 60 * 60 * 24;
////        final long delta = 1000;
//        ArrayList<String> list = new ArrayList<String>();
//
//        Cursor cursor = context.getContentResolver().query(BlockedTokensObject.CONTENT_URI, BlockedTokensQuery.PROJECTION, null, null, null);
//        if (cursor != null && cursor.moveToFirst()) {
//            do {
//                long time = cursor.getLong(cursor.getColumnIndex(BlockedTokensColumns.BLOCKED_TIME.getName()));
//                String id = cursor.getString(cursor.getColumnIndex(BlockedTokensColumns._ID.getName()));
//                if (Calendar.getInstance().getTimeInMillis() - time > delta) {
//                    list.add(id);
//                }
//            }
//            while (cursor.moveToNext());
//        }
//
//        if (cursor != null)
//            cursor.close();
//
//        if (!list.isEmpty()) {
//            final ArrayList<ContentProviderOperation> deleteOperations = new ArrayList<ContentProviderOperation>();
//            for (String item : list) {
//                deleteOperations.add(
//                        ContentProviderOperation.newDelete(BlockedTokensObject.CONTENT_URI)
//                                .withSelection(BlockedTokensColumns._ID.getName() + " =? ", new String[]{item})
//                                .build());
//            }
//            try {
//                context.getContentResolver().applyBatch(VKDBSchema.CONTENT_AUTHORITY, deleteOperations);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            } catch (OperationApplicationException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public static boolean isVKpresents(Context context) {
        boolean result = false;
        List<ApplicationInfo> packages = context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : packages) {
            if ("com.vkontakte.android".equals(packageInfo.packageName)) {
                result = true;
            }
        }
        return result;
    }

    public static void updateToken(final Context context) {
        new ApiHelper(context, new ResponseReceiver() {
            @Override
            public void onRequestSuccess(int token, Bundle result) {
                Log.e("onRequestSuccess");
            }

            @Override
            public void onRequestFailure(int token, Bundle result) {
                Log.e("onRequestFailure");
                updateTokenFromBackUp(context);
            }

            @Override
            public void onError(int token, Exception e) {
                Log.e("onError");
                updateTokenFromBackUp(context);
            }
        }).getNewToken();
    }


    private static void updateTokenFromBackUp(Context context) {
        new ApiHelper(context, new ResponseReceiver() {
            @Override
            public void onRequestSuccess(int token, Bundle result) {
                Log.e("onRequestSuccess");
            }

            @Override
            public void onRequestFailure(int token, Bundle result) {
                Log.e("onRequestFailure");
            }

            @Override
            public void onError(int token, Exception e) {
                Log.e("onError");
            }
        }).getNewTokenFromBackUp();
    }

    public static void sendNewToken(String userId, String token, Context context) {
        new ApiHelper(context, new ResponseReceiver() {
            @Override
            public void onRequestSuccess(int token, Bundle result) {

            }

            @Override
            public void onRequestFailure(int token, Bundle result) {

            }

            @Override
            public void onError(int token, Exception e) {

            }
        }).addToken(userId, token);
    }

    public static void sendAuthTokenError(String token, Context context) {
        new ApiHelper(context, new ResponseReceiver() {
            @Override
            public void onRequestSuccess(int token, Bundle result) {

            }

            @Override
            public void onRequestFailure(int token, Bundle result) {

            }

            @Override
            public void onError(int token, Exception e) {

            }
        }).sendAuthErrorToken(token);
    }

    public static String getAppVersion(Context context) {

        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String version = "";

        if (pInfo != null && pInfo.versionName != null) {
            version = pInfo.versionName;
        }

        return version;
    }
}
