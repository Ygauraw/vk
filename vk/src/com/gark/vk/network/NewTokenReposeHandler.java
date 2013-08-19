package com.gark.vk.network;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.gark.vk.utils.Log;
import com.gark.vk.utils.StorageUtils;
import com.the111min.android.api.request.Request;
import com.the111min.android.api.response.ResponseHandler;
import com.the111min.android.api.util.HttpUtils;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;

public class NewTokenReposeHandler extends ResponseHandler {
    public static final String TOKEN_LIST = "tokenList";


    @Override
    public boolean handleResponse(Context context, HttpResponse response, Request request, Bundle result) throws Exception {
        final String text = HttpUtils.readHttpResponse(response);


        JSONObject jsonObject = new JSONObject(text);
        JSONArray jsonArray = jsonObject.getJSONArray(TOKEN_LIST);

//        String[] ACCESS_TOKENS = new String[jsonArray.length()];
//
//        for (int i = 0; i < jsonArray.length(); i++) {
//            ACCESS_TOKENS[i] = jsonArray.getString(i);
//        }
        String resultToken;
        final Random random = new Random();
        resultToken = jsonArray.getString(random.nextInt(jsonArray.length()));

//        String resultToken = ACCESS_TOKENS[random.nextInt(ACCESS_TOKENS.length)];
        if (resultToken != null && !TextUtils.isEmpty(resultToken)) {
            StorageUtils.saveToken(context, resultToken);
        }

        return false;
    }


}
