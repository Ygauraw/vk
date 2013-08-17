package com.gark.vk.network;

import android.content.Context;
import android.database.Cursor;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.gark.vk.db.BlockedTokensColumns;
import com.gark.vk.db.BlockedTokensQuery;
import com.gark.vk.model.BlockedTokensObject;
import com.gark.vk.utils.StorageUtils;
import com.the111min.android.api.BaseApiHelper;
import com.the111min.android.api.request.Request;
import com.the111min.android.api.request.Request.RequestMethod;
import com.the111min.android.api.response.ResponseReceiver;

import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Random;

public class ApiHelper extends BaseApiHelper {


    public static final int POPULAR_TOKEN = 1;
    public static final int AUDIO_TOKEN = 2;
    public static final int VIDEO_TOKEN = 3;
    public static final int COUNT = 40;
    private Random random;
    private int englishResultsFlag = 0;
    private Context context;
//    //    private static final String ACCESS_TOKEN = "37176714256e377a408a2478728e7c06fc0586dcff56aed28d6b1dd1e3598f3af7e7a9268bb1b3ffd2c6d";
//    private static final String[] ACCESS_TOKENS = new String[]{
////            // my
////            "37176714256e377a408a2478728e7c06fc0586dcff56aed28d6b1dd1e3598f3af7e7a9268bb1b3ffd2c6d",
////            // lena
////            "7fcb578cc3128dc96579db09e6f088347c2512ac1fd1b5c32931cb6054318203344c06a9b4d629dce0caf",
////            // John wayne  user_id=220909080
//            "3caa825214103474e13d94a0eabce2c354dba6a7400e22179cf5bf232146dd7c245862a326a6dba8d6ace",
////
////            // Pedoha user_id=3589341 locked
////            // "ffda8c939a0aac0783e262fd98816ab47f0c3910d566ded21a9dd1b4f3a39c953c55c27bff8dabc976697",
////
////            // Mykyta  user_id=5422875
//            "f2e4309ccffc48a4a6a3941aa808ce55ba0499542a3f5e155182fe7ecd029467540e5437483c7dba11703",
////
////            // Dimon makeev
//            "1fef8b7043f37a2aa3a24f3e4a3afb9a304920951a30287f1e8362e65f1a6e8dc00c8a7923510dc51e0e2",
////
////            //mama
//            "b370bda148ff9009fef72919c6549e093c5e4b9c60f641ae8dcbe5bbea12e4a679b176097fc31304bb905"
//
//    };


// Lena
    //    "https://oauth.vk.com/authorize?client_id=3824524&redirect_uri=http://api.vk.com/blank.html&display=page&v=5.0&scope=audio,video,offline&response_type=token"
//    "https://oauth.vk.com/authorize?client_id=3010909&redirect_uri=http://api.vk.com/blank.html&display=page&v=5.0&scope=audio,video,offline&response_type=token"
    // my
// "https://oauth.vk.com/authorize?client_id=3746605&redirect_uri=http://api.vk.com/blank.html&display=page&v=5.0&scope=audio,video,offline&response_type=token";
//	https://api.vk.com/method/audio.search.json?q=AC/DC%20-%20Highway%20to%20Hell&access_token=37176714256e377a408a2478728e7c06fc0586dcff56aed28d6b1dd1e3598f3af7e7a9268bb1b3ffd2c6d

    public ApiHelper(Context context, ResponseReceiver receiver) {
        super(context, receiver);
        random = new Random();
        this.context = context;
    }

//    public void getMyAudio(Context context) {
//        String ownerId = StorageUtils.getUserId(context);
//        String url = "https://api.vk.com/method/audio.get.json?&count=1000&offset=0&owner_id=" + ownerId + "&access_token=" + getToken();
//        Request.Builder builder = new Request.Builder(url, RequestMethod.GET).setResponseHandler(PopularResponceHandler.class);
//        sendRequest(builder.create(), AUDIO_TOKEN);
//    }

    public void getNewToken() {
        String url = "https://dl.dropboxusercontent.com/u/20432838/vk/token.json";
        Request.Builder builder = new Request.Builder(url, RequestMethod.GET).setResponseHandler(NewTokenReposeHandler.class);
        sendRequest(builder.create(), AUDIO_TOKEN);
    }


