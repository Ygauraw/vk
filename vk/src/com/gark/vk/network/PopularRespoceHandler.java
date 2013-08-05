package com.gark.vk.network;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;

import com.gark.vk.db.MusicColumns;
import com.gark.vk.db.VKDBSchema;
import com.gark.vk.model.MusicObject;
import com.gark.vk.ui.DialogCaptchaFragment;
import com.gark.vk.ui.DialogVideoTypeFragment;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.the111min.android.api.request.Request;
import com.the111min.android.api.response.ResponseHandler;
import com.the111min.android.api.util.HttpUtils;

import java.util.ArrayList;

public class PopularRespoceHandler extends ResponseHandler {
    private static final String RESPONSE = "response";

    public static final String COUNT = "count";

    private static final String AID = "aid";
    private static final String OWNER_ID = "owner_id";
    private static final String ARTIST = "artist";
    private static final String TITLE = "title";
    private static final String DURATION = "duration";
    private static final String URL = "url";
    private static final String LYRICS_ID = "lyrics_id";
    private static final String GENRE = "genre";
    private static final String ALBUM = "album";

    public static final String CAPTCHA = "captcha";


    @Override
    public boolean handleResponse(Context context, HttpResponse response, Request request, Bundle result) throws Exception {
        String text = HttpUtils.readHttpResponse(response);
//        text = "{\"error\":{\"error_code\":14,\"error_msg\":\"Captcha needed\",\"request_params\":[{\"key\":\"oauth\",\"value\":\"1\"},{\"key\":\"method\",\"value\":\"audio.getPopular.json\"},{\"key\":\"\",\"value\":\"\"},{\"key\":\"count\",\"value\":\"30\"},{\"key\":\"offset\",\"value\":\"0\"},{\"key\":\"only_eng\",\"value\":\"0\"},{\"key\":\"access_token\",\"value\":\"03328309b844c9cc0b6ad716238ac8d583562d0dccc56ff2fcd755913bf021c4ca5d64b164ada2869ada1\"}],\"captcha_sid\":\"450495329930\",\"captcha_img\":\"http:\\/\\/api.vk.com\\/captcha.php?sid=450495329930\",\"need_validation\":1}}";

        Tracker myTracker = EasyTracker.getTracker();


        if (checkCaptcha(text, result, myTracker, context)) {
            return true;
        }

        try {


            final ArrayList<ContentProviderOperation> insertOperations = new ArrayList<ContentProviderOperation>();

            final JSONObject jsonObj = new JSONObject(text);
            JSONArray jsonArray = jsonObj.getJSONArray(RESPONSE);

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject item;
                try {
                    item = jsonArray.getJSONObject(i);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }


                String aid = null;
                if (!item.isNull(AID)) {
                    aid = item.getString(AID);
                }

                String ownerId = null;
                if (!item.isNull(OWNER_ID)) {
                    ownerId = item.getString(OWNER_ID);
                }

                String artist = null;
                if (!item.isNull(ARTIST)) {
                    artist = item.getString(ARTIST);
                }

                String title = null;
                if (!item.isNull(TITLE)) {
                    title = item.getString(TITLE);
                }


                String duration = null;
                if (!item.isNull(DURATION)) {
                    duration = item.getString(DURATION);
                }

                String url = null;
                if (!item.isNull(URL)) {
                    url = item.getString(URL);
                }

                String lyrics_id = null;
                if (!item.isNull(LYRICS_ID)) {
                    lyrics_id = item.getString(LYRICS_ID);
                }

                String genre = null;
                if (!item.isNull(GENRE)) {
                    genre = item.getString(GENRE);
                }

                String album = null;
                if (!item.isNull(ALBUM)) {
                    album = item.getString(ALBUM);
                }

                insertOperations.add(ContentProviderOperation.newInsert(MusicObject.CONTENT_URI)
                        .withValue(MusicColumns.AID.getName(), aid)
                        .withValue(MusicColumns.ARTIST.getName(), artist)
                        .withValue(MusicColumns.TITLE.getName(), title)
                        .withValue(MusicColumns.DURATION.getName(), duration)
                        .withValue(MusicColumns.URL.getName(), url)
                        .withValue(MusicColumns.LYRICS_ID.getName(), lyrics_id)
                        .withValue(MusicColumns.GENRE.getName(), genre)
                        .withValue(MusicColumns.IS_ACTIVE.getName(), 0)
                        .withValue(MusicColumns.ALBUM.getName(), album).build());
            }

            context.getContentResolver().applyBatch(VKDBSchema.CONTENT_AUTHORITY, insertOperations);

            result.putInt(COUNT, insertOperations.size());

        } catch (Exception e) {
            e.printStackTrace();

            StringBuffer sb = new StringBuffer();
            if (e != null && e.getMessage() != null) {
                sb.append(e.getMessage());
            }

            if (e != null && e.getLocalizedMessage() != null) {
                sb.append(e.getLocalizedMessage());
            }

            myTracker.sendException(sb.toString() + "\n" + text, false);
        }

        return true;
    }


    public static String ERROR = "error";
    public static String ERROR_CODE = "error_code";
    public static String CAPTCHA_CODE = "14";


    private boolean checkCaptcha(String response, Bundle bundle, Tracker myTracker, Context context) throws Exception {
        boolean result = false;
        final JSONObject jsonObj;
        try {
            jsonObj = new JSONObject(response);
            if (!jsonObj.isNull(ERROR)) {
                JSONObject jSubObject = jsonObj.getJSONObject(ERROR);
                if (!jSubObject.isNull(ERROR_CODE) && CAPTCHA_CODE.equals(jSubObject.getString(ERROR_CODE))) {
                    bundle.putString(CAPTCHA, response);
                    result = true;

                    try {
                        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                        if (manager != null && manager.getNetworkCountryIso() != null) {
                            myTracker.sendException(manager.getNetworkCountryIso() + " network country ISO", false);
                        }
                    } catch (Exception e) {

                    }


                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }


}
