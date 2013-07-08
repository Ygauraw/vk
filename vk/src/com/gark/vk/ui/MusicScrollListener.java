package com.gark.vk.ui;

import android.content.Context;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.gark.vk.network.ApiHelper;
import com.gark.vk.network.PopularRespoceHandler;
import com.the111min.android.api.response.ResponseReceiver;

/**
 * Created by Gark on 08.07.13.
 */
public class MusicScrollListener implements AbsListView.OnScrollListener {

    private ApiHelper mApiHelper;
    private Context context;
    private ListView list;

    public MusicScrollListener(Context context, ListView list) {
        this.context = context;
        this.list = list;
        mApiHelper = new ApiHelper(context, mResponseReceiver);
        mApiHelper.getPopular(0);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem >= totalItemCount - visibleItemCount) {
            mApiHelper.getPopular(list.getCount());
            list.setOnScrollListener(null);
        }
    }

    final ResponseReceiver mResponseReceiver = new ResponseReceiver() {
        @Override
        public void onRequestSuccess(int token, Bundle result) {

            int count = result.getInt(PopularRespoceHandler.COUNT);
            Toast.makeText(context, "count " + count + "token = " + token, Toast.LENGTH_SHORT).show();
            list.setOnScrollListener((count == 0) ? null : MusicScrollListener.this);
        }

        @Override
        public void onRequestFailure(int token, Bundle result) {

        }

        @Override
        public void onError(int token, Exception e) {

        }
    };
}
