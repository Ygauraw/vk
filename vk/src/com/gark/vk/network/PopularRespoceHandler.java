package com.gark.vk.network;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.os.Bundle;

import com.gark.vk.db.MusicColumns;
import com.gark.vk.db.VKDBSchema;
import com.gark.vk.model.MusicObject;
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

        return true;
    }

}
