package com.gark.vk.network;

import android.content.Context;

import com.the111min.android.api.BaseApiHelper;
import com.the111min.android.api.request.Request;
import com.the111min.android.api.request.Request.RequestMethod;
import com.the111min.android.api.response.ResponseReceiver;

import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

public class ApiHelper extends BaseApiHelper {


    public static final int POPULAR_TOKEN = 1;
    public static final int AUDIO_TOKEN = 2;
    public static final int VIDEO_TOKEN = 3;
    public static final int COUNT = 30;
    private Random random;
    //    private static final String ACCESS_TOKEN = "37176714256e377a408a2478728e7c06fc0586dcff56aed28d6b1dd1e3598f3af7e7a9268bb1b3ffd2c6d";
    private static final String[] ACCESS_TOKENS = new String[]{
            "37176714256e377a408a2478728e7c06fc0586dcff56aed28d6b1dd1e3598f3af7e7a9268bb1b3ffd2c6d", "74e37c7cae5fe2f9d48f5533686cd0979f4e44fd070979ae9dd5d26b42b591161fdefd95396850d0ffde9",
            "c82cb8591d7cd186916028bb29b3faf289999328ac5482d2a998747e3f116cf16201c3d8be6e9c328c3e5", "4057a57148daca8c3d5fc6e4e10bf37b42ea67bcb0bc059c6fbc74766c35b027d96e95c465d21792932ed",
            "a21756528ec71dec8b016b7975b470c708b54535d1d068f130641790795f2a05243f478a6898c82481e91", "d868d0c17e551f0d61f01b0e2cdcbf31908531227dd036e43cb75ce33e66ce3a2eade69a53437dec6272a",
            "2b51f6a4e797603ddbe85cdef1de20f872ffe229fc9de87478efe037148793689c320d3b4a5226b0bb220", "03328309b844c9cc0b6ad716238ac8d583562d0dccc56ff2fcd755913bf021c4ca5d64b164ada2869ada1",
            "a10b720def064f31ffd3e06e8966aad4faac465f0a4c6be4c8576e3354c008bac6cf7c38d8fdf8a530b9c", "6224917185ff5ae27ce3da74fbceaf7768ebb9df00d46de6aa5ac7f6e7296030d63ce3830f05850429b9b"

    };

// "https://oauth.vk.com/authorize?client_id=3746605&redirect_uri=http://api.vk.com/blank.html&display=page&v=5.0&scope=audio,video,offline&response_type=token";
//	https://api.vk.com/method/audio.search.json?q=AC/DC%20-%20Highway%20to%20Hell&access_token=37176714256e377a408a2478728e7c06fc0586dcff56aed28d6b1dd1e3598f3af7e7a9268bb1b3ffd2c6d

    public ApiHelper(Context context, ResponseReceiver receiver) {
        super(context, receiver);
        random = new Random();

    }



    public void getSongsList(int offset, String query, int token) {
        String URL = null;
        switch (token) {
            case POPULAR_TOKEN:
                URL = "https://api.vk.com/method/audio.getPopular.json?&count=%s&offset=%s&access_token=" + getToken();
                URL = String.format(URL, COUNT, offset);
                break;
            case AUDIO_TOKEN:
                URL = "https://api.vk.com/method/audio.search.json?&q=%s&count=%s&offset=%s&access_token=" + getToken();
                try {
                    URL = String.format(URL, URLEncoder.encode(query, HTTP.UTF_8), COUNT, offset);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
        }

        Request.Builder builder = new Request.Builder(URL, RequestMethod.GET).setResponseHandler(PopularRespoceHandler.class);
        sendRequest(builder.create());
    }


    public void getVideoList(int offset, String query) {
        String URL = "https://api.vk.com/method/video.search.json?&q=%s&count=%s&offset=%s&adult=1&filters=mp4&access_token=" + getToken();
        try {
            URL = String.format(URL, URLEncoder.encode(query, HTTP.UTF_8), COUNT, offset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Request.Builder builder = new Request.Builder(URL, RequestMethod.GET).setResponseHandler(VideoRespoceHandler.class);
        sendRequest(builder.create());
    }

    public void getVideoDirectFiles(String url) {
        Request.Builder builder = new Request.Builder(url, RequestMethod.GET).setResponseHandler(DirectVideoFilesReposeHandler.class);
        sendRequest(builder.create(), VIDEO_TOKEN);
    }

    private String getToken() {
        return ACCESS_TOKENS[random.nextInt(ACCESS_TOKENS.length)];
    }

}
