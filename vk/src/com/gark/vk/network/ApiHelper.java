package com.gark.vk.network;

import android.content.Context;

import com.the111min.android.api.BaseApiHelper;
import com.the111min.android.api.request.Request;
import com.the111min.android.api.request.Request.RequestMethod;
import com.the111min.android.api.response.ResponseReceiver;

public class ApiHelper extends BaseApiHelper {

	private static final String ACCESS_TOKEN = "37176714256e377a408a2478728e7c06fc0586dcff56aed28d6b1dd1e3598f3af7e7a9268bb1b3ffd2c6d";
	
//	https://api.vk.com/method/audio.search.json?q=AC/DC%20-%20Highway%20to%20Hell&access_token=37176714256e377a408a2478728e7c06fc0586dcff56aed28d6b1dd1e3598f3af7e7a9268bb1b3ffd2c6d

	public ApiHelper(Context context, ResponseReceiver receiver) {
		super(context, receiver);
	}

	public void getPopular() {
		String URL = "https://api.vk.com/method/audio.getPopular.json?&access_token=" + ACCESS_TOKEN;
		Request.Builder builder = new Request.Builder(URL, RequestMethod.GET).setResponseHandler(PopularRespoceHandler.class);
//		builder.addHeaderParam(HTTP.CONTENT_TYPE, APPLICATION_JSON);
//		builder.addHeaderParam(ACCEPT, APPLICATION_JSON);
//		builder.addHeaderParam(AUTHORIZATION, LOGIN_PASS);
		sendRequest(builder.create());
	}

}
