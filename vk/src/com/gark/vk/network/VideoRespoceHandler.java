package com.gark.vk.network;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.os.Bundle;

import com.gark.vk.db.MusicColumns;
import com.gark.vk.db.VKDBSchema;
import com.gark.vk.db.VideoColumns;
import com.gark.vk.model.MusicObject;
import com.gark.vk.model.VideoObject;
import com.the111min.android.api.request.Request;
import com.the111min.android.api.response.ResponseHandler;
import com.the111min.android.api.util.HttpUtils;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
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
        final String text = HttpUtils.readHttpResponse(response);

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
                    .build());
        }

        context.getContentResolver().applyBatch(VKDBSchema.CONTENT_AUTHORITY, insertOperations);

        result.putInt(COUNT, insertOperations.size());

        return true;
    }

}
