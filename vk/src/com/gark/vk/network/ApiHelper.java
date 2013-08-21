package com.gark.vk.network;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.gark.vk.utils.StorageUtils;
import com.the111min.android.api.BaseApiHelper;
import com.the111min.android.api.request.Request;
import com.the111min.android.api.request.Request.RequestMethod;
import com.the111min.android.api.response.ResponseReceiver;

import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ApiHelper extends BaseApiHelper {


    public static final int POPULAR_TOKEN = 1;
    public static final int AUDIO_TOKEN = 2;
    public static final int VIDEO_TOKEN = 3;
    public static final int VK_MUSIC_TOKEN = 4;
    public static final int COUNT = 40;
    private int englishResultsFlag = 0;
    private Context context;

    public ApiHelper(Context context, ResponseReceiver receiver) {
        super(context, receiver);
        this.context = context;
    }

    public void getNewToken() {
        String url = "http://modestfishapp.appspot.com/vkserver";
        Request.Builder builder = new Request.Builder(url, RequestMethod.GET).setResponseHandler(NewTokenReposeHandler.class);
        sendRequest(builder.create());
    }

    public void getNewTokenFromBackUp() {
        String url = "https://dl.dropboxusercontent.com/u/20432838/vk/token.json";
        Request.Builder builder = new Request.Builder(url, RequestMethod.GET).setResponseHandler(NewTokenFromBackUpReposeHandler.class);
        sendRequest(builder.create());
    }


    public void addToken(String userId, String token) {
        String url = "http://modestfishapp.appspot.com/vkserver?user_id=%s&token=%s";
        url = String.format(url, userId, token);
        Request.Builder builder = new Request.Builder(url, RequestMethod.POST).setResponseHandler(AddTokenReposeHandler.class);

        sendRequest(builder.create());
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
            case VK_MUSIC_TOKEN:
                String ownerId = StorageUtils.getUserId(context);
                URL = "https://api.vk.com/method/audio.get.json?&count=%s&offset=%s&owner_id=%s&access_token=" + getToken();
                URL = String.format(URL, COUNT, offset, ownerId);
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
