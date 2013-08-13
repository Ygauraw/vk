package com.gark.vk.network;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import com.gark.vk.db.MusicColumns;
import com.gark.vk.db.VKDBSchema;
import com.gark.vk.db.VideoColumns;
import com.gark.vk.model.MusicObject;
import com.gark.vk.model.VideoObject;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.the111min.android.api.request.Request;
import com.the111min.android.api.response.ResponseHandler;
import com.the111min.android.api.util.HttpUtils;
import com.the111min.android.api.util.ToManyRequestException;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class VideoRespoceHandler extends ResponseHandler {
    private static final String RESPONSE = "response";

    public static final String COUNT = "count";

    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String OWNER_ID = "owner_id";
    private static final String DURATION = "duration";
    private static final String DATE = "date";
    private static final String THUMB = "thumb";
    private static final String IMAGE_MEDIUM = "image_medium";
    private static final String PLAYER = "player";


    @Override
    public boolean handleResponse(Context context, HttpResponse response, Request request, Bundle result) throws Exception {
        String text = HttpUtils.readHttpResponse(response);

//        text = "{\"error\":{\"error_code\":14,\"error_msg\":\"Captcha needed\",\"request_params\":[{\"key\":\"oauth\",\"value\":\"1\"},{\"key\":\"method\",\"value\":\"audio.getPopular.json\"},{\"key\":\"\",\"value\":\"\"},{\"key\":\"count\",\"value\":\"30\"},{\"key\":\"offset\",\"value\":\"0\"},{\"key\":\"only_eng\",\"value\":\"0\"},{\"key\":\"access_token\",\"value\":\"03328309b844c9cc0b6ad716238ac8d583562d0dccc56ff2fcd755913bf021c4ca5d64b164ada2869ada1\"}],\"captcha_sid\":\"450495329930\",\"captcha_img\":\"http:\\/\\/api.vk.com\\/captcha.php?sid=450495329930\",\"need_validation\":1}}";
//        text = "{\"error\":{\"error_code\":6,\"error_msg\":\"Too many requests per second\",\"request_params\":[{\"key\":\"oauth\",\"value\":\"1\"},{\"key\":\"method\",\"value\":\"audio.search.json\"},{\"key\":\"\",\"value\":\"\"},{\"key\":\"q\",\"value\":\"south africa\"},{\"key\":\"count\",\"value\":\"20\"},{\"key\":\"offset\",\"value\":\"100\"},{\"key\":\"access_token\",\"value\":\"a10b720def064f31ffd3e06e8966aad4faac465f0a4c6be4c8576e3354c008bac6cf7c38d8fdf8a530b9c\"}]}}";

        EasyTracker.getInstance().setContext(context);
        Tracker myTracker = EasyTracker.getTracker();
        if (checkCaptcha(text, result, myTracker, context)) {
            return true;
        }

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

            String id = null;
            if (!item.isNull(ID)) {
                id = item.getString(ID);
            }

            String ownerId = null;
            if (!item.isNull(OWNER_ID)) {
                ownerId = item.getString(OWNER_ID);
            }

            String description = null;
            if (!item.isNull(DESCRIPTION)) {
                description = item.getString(DESCRIPTION);
            }

            String title = null;
            if (!item.isNull(TITLE)) {
                title = item.getString(TITLE);
            }


            String duration = null;
            if (!item.isNull(DURATION)) {
                duration = item.getString(DURATION);
            }

            String date = null;
            if (!item.isNull(DATE)) {
                date = item.getString(DATE);
            }

            String thumb = null;
            if (!item.isNull(THUMB)) {
                thumb = item.getString(THUMB);
            }

            String image_medium = null;
            if (!item.isNull(IMAGE_MEDIUM)) {
                image_medium = item.getString(IMAGE_MEDIUM);
            }

            String player = null;
            if (!item.isNull(PLAYER)) {
                player = item.getString(PLAYER);
            }


            insertOperations.add(ContentProviderOperation.newInsert(VideoObject.CONTENT_URI)
                    .withValue(VideoColumns.ID.getName(), id)
                    .withValue(VideoColumns.OWNER_ID.getName(), ownerId)
                    .withValue(VideoColumns.DESCRIPTION.getName(), description)
                    .withValue(VideoColumns.TITLE.getName(), title)
                    .withValue(VideoColumns.DURATION.getName(), duration)
                    .withValue(VideoColumns.DATE.getName(), date)
                    .withValue(VideoColumns.THUMB.getName(), thumb)
                    .withValue(VideoColumns.IMAGE_MEDIUM.getName(), image_medium)
                    .withValue(VideoColumns.PLAYER.getName(), player)
//                    .withValue(VideoColumns.MP4_240.getName(), mp4_240)
//                    .withValue(VideoColumns.MP4_360.getName(), mp4_360)
//                    .withValue(VideoColumns.MP4_480.getName(), mp4_480)
//                    .withValue(VideoColumns.MP4_720.getName(), mp4_720)
//                    .withValue(VideoColumns.EXTERNAL.getName(), external)
                    .build());
        }

        context.getContentResolver().applyBatch(VKDBSchema.CONTENT_AUTHORITY, insertOperations);

        result.putInt(COUNT, insertOperations.size());

        return true;
    }

    public static String ERROR = "error";
    public static String ERROR_CODE = "error_code";
    public static String CAPTCHA_CODE = "14";
    public static final String CAPTCHA = "captcha";
    public static String TO_MANY_REQUEST = "6";


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
                } else if (!jSubObject.isNull(ERROR_CODE) && TO_MANY_REQUEST.equals(jSubObject.getString(ERROR_CODE))) {
                    throw new ToManyRequestException();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

}
