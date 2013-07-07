package com.gark.vk;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.gark.vk.network.ApiHelper;
import com.the111min.android.api.response.ResponseReceiver;

public class MainActivity extends FragmentActivity {

	// String uri =
	// "http://api.vk.com/oauth/authorize?client_id=3746605&redirect_uri=http://api.vk.com/blank.html&scope=nohttps&display=page&response_type=token";
	// String uri =
	// "https://oauth.vk.com/authorize?client_id=3746605&redirect_uri=http://api.vk.com/blank.html&scope=nohttps&display=page&v=5.0&response_type=token";

	// String get =
	// "http://oauth.vk.com/authorize?client_id=3746605&scope=audio,video,friends,offline,groups&redirect_uri=http://oauth.vk.com/blank.html&display=wap&response_type=token";
	String get = "https://api.vk.com/method/audio.search.json?q=AC/DC%20-%20Highway%20to%20Hell&access_token=37176714256e377a408a2478728e7c06fc0586dcff56aed28d6b1dd1e3598f3af7e7a9268bb1b3ffd2c6d";

	// String uri =
	// "https://oauth.vk.com/authorize?client_id=3746605&response_type=token";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		new ApiHelper(this, receiver).getPopular();

		// new ApiHelp

	}

	final ResponseReceiver receiver = new ResponseReceiver() {

		@Override
		public void onRequestSuccess(int token, Bundle result) {

		}

		@Override
		public void onRequestFailure(int token, Bundle result) {

		}

		@Override
		public void onError(int token, Exception e) {

		}
	};

}
