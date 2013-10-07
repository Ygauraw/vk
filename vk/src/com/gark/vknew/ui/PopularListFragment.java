package com.gark.vknew.ui;

import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.gark.vknew.R;
import com.gark.vknew.adapters.MusicAdapter;
import com.gark.vknew.db.MusicColumns;
import com.gark.vknew.db.MusicQuery;
import com.gark.vknew.model.MusicObject;
import com.gark.vknew.navigation.NavigationControllerFragment;
import com.gark.vknew.network.ApiHelper;
import com.gark.vknew.network.PopularResponseHandler;
import com.gark.vknew.services.PlaybackService;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.the111min.android.api.response.ResponseReceiver;

import java.util.ArrayList;

/**
 * Created by Artem on 08.07.13.
 */
public class PopularListFragment extends NavigationControllerFragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {


    private MusicAdapter musicAdapter;
    private ListView list;
    private TextView mNoResult;
    //    private TextView searchResult;
    private AsyncQueryHandler mAsyncQueryHandler;
    private ApiHelper mApiHelper;
    private static int offset = 0;
    private int receivedCount = 1;
    private BroadcastReceiver onPrepareReceiver;
    //    private boolean isRequestProceed = false;
    private int mRequestType = ApiHelper.POPULAR_TOKEN;
    private String searchMask = null;
    private Handler handler = new Handler();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

//        myTracker = EasyTracker.getTracker();

        offset = 0;
        mAsyncQueryHandler = new AsyncQueryHandler(getActivity().getContentResolver()) {
        };

        mApiHelper = new ApiHelper(getActivity(), mResponseReceiver);
        mRequestType = ApiHelper.POPULAR_TOKEN;
        mApiHelper.getSongsList(offset, null, mRequestType);


