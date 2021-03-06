package com.gark.vk.ui;

import android.content.AsyncQueryHandler;
import android.content.Intent;
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
import android.widget.Toast;

import com.gark.vk.R;
import com.gark.vk.adapters.VideoAdapter;
import com.gark.vk.db.VideoColumns;
import com.gark.vk.db.VideoQuery;
import com.gark.vk.model.VideoObject;
import com.gark.vk.model.VideoTypes;
import com.gark.vk.navigation.NavigationControllerFragment;
import com.gark.vk.network.ApiHelper;
import com.gark.vk.network.DirectVideoFilesReposeHandler;
import com.gark.vk.network.PopularResponceHandler;
import com.gark.vk.services.PlaybackService;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.the111min.android.api.response.ResponseReceiver;

/**
 * Created by Artem on 08.07.13.
 */
public class VideoListFragment extends NavigationControllerFragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {


    private VideoAdapter videoAdapter;
    private ListView list;
    private AsyncQueryHandler mAsyncQueryHandler;
    private ApiHelper mApiHelper;
    private static int offset = 0;
    private int receivedCount = 1;
    private TextView mNoResult;
    private String searchMask = null;
    private String currentTitle = "";
    private Tracker myTracker;
    private Handler handler = new Handler();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setRetainInstance(true);

        offset = 0;
        mAsyncQueryHandler = new AsyncQueryHandler(getActivity().getContentResolver()) {
        };

        mApiHelper = new ApiHelper(getActivity(), mResponseReceiver);


        videoAdapter = new VideoAdapter(getActivity(), null);
    }


    public void updateSearchFilter(String mask) {
        if (/*searchResult != null && */mask != null) {
            offset = 0;
//            isRequestProceed = false;
            searchMask = mask;
            mAsyncQueryHandler.startDelete(0, null, VideoObject.CONTENT_URI, null, null);
            mApiHelper.getVideoList(offset, searchMask);
//            searchResult.setText(getString(R.string.result_search_by, mask));
        }
    }


//    public void updateList(String newQuery) {
//        mApiHelper.getVideoList(offset, newQuery);
//    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        EasyTracker.getInstance().setContext(getActivity());
        myTracker = EasyTracker.getTracker();

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
        View view = inflater.inflate(R.layout.audio_list, null);
        list = (ListView) view.findViewById(R.id.audio_list);
        mNoResult = (TextView) view.findViewById(R.id.no_result);
//        searchResult = (TextView) view.findViewById(R.id.search_result_filter);
//        searchResult.setText((searchMask == null) ? getString(R.string.video_search) : getString(R.string.result_search_by, searchMask));
        return view;
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
//        list.setOnScrollListener((receivedCount == 0) ? null : mOnScrollListener);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                list.setOnScrollListener((videoAdapter.getCount() == 0 || receivedCount == 0) ? null : mOnScrollListener);
            }
        }, 5 * 1000);

//        if (isRequestProceed) {
//            mNoResult.setVisibility((cursor.getCount() == 0) ? View.VISIBLE : View.GONE);
//        }

    }

    private void updateUI() {
//        isRequestProceed = true;
        if (getActivity() != null) {
            getActivity().setProgressBarIndeterminateVisibility(false);
        }
        mNoResult.setVisibility((videoAdapter.getCount() == 0 && receivedCount == 0) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        videoAdapter.swapCursor(null);
    }

    final ResponseReceiver mResponseReceiver = new ResponseReceiver() {
        @Override
        public void onRequestSuccess(int token, Bundle result) {
            switch (token) {
                case ApiHelper.VIDEO_TOKEN:
                    try {
                        VideoTypes videoTypes = result.getParcelable(DirectVideoFilesReposeHandler.VIDEO_TYPES);
                        DialogFragment dialogFragment = new DialogVideoTypeFragment(videoTypes, currentTitle);
                        dialogFragment.show(getActivity().getSupportFragmentManager(), "dlg1");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    receivedCount = result.getInt(PopularResponceHandler.COUNT);
                    updateUI();
                    try {
                        if (result.containsKey(PopularResponceHandler.CAPTCHA)) {
                            DialogFragment dialogFragment = new DialogCaptchaFragment(result.getString(PopularResponceHandler.CAPTCHA));
                            dialogFragment.show(getActivity().getSupportFragmentManager(), "dlg2");

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }

        }

        @Override
        public void onRequestFailure(int token, Bundle result) {
            switch (token) {
                default:
                    receivedCount = 0;
                    updateUI();
                    break;
            }
        }

        @Override
        public void onError(int token, Exception e) {
            switch (token) {
                default:
                    receivedCount = 0;
                    updateUI();
                    StringBuffer sb = new StringBuffer();
                    if (e != null && e.getMessage() != null) {
                        sb.append(e.getMessage());
                    }

                    if (e != null && e.getLocalizedMessage() != null) {
                        sb.append(e.getLocalizedMessage());
                    }
                    myTracker.sendException(sb.toString() + "\n" + VideoListFragment.class.getSimpleName(), false);
                    break;
            }
        }
    };

    final AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem + 3 >= totalItemCount - visibleItemCount && videoAdapter.getCount() != 0) {
                list.setOnScrollListener(null);
                offset += ApiHelper.COUNT;
                mApiHelper.getVideoList(offset, searchMask);
                getActivity().setProgressBarIndeterminateVisibility(true);
//                Toast.makeText(getActivity(), String.valueOf(offset) + " " + receivedCount, Toast.LENGTH_SHORT).show();
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

        Intent intent = new Intent(getActivity(), PlaybackService.class);
        intent.setAction(PlaybackService.SERVICE_STOP);
        getActivity().startService(intent);

//        getSherlockActivity().setTitle("ololol");

//
        Cursor cursor = ((VideoAdapter) parent.getAdapter()).getCursor();
        if (cursor != null && cursor.moveToPosition(position)) {
            final String player = cursor.getString(cursor.getColumnIndex(VideoColumns.PLAYER.getName()));
            final String title = cursor.getString(cursor.getColumnIndex(VideoColumns.TITLE.getName()));
            currentTitle = title;
            mApiHelper.getVideoDirectFiles(player);

//            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://rutube.ru/player.swf?hash=5925153")));

//            final String title = cursor.getString(cursor.getColumnIndex(MusicColumns.TITLE.getName()));

//            Intent i = new Intent(Intent.ACTION_VIEW);
//            i.setData(Uri.parse(player));
//            i.setDataAndType(Uri.parse("http://vimeo.com/30592532"), "video/*");
//            startActivity(i);

//            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://player.vimeo.com/video/30592532")));

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
