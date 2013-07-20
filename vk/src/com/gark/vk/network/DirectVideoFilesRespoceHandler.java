package com.gark.vk.network;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.os.Bundle;

import com.gark.vk.db.VKDBSchema;
import com.gark.vk.db.VideoColumns;
import com.gark.vk.model.VideoObject;
import com.gark.vk.utils.Log;
import com.the111min.android.api.request.Request;
import com.the111min.android.api.response.ResponseHandler;
import com.the111min.android.api.util.HttpUtils;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class DirectVideoFilesRespoceHandler extends ResponseHandler {
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
    private static final String FILES = "files";

    private static final String MP4_240 = "mp4_240";
    private static final String MP4_360 = "mp4_360";
    private static final String MP4_480 = "mp4_480";
    private static final String MP4_720 = "mp4_720";
    private static final String EXTERNAL = "external";


    @Override
    public boolean handleResponse(Context context, HttpResponse response, Request request, Bundle result) throws Exception {
        final String text = HttpUtils.readHttpResponse(response);

       int startPosition = text.indexOf("url240");
        Log.e("" + startPosition);

        Log.e("" + text.substring(startPosition, startPosition + 40));

        Log.e("" + startPosition);

        return true;
    }

}
