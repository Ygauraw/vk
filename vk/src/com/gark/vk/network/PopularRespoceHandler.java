package com.gark.vk.network;

import org.apache.http.HttpResponse;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.the111min.android.api.request.Request;
import com.the111min.android.api.response.ResponseHandler;
import com.the111min.android.api.util.HttpUtils;

public class PopularRespoceHandler extends ResponseHandler {

	@Override
	public boolean handleResponse(Context context, HttpResponse response, Request request, Bundle result) throws Exception {
		final String text = HttpUtils.readHttpResponse(response);

		Log.e("responce", text);

		return false;
	}

}
