package com.gark.vk.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.gark.vk.R;
import com.gark.vk.network.ApiHelper;
import com.gark.vk.utils.BitmapLruCache;
import com.gark.vk.utils.Log;
import com.the111min.android.api.response.ResponseReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by Gark on 21.07.13.
 */
public class DialogCaptchaFragment extends DialogFragment implements DialogInterface.OnClickListener {

    public static final String ERROR = "error";
    //    public static final String ERROR_CODE = "error_code";
//    public static final String ERROR_MESSAGE = "error_msg";
//    public static final String METHOD = "method";
//    public static final String COUNT = "count";
//    public static final String OFFSET = "offset";
//    public static final String ONLY_ENG = "only_eng";
    public static final String REQUEST_PARAMS = "request_params";

    public static final String CAPTCHA_SIG = "captcha_sid";
    public static final String CAPTCHA_IMG = "captcha_img";

    public static final String KEY = "key";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String VALUE = "value";

    public static final String METHOD = "method";
    public static final String QUERY = "q";


    private String query;
    private String method;
    private String token;
    private String captchaUrl;
    private String captchaSig;
    private String captchaResponse;
    private ApiHelper mApiHelper;
    private LayoutInflater mInflater;

    private EditText captchaText;
    private ImageView imageCaptcha;
    private ImageLoader mImageLoader;
    private RequestQueue mRequestQ;


    public DialogCaptchaFragment() {
        Log.e("");
    }

    public DialogCaptchaFragment(String captchaResponse) {
        this.captchaResponse = captchaResponse;
        parseCaptcha();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRequestQ = Volley.newRequestQueue(getActivity());
        mImageLoader = new ImageLoader(mRequestQ, new BitmapLruCache(getActivity()));
        mApiHelper = new ApiHelper(getActivity(), responseReceiver);
    }

    final ResponseReceiver responseReceiver = new ResponseReceiver() {
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(CAPTCHA_IMG, captchaUrl);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {


        mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.captcha_layout, null);
        imageCaptcha = (ImageView) view.findViewById(R.id.captcha_image);
        captchaText = (EditText) view.findViewById(R.id.captcha_text);

        if (savedInstanceState != null) {
            captchaUrl = savedInstanceState.getString(CAPTCHA_IMG);
        }
        mImageLoader.get(captchaUrl, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                imageCaptcha.setImageBitmap(response.getBitmap());
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                imageCaptcha.setImageResource(R.drawable.ic_movie_default);
            }
        });


        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.captcha_text)
                .setPositiveButton(R.string.apply, this)
                .setView(view);
        return adb.create();
    }


    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                String captcha_key = captchaText.getText().toString().trim();
                String url = String.format("https://api.vk.com/method/%s?&q=%s&count=%s&offset=%s&only_eng=%s&access_token=%s&captcha_sid=%s&captcha_key=%s", method, query, 30, 0, 1, token, captchaSig, captcha_key);
                mApiHelper.getRequestCaptcha(url);
                break;
        }
    }

    private void parseCaptcha() {
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(captchaResponse);
            if (!jsonObj.isNull(ERROR)) {

                JSONObject jImg = jsonObj.getJSONObject(ERROR);

                if (!jImg.isNull(CAPTCHA_IMG)) {
                    captchaUrl = jImg.getString(CAPTCHA_IMG);
                }

                if (!jImg.isNull(CAPTCHA_SIG)) {
                    captchaSig = jImg.getString(CAPTCHA_SIG);
                }

                if (!jImg.isNull(REQUEST_PARAMS)) {

                    JSONArray jsonArray = jImg.getJSONArray(REQUEST_PARAMS);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject j = (JSONObject) jsonArray.get(i);
                        String key = j.getString(KEY);
                        String value = j.getString(VALUE);

                        if (QUERY.equals(key)) {
                            query = value;
                        }

                        if (METHOD.equals(key)) {
                            method = value;
                        }

                        if (ACCESS_TOKEN.equals(key)) {
                            token = value;
                            break;
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
