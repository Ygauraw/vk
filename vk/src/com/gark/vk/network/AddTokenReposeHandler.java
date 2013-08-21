package com.gark.vk.network;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.gark.vk.utils.StorageUtils;
import com.the111min.android.api.request.Request;
import com.the111min.android.api.response.ResponseHandler;
import com.the111min.android.api.util.HttpUtils;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;

public class AddTokenReposeHandler extends ResponseHandler {
    public static final String TOKEN_LIST = "tokenList";


    @Override
    public boolean handleResponse(Context context, HttpResponse response, Request request, Bundle result) throws Exception {
        final String text = HttpUtils.readHttpResponse(response);
        return false;
    }


}
