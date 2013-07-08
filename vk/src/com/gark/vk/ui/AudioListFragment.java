package com.gark.vk.ui;

import android.content.AsyncQueryHandler;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.gark.vk.R;
import com.gark.vk.adapters.MusicAdapter;
import com.gark.vk.db.MusicQuery;
import com.gark.vk.model.MusicObject;
import com.gark.vk.navigation.NavigationControllerFragment;
import com.gark.vk.network.ApiHelper;
import com.the111min.android.api.response.ResponseReceiver;

/**
 * Created by Artem on 08.07.13.
 */
public class AudioListFragment extends NavigationControllerFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private MusicAdapter musicAdapter;
    private ListView list;
    private AsyncQueryHandler mAsyncQueryHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAsyncQueryHandler = new AsyncQueryHandler(getActivity().getContentResolver()) {
        };
        new ApiHelper(getActivity(), mResponseReceiver).getPopular();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        musicAdapter = new MusicAdapter(getActivity(), null);
        list.setAdapter(musicAdapter);
        getActivity().getSupportLoaderManager().initLoader(MusicQuery._TOKEN, Bundle.EMPTY, this);
    }

    @Override
    public void onDestroy() {
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
//        musicAdapter.changeCursor(cursor);
        musicAdapter.swapCursor(cursor);
        Toast.makeText(getActivity(), "" + cursor.getCount(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        musicAdapter.swapCursor(null);
    }

    final ResponseReceiver mResponseReceiver = new ResponseReceiver() {
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
