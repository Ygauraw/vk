package com.gark.vk.ui;

import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.gark.vk.R;
import com.gark.vk.adapters.MusicAdapter;
import com.gark.vk.db.MusicColumns;
import com.gark.vk.db.MusicQuery;
import com.gark.vk.model.MusicObject;
import com.gark.vk.navigation.NavigationControllerFragment;
import com.gark.vk.network.ApiHelper;
import com.gark.vk.network.PopularRespoceHandler;
import com.gark.vk.services.PlaybackService;
import com.the111min.android.api.response.ResponseReceiver;

import java.util.ArrayList;

/**
 * Created by Artem on 08.07.13.
 */
public class AudioListFragment extends NavigationControllerFragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    private MusicAdapter musicAdapter;
    private ListView list;
    private AsyncQueryHandler mAsyncQueryHandler;
    private ApiHelper mApiHelper;
    private static int offset = 0;
    private int receivedCount;

//    private Cursor cursor;
//    private int currentTrackNumber = 0;
//    private BroadcastReceiver moveingReceiver;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAsyncQueryHandler = new AsyncQueryHandler(getActivity().getContentResolver()) {
        };
        mApiHelper = new ApiHelper(getActivity(), mResponseReceiver);
        mApiHelper.getPopular(offset);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        musicAdapter = new MusicAdapter(getActivity(), null);
        list.setAdapter(musicAdapter);
        list.setOnItemClickListener(this);

        getActivity().getSupportLoaderManager().initLoader(MusicQuery._TOKEN, Bundle.EMPTY, this);


//        moveingReceiver = new MovingReceiver();
//        getActivity().registerReceiver(moveingReceiver, new IntentFilter(PlaybackService.SERVICE_PLAY_MOVE));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        if (moveingReceiver != null) {
//            getActivity().unregisterReceiver(moveingReceiver);
//            moveingReceiver = null;
//        }
    }

    @Override
    public void onDestroy() {
        offset = 0;
        mAsyncQueryHandler.startDelete(0, null, MusicObject.CONTENT_URI, null, null);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        list = (ListView) inflater.inflate(R.layout.audio_list, null);
        return list;
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
//        this.cursor = cursor;
        list.setOnScrollListener((receivedCount == 0) ? null : mOnScrollListener);


        Intent intent = new Intent(getActivity(), PlaybackService.class);
        intent.setAction(PlaybackService.SERVICE_PLAY_PLAYLIST);
        intent.putExtra(PlaybackService.SERVICE_PLAY_PLAYLIST, getPlaylist(cursor));
        getActivity().startService(intent);

//        Intent intent = new Intent();
//        Toast.makeText(getActivity(), "" + cursor.getCount(), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        musicAdapter.swapCursor(null);
    }

    final ResponseReceiver mResponseReceiver = new ResponseReceiver() {
        @Override
        public void onRequestSuccess(int token, Bundle result) {
            receivedCount = result.getInt(PopularRespoceHandler.COUNT);
//            Toast.makeText(getActivity(), "count " + receivedCount, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRequestFailure(int token, Bundle result) {

        }

        @Override
        public void onError(int token, Exception e) {

        }
    };

    final AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem + 3 >= totalItemCount - visibleItemCount) {
                offset += ApiHelper.COUNT;
                mApiHelper.getPopular(offset);
                list.setOnScrollListener(null);
            }
        }
    };

//    private void playMusic(String url) {
//        Intent intent = new Intent(getActivity(), PlaybackService.class);
//        intent.setAction(PlaybackService.SERVICE_PLAY_ENTRY);
//        intent.putExtra(PlaybackService.EXTRA_URL, url);
//        getActivity().startService(intent);
//    }

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

//        list.smoothScrollToPosition(0);
    }

//    private class MovingReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//
//            String moving = intent.getStringExtra(PlaybackService.SERVICE_PLAY_MOVE);
//
//            if (PlaybackService.SERVICE_PLAY_NEXT.equals(moving)) {
//                if (cursor != null && cursor.moveToPosition(currentTrackNumber)) {
//                    if (cursor.moveToNext()) {
//                        currentTrackNumber = cursor.getPosition();
//                        final String url = cursor.getString(cursor.getColumnIndex(MusicColumns.URL.getName()));
//                        playMusic(url);
//                    } else if (cursor.moveToPosition(0)) {
//                        currentTrackNumber = 0;
//                        final String url = cursor.getString(cursor.getColumnIndex(MusicColumns.URL.getName()));
//                        playMusic(url);
//                    }
//                }
//            } else if (PlaybackService.SERVICE_PLAY_PREVIOUS.equals(moving)){
//                if (cursor != null && cursor.moveToPosition(currentTrackNumber)) {
//                    if (cursor.moveToPrevious()) {
//                        currentTrackNumber = cursor.getPosition();
//                        final String url = cursor.getString(cursor.getColumnIndex(MusicColumns.URL.getName()));
//                        playMusic(url);
//                    } else if (cursor.moveToPosition(0)) {
//                        currentTrackNumber = 0;
//                        final String url = cursor.getString(cursor.getColumnIndex(MusicColumns.URL.getName()));
//                        playMusic(url);
//                    }
//                }
//            }
//        }
//    }

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
}
