package com.gark.vknew.network;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.gark.vknew.model.VideoTypes;
import com.the111min.android.api.request.Request;
import com.the111min.android.api.response.ResponseHandler;
import com.the111min.android.api.util.HttpUtils;

import org.apache.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;

public class DirectVideoFilesReposeHandler extends ResponseHandler {
    public static final String VIDEO_TYPES = "video_types";

    private static final String URL240 = "url240";
    private static final String URL360 = "url360";
    private static final String URL480 = "url480";
    private static final String URL720 = "url720";


    @Override
    public boolean handleResponse(Context context, HttpResponse response, Request request, Bundle result) throws Exception {
        final String text = HttpUtils.readHttpResponse(response);


        Map<String, String> map = getQueryMap(text);

        VideoTypes videoTypes = new VideoTypes();
        videoTypes.setVkVideo240(map.get(URL240));
        videoTypes.setVkVideo360(map.get(URL360));
        videoTypes.setVkVideo480(map.get(URL480));
        videoTypes.setVkVideo720(map.get(URL720));

        result.putParcelable(VIDEO_TYPES, videoTypes);

        return true;
    }


    private static Map<String, String> getQueryMap(String query) {
        String[] params = query.split("&amp;");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {
            try {
                String name = param.split("=")[0];//.replace("&", "").replace("amp;", "");
                String value = param.split("=")[1];//.replace("&", "").replace("amp;", "");
                map.put(name.trim(), value.trim());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    private String getYTubeID(String text) {

        String yTubeID = null;
        try {

            int startPosition = text.indexOf("src=\"http://www.youtube.com");
            if (startPosition != -1) {
                String yTubeURL = text.substring(startPosition, startPosition + 150);
                Uri u = Uri.parse(yTubeURL.split(" ")[0]);
                yTubeID = u.getLastPathSegment();
            }
        } catch (Exception e) {
        }

        return yTubeID;
    }


    private String getVimoID(String text) {

        String yTubeID = null;
        try {

            int startPosition = text.indexOf("src=\"http://player.vimeo.com");
            if (startPosition != -1) {
                String yTubeURL = text.substring(startPosition, startPosition + 150);
                Uri u = Uri.parse(yTubeURL.split(" ")[0]);
                yTubeID = u.getLastPathSegment();
            }
        } catch (Exception e) {
        }

        return yTubeID;
    }

}
