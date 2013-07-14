package com.gark.vk.ui;

import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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

import com.gark.vk.R;
import com.gark.vk.adapters.VideoAdapter;
import com.gark.vk.db.VideoColumns;
import com.gark.vk.db.VideoQuery;
import com.gark.vk.model.VideoObject;
import com.gark.vk.navigation.NavigationControllerFragment;
import com.gark.vk.network.ApiHelper;
import com.gark.vk.network.PopularRespoceHandler;
import com.the111min.android.api.response.ResponseReceiver;

/**
 * Created by Artem on 08.07.13.
 */
public class VideoListFragment extends NavigationControllerFragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {


    public static final String QUERY = "query";
    private String curentQuery;
    private VideoAdapter videoAdapter;
    private ListView list;
    private AsyncQueryHandler mAsyncQueryHandler;
    private ApiHelper mApiHelper;
    private static int offset = 0;
    private int receivedCount;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        offset = 0;
        mAsyncQueryHandler = new AsyncQueryHandler(getActivity().getContentResolver()) {
        };

        curentQuery = getArguments().getString(QUERY);

        mApiHelper = new ApiHelper(getActivity(), mResponseReceiver);
        mApiHelper.getVideoList(offset, curentQuery);


        videoAdapter = new VideoAdapter(getActivity(), null);
    }

    public void updateList(String newQuery) {
        mApiHelper.getVideoList(offset, newQuery);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        list.setAdapter(videoAdapter);
        list.setOnItemClickListener(this);

        getActivity().getSupportLoaderManager().initLoader(VideoQuery._TOKEN, Bundle.EMPTY, this);


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        offset = 0;
        mAsyncQueryHandler.startDelete(0, null, VideoObject.CONTENT_URI, null, null);
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
            case VideoQuery._TOKEN:
                return new CursorLoader(getActivity(), VideoObject.CONTENT_URI, VideoQuery.PROJECTION, null, null, VideoObject.DEFAULT_SORT);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        videoAdapter.swapCursor(cursor);
//        this.cursor = cursor;
        list.setOnScrollListener((receivedCount == 0) ? null : mOnScrollListener);


//        Intent intent = new Intent(getActivity(), PlaybackService.class);
//        intent.setAction(PlaybackService.SERVICE_PLAY_PLAYLIST);
//        intent.putExtra(PlaybackService.SERVICE_PLAY_PLAYLIST, getPlaylist(cursor));
//        getActivity().startService(intent);

//        Intent intent = new Intent();
//        Toast.makeText(getActivity(), "" + cursor.getCount(), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        videoAdapter.swapCursor(null);
    }

    final ResponseReceiver mResponseReceiver = new ResponseReceiver() {
        @Override
        public void onRequestSuccess(int token, Bundle result) {
            receivedCount = result.getInt(PopularRespoceHandler.COUNT);
//            Toast.makeText(getActivity(), "count " + receivedCount, Toast.LENGTH_SHORT).show();
//            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(list.getWindowToken(), 0);
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
                mApiHelper.getVideoList(offset, curentQuery);
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

//        Intent intent = new Intent(getActivity(), PlaybackService.class);
//        intent.setAction(PlaybackService.SERVICE_PLAY_SINGLE);
//        intent.putExtra(PlaybackService.EXTRA_POSITION, position);
//        getActivity().startService(intent);
//
        Cursor cursor = ((VideoAdapter) parent.getAdapter()).getCursor();
        if (cursor != null && cursor.moveToPosition(position)) {
            final String player = cursor.getString(cursor.getColumnIndex(VideoColumns.PLAYER.getName()));
//            final String title = cursor.getString(cursor.getColumnIndex(MusicColumns.TITLE.getName()));

            Intent i = new Intent(Intent.ACTION_VIEW);
//            i.setData(Uri.parse(player));
            i.setDataAndType(Uri.parse("http://vimeo.com/30592532"), "video/*");
//            startActivity(i);

            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://player.vimeo.com/video/30592532")));

//            Intent localIntent = new Intent("android.intent.action.VIEW");
//            localIntent.setDataAndType(Uri.fromFile(new File(((DownloadEntry)paramAnonymousView.getTag()).getFilename())), "video/mp4");
//            App.getInstance().ma.startActivity(localIntent);
        }

    }


//    private ArrayList<MusicObject> getPlaylist(Cursor cursor) {
//        ArrayList<MusicObject> list = new ArrayList<MusicObject>();
//        MusicObject musicObject;
//        if (cursor != null && cursor.moveToFirst()) {
//            do {
//                musicObject = new MusicObject();
//
//                final String aid = cursor.getString(cursor.getColumnIndex(MusicColumns.AID.getName()));
//                final String artist = cursor.getString(cursor.getColumnIndex(MusicColumns.ARTIST.getName()));
//                final String title = cursor.getString(cursor.getColumnIndex(MusicColumns.TITLE.getName()));
//                final String duration = cursor.getString(cursor.getColumnIndex(MusicColumns.DURATION.getName()));
//                final String url = cursor.getString(cursor.getColumnIndex(MusicColumns.URL.getName()));
//
//                musicObject.setAid(aid);
//                musicObject.setArtist(artist);
//                musicObject.setTitle(title);
//                musicObject.setDuration(duration);
//                musicObject.setUrl(url);
//
//                list.add(musicObject);
//
//            } while (cursor.moveToNext());
//        }
//
//        return list;
//    }


//    private class OnPrepareReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            int position = intent.getIntExtra(PlaybackService.SERVICE_ON_PREPARE, 0);
//            list.smoothScrollToPosition(position);
//        }
//    }
//
//    ;


}