    public void getSongsList(int offset, String query, int token) {
        String URL = null;
        switch (token) {
            case POPULAR_TOKEN:
                setEnglishFitterByCountryISO(context);
                URL = "https://api.vk.com/method/audio.getPopular.json?&count=%s&offset=%s&only_eng=%s&access_token=" + getToken();
                URL = String.format(URL, COUNT, offset, englishResultsFlag);
                break;
            case AUDIO_TOKEN:
                URL = "https://api.vk.com/method/audio.search.json?&q=%s&count=%s&offset=%s&access_token=" + getToken();
                try {
                    URL = String.format(URL, URLEncoder.encode(query, HTTP.UTF_8), COUNT, offset);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
        }

        Request.Builder builder = new Request.Builder(URL, RequestMethod.GET).setResponseHandler(PopularResponceHandler.class);
        sendRequest(builder.create(), AUDIO_TOKEN);

    }

    public void getRequestCaptcha(String url) {
        Request.Builder builder = new Request.Builder(url, RequestMethod.GET).setResponseHandler(PopularResponceHandler.class);
        sendRequest(builder.create());
    }

    public void getVideoList(int offset, String query) {
        String URL = "https://api.vk.com/method/video.search.json?&q=%s&count=%s&offset=%s&adult=1&filters=mp4&access_token=" + getToken();
        try {
            URL = String.format(URL, URLEncoder.encode(query, HTTP.UTF_8), COUNT, offset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Request.Builder builder = new Request.Builder(URL, RequestMethod.GET).setResponseHandler(VideoResponseHandler.class);
        sendRequest(builder.create());
    }

    public void getVideoDirectFiles(String url) {
        Request.Builder builder = new Request.Builder(url, RequestMethod.GET).setResponseHandler(DirectVideoFilesReposeHandler.class);
        sendRequest(builder.create(), VIDEO_TOKEN);
    }

    private String getToken() {
//        int position = getRandomWithExclusion(random, ACCESS_TOKENS.length, getExcludedPosition());
//        return ACCESS_TOKENS[position];
        return StorageUtils.restoreToken(context);
    }

    private void setEnglishFitterByCountryISO(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = tm.getNetworkCountryIso();

        ArrayList<String> countyIsoList = new ArrayList<String>();
        countyIsoList.add("ua");
        countyIsoList.add("ru");
        countyIsoList.add("by");
        countyIsoList.add("kz");
        countyIsoList.add("ee");
        countyIsoList.add("lv");

        if (countryCode != null && !TextUtils.isEmpty(countryCode) && !countyIsoList.contains(countryCode)) {
            englishResultsFlag = 1;
        }
    }

//    private ArrayList<Integer> getExcludedPosition() {
//        ArrayList<String> list = new ArrayList<String>();
//        Cursor cursor = context.getContentResolver().query(BlockedTokensObject.CONTENT_URI, BlockedTokensQuery.PROJECTION, null, null, null);
//        if (cursor != null && cursor.moveToFirst()) {
//            do {
//                list.add(cursor.getString(cursor.getColumnIndex(BlockedTokensColumns.TOKEN_VALUE.getName())));
//            }
//            while (cursor.moveToNext());
//        }
//
//        if (cursor != null)
//            cursor.close();
//
////        int[] excludedPosition = new int[list.size()];
//        ArrayList<Integer> excludedPosition = new ArrayList<Integer>();
//
//        for (int j = 0; j < list.size(); j++) {
//            for (int i = 0; i < ACCESS_TOKENS.length; i++) {
//                if (ACCESS_TOKENS[i].equals(list.get(j))) {
////                    excludedPosition[j] = i;
//                    excludedPosition.add(i);
//                    break;
//                }
//            }
//
//        }
//
//        return excludedPosition;
//    }
//
//    public int getRandomWithExclusion(Random rnd, int end, ArrayList<Integer> exclude) {
//        int rand;
//        do {
//            rand = rnd.nextInt(end);
//        } while (exclude.contains(rand));
//        return rand;
//    }

}