        getActivity().setProgressBarIndeterminateVisibility(true);
        musicAdapter = new MusicAdapter(getActivity(), null);
    }


    public void updateSearchFilter(String mask, int videoTypeToken) {
        if (mask != null) {
            offset = 0;
            searchMask = mask;
            mAsyncQueryHandler.startDelete(0, null, MusicObject.CONTENT_URI, null, null);
            mRequestType = videoTypeToken;
            mApiHelper.getSongsList(offset, searchMask, mRequestType);
        }
    }

    public void updateMyVKMusic(int videoTypeToken) {
        offset = 0;
        mAsyncQueryHandler.startDelete(0, null, MusicObject.CONTENT_URI, null, null);
        mRequestType = videoTypeToken;
        mApiHelper.getSongsList(offset, searchMask, mRequestType);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        list.setAdapter(musicAdapter);
        list.setOnItemClickListener(this);

        getActivity().getSupportLoaderManager().initLoader(MusicQuery._TOKEN, Bundle.EMPTY, this);

        onPrepareReceiver = new OnPrepareReceiver();
        getActivity().registerReceiver(onPrepareReceiver, new IntentFilter(PlaybackService.SERVICE_ON_PREPARE));


    }

    @Override
    public void onDestroyView() {
        if (onPrepareReceiver != null) {
            getActivity().unregisterReceiver(onPrepareReceiver);
            onPrepareReceiver = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        offset = 0;
        mAsyncQueryHandler.startDelete(0, null, MusicObject.CONTENT_URI, null, null);
        super.onDestroy();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.audio_list, null);
        list = (ListView) view.findViewById(R.id.audio_list);
        mNoResult = (TextView) view.findViewById(R.id.no_result);
        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int token, Bundle bundle) {
        switch (token) {
            case MusicQuery._TOKEN:
                return new CursorLoader(getActivity(), MusicObject.CONTENT_URI, MusicQuery.PROJECTION, null, null, MusicObject.DEFAULT_SORT);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        musicAdapter.swapCursor(cursor);


//        list.setOnScrollListener((receivedCount == 0) ? null : mOnScrollListener);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                list.setOnScrollListener((musicAdapter.getCount() == 0 || receivedCount == 0) ? null : mOnScrollListener);
            }
        }, 3 * 1000);

        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), PlaybackService.class);
            intent.setAction(PlaybackService.SERVICE_PLAY_PLAYLIST);
            intent.putExtra(PlaybackService.SERVICE_PLAY_PLAYLIST, getPlaylist(cursor));
            getActivity().startService(intent);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        musicAdapter.swapCursor(null);
    }

    final ResponseReceiver mResponseReceiver = new ResponseReceiver() {

        @Override
        public void onRequestSuccess(int token, Bundle result) {


            switch (token) {
                case ApiHelper.AUDIO_TOKEN:

                    receivedCount = result.getInt(PopularResponseHandler.COUNT);
//                    updateUI();
                    try {
                        if (result.containsKey(PopularResponseHandler.CAPTCHA)) {
                            DialogFragment dialogFragment = new DialogCaptchaFragment(result.getString(PopularResponseHandler.CAPTCHA));
                            dialogFragment.show(getActivity().getSupportFragmentManager(), "dlg2");
                        } else if (result.containsKey(PopularResponseHandler.AUTHORIZATION_ERROR)) {
                            ((MainActivity1) getActivity()).showDialogLogin(MainActivity1.POPULAR_VK_LIST);
                        } else {
                            updateUI();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (getActivity() != null) {
                        getActivity().setProgressBarIndeterminateVisibility(false);
                    }

                    break;
            }


        }

        @Override
        public void onRequestFailure(int token, Bundle result) {
            receivedCount = 0;
            updateUI();
        }

        @Override
        public void onError(int token, Exception e) {
            receivedCount = 0;
            updateUI();
        }
    };

    private void updateUI() {
        if (getActivity() != null) {
            getActivity().setProgressBarIndeterminateVisibility(false);
        }
        mNoResult.setVisibility((musicAdapter.getCount() == 0 && receivedCount == 0) ? View.VISIBLE : View.GONE);
    }

    final AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem + 3 >= totalItemCount - visibleItemCount && musicAdapter.getCount() != 0 && PopularListFragment.this.isVisible()) {
                offset += ApiHelper.COUNT;
                list.setOnScrollListener(null);
                mApiHelper.getSongsList(offset, searchMask, mRequestType);
                getActivity().setProgressBarIndeterminateVisibility(true);
//                Toast.makeText(getActivity(), String.valueOf(offset) + " " + receivedCount, Toast.LENGTH_SHORT).show();
            }

        }
    };


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent intent = new Intent(getActivity(), PlaybackService.class);
        intent.setAction(PlaybackService.SERVICE_PLAY_SINGLE);
        intent.putExtra(PlaybackService.EXTRA_POSITION, position);
        getActivity().startService(intent);

        Cursor cursor = ((MusicAdapter) parent.getAdapter()).getCursor();
        if (cursor != null && cursor.moveToPosition(position)) {
            final String artist = cursor.getString(cursor.getColumnIndex(MusicColumns.ARTIST.getName()));
            final String title = cursor.getString(cursor.getColumnIndex(MusicColumns.TITLE.getName()));

            Intent tempUpdateBroadcast = new Intent(PlaybackService.SERVICE_UPDATE_NAME);
            tempUpdateBroadcast.putExtra(PlaybackService.EXTRA_DURATION, 2);
            tempUpdateBroadcast.putExtra(PlaybackService.EXTRA_ARTIST, artist);
            tempUpdateBroadcast.putExtra(PlaybackService.EXTRA_TITLE, title);
            getActivity().sendBroadcast(tempUpdateBroadcast);

        }

    }


    private ArrayList<MusicObject> getPlaylist(Cursor cursor) {
        ArrayList<MusicObject> list = new ArrayList<MusicObject>();
        MusicObject musicObject;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                musicObject = new MusicObject();

                final String aid = cursor.getString(cursor.getColumnIndex(MusicColumns.AID.getName()));
                final String artist = cursor.getString(cursor.getColumnIndex(MusicColumns.ARTIST.getName()));
                final String title = cursor.getString(cursor.getColumnIndex(MusicColumns.TITLE.getName()));
                final String duration = cursor.getString(cursor.getColumnIndex(MusicColumns.DURATION.getName()));
                final String url = cursor.getString(cursor.getColumnIndex(MusicColumns.URL.getName()));

                musicObject.setAid(aid);
                musicObject.setArtist(artist);
                musicObject.setTitle(title);
                musicObject.setDuration(duration);
                musicObject.setUrl(url);

                list.add(musicObject);

            } while (cursor.moveToNext());
        }

        return list;
    }


    private class OnPrepareReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int position = intent.getIntExtra(PlaybackService.SERVICE_ON_PREPARE, 0);
            int last = list.getLastVisiblePosition();
            int first = list.getFirstVisiblePosition();

            if (last <= position || position <= first) {
                list.setSelection(position);

            }
        }
    }

    ;


}
