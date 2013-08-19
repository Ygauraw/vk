package com.gark.vk.network;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import com.gark.vk.db.MusicColumns;
import com.gark.vk.db.VKDBSchema;
import com.gark.vk.model.MusicObject;
import com.gark.vk.utils.StorageUtils;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.the111min.android.api.request.Request;
import com.the111min.android.api.response.ResponseHandler;
import com.the111min.android.api.util.HttpUtils;
import com.the111min.android.api.util.ToManyRequestException;

import java.util.ArrayList;

public class PopularResponceHandler extends ResponseHandler {
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
//        text = "{\"error\":{\"error_code\":6,\"error_msg\":\"Too many requests per second\",\"request_params\":[{\"key\":\"oauth\",\"value\":\"1\"},{\"key\":\"method\",\"value\":\"audio.search.json\"},{\"key\":\"\",\"value\":\"\"},{\"key\":\"q\",\"value\":\"south africa\"},{\"key\":\"count\",\"value\":\"20\"},{\"key\":\"offset\",\"value\":\"100\"},{\"key\":\"access_token\",\"value\":\"a10b720def064f31ffd3e06e8966aad4faac465f0a4c6be4c8576e3354c008bac6cf7c38d8fdf8a530b9c\"}]}}";
//         text = "{\"error\":{\"error_code\":9,\"error_msg\":\"Flood control: too much captcha requests\",\"request_params\":[{\"key\":\"oauth\",\"value\":\"1\"},{\"key\":\"method\",\"value\":\"audio.search.json\"},{\"key\":\"\",\"value\":\"\"},{\"key\":\"q\",\"value\":\"аркадий лайкинshakira\"},{\"key\":\"count\",\"value\":\"20\"},{\"key\":\"offset\",\"value\":\"0\"},{\"key\":\"access_token\",\"value\":\"d868d0c17e551f0d61f01b0e2cdcbf31908531227dd036e43cb75ce33e66ce3a2eade69a53437dec6272a\"}]}}";
        //text = "{\"error\":{\"error_code\":5,\"error_msg\":\"User authorization failed: invalid access_token.\",\"request_params\":[{\"key\":\"oauth\",\"value\":\"1\"},{\"key\":\"method\",\"value\":\"audio.getPopular.json\"},{\"key\":\"\",\"value\":\"\"},{\"key\":\"count\",\"value\":\"40\"},{\"key\":\"offset\",\"value\":\"0\"},{\"key\":\"only_eng\",\"value\":\"0\"},{\"key\":\"access_token\",\"value\":\"ffda8c939a0aac0783e262fd98816ab47f0c3910d566ded21a9dd1b4f3a39c953c55c27bff8dabc976697\"}]}}\n";

        EasyTracker.getInstance().setContext(context);
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
            sb.append(" e.getMessage()=");
            if (e != null && e.getMessage() != null) {
                sb.append(e.getMessage());
            }

            sb.append(" e.getLocalizedMessage()=");
            if (e != null && e.getLocalizedMessage() != null) {
                sb.append(e.getLocalizedMessage());
            }

            sb.append(" e.getClass().getName()=");
            try {
                sb.append(e.getClass().getName());
            } catch (Exception ex) {
            }

            myTracker.sendException(sb.toString() + "\n" + text, false);

            myTracker.sendEvent("Popular Response handler " + StorageUtils.getAppVersion(context), sb.toString() + "\n" + text, "tratat", 1l);
        }

        return true;
    }


    public static String ERROR = "error";
    public static String ERROR_CODE = "error_code";
    public static String CAPTCHA_CODE = "14";
    public static String TO_MANY_REQUEST = "6";
    public static String FLOOD_CONTROL = "9";
    public static String AUTHORIZATION_ERROR = "5";


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

//                    try {
//                        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//                        if (manager != null && manager.getNetworkCountryIso() != null) {
//                            myTracker.sendException(manager.getNetworkCountryIso() + " network country ISO", false);
//                        }
//                    } catch (Exception e) {
//
//                    }

                    myTracker.sendEvent("Captcha ", response + StorageUtils.getAppVersion(context), response, 3555l);

                } else if (!jSubObject.isNull(ERROR_CODE) && TO_MANY_REQUEST.equals(jSubObject.getString(ERROR_CODE))) {
                    throw new ToManyRequestException();
                } else if (!jSubObject.isNull(ERROR_CODE) && FLOOD_CONTROL.equals(jSubObject.getString(ERROR_CODE))) {

                    try {
                        String badToken = getBadToken(jSubObject);
//                        ContentValues contentValues = new ContentValues();
//                        contentValues.put(BlockedTokensColumns.TOKEN_VALUE.getName(), badToken);
//                        contentValues.put(BlockedTokensColumns.BLOCKED_TIME.getName(), Calendar.getInstance().getTimeInMillis());
//                        context.getContentResolver().insert(BlockedTokensObject.CONTENT_URI, contentValues);

                        myTracker.sendEvent("Flood control" + StorageUtils.getAppVersion(context), badToken, response, 3l);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    StorageUtils.eraseUserID(context);
                    StorageUtils.updateToken(context);

                } else if (!jSubObject.isNull(ERROR_CODE) && AUTHORIZATION_ERROR.equals(jSubObject.getString(ERROR_CODE))) {
                    try {
                        String badToken = getBadToken(jSubObject);
                        myTracker.sendEvent("Authorization error" + StorageUtils.getAppVersion(context), badToken, response, 3254l);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    StorageUtils.eraseUserID(context);
                    StorageUtils.updateToken(context);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getBadToken(JSONObject jImg) throws JSONException {

        final String KEY = "key";
        final String ACCESS_TOKEN = "access_token";
        final String VALUE = "value";
        final String REQUEST_PARAMS = "request_params";
        String token = null;

        if (!jImg.isNull(REQUEST_PARAMS)) {

            JSONArray jsonArray = jImg.getJSONArray(REQUEST_PARAMS);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject j = (JSONObject) jsonArray.get(i);
                String key = j.getString(KEY);
                String value = j.getString(VALUE);


                if (ACCESS_TOKEN.equals(key)) {
                    token = value;
                    break;
                }
            }
        }

        return token;

    }


}
