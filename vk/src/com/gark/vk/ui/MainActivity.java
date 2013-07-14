package com.gark.vk.ui;

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.gark.vk.R;
import com.gark.vk.adapters.SuggestionsAdapter;
import com.gark.vk.db.SuggestionColumns;
import com.gark.vk.db.SuggestionQuery;
import com.gark.vk.model.MusicObject;
import com.gark.vk.model.SuggestionObject;
import com.gark.vk.model.VideoObject;
import com.gark.vk.navigation.NavigationController;

public class MainActivity extends BaseActivity implements SearchView.OnQueryTextListener, SearchView.OnSuggestionListener, ActionBar.OnNavigationListener {

    private NavigationController navigationController;
    private AsyncQueryHandler mAsyncQueryHandler;
    private SuggestionsAdapter mSuggestionsAdapter;
    private SearchView searchView;
    private int currentPosition = 0;
    private ControlsFragment controlsFragment;


    public MainActivity() {
        super(R.string.app_name);
    }

    public MainActivity(int titleRes) {
        super(titleRes);
    }

    // String uri =
    // "http://api.vk.com/oauth/authorize?client_id=3746605&redirect_uri=http://api.vk.com/blank.html&scope=nohttps&display=page&response_type=token";
    // String uri =
    // "https://oauth.vk.com/authorize?client_id=2709622&redirect_uri=http://api.vk.com/blank.html&display=page&v=5.0&scope=audio,video,offline&response_type=token";

    // String get =
    // "http://oauth.vk.com/authorize?client_id=3746605&scope=audio,video,friends,offline,groups&redirect_uri=http://oauth.vk.com/blank.html&display=wap&response_type=token";
//	String get = "https://api.vk.com/method/audio.search.json?q=AC/DC%20-%20Highway%20to%20Hell&access_token=37176714256e377a408a2478728e7c06fc0586dcff56aed28d6b1dd1e3598f3af7e7a9268bb1b3ffd2c6d";

    // String uri =
    // "https://oauth.vk.com/authorize?client_id=3746605&response_type=token";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAsyncQueryHandler = new AsyncQueryHandler(getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                mSuggestionsAdapter.changeCursor(cursor);
                super.onQueryComplete(token, cookie, cursor);
            }
        };

        navigationController = new NavigationController(this, null);

        if (savedInstanceState == null) {
            Fragment fragment = new PopularListFragment();
            getNavigationController().pushView(this, R.id.main_frame, fragment, NavigationController.Transition.NO_EFFECT, NavigationController.Backstack.DO_NOT_ADD);

            controlsFragment = new ControlsFragment();
            getNavigationController().pushView(this, R.id.controls_frame, controlsFragment, NavigationController.Transition.VERTICAL, NavigationController.Backstack.DO_NOT_ADD);
        }

        ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(this, R.array.search_list, R.layout.sherlock_spinner_item);
        list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(list, this);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

    }

    public NavigationController getNavigationController() {
        return navigationController;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                toggle();
                break;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //Create the search view
        searchView = new SearchView(getSupportActionBar().getThemedContext());
        searchView.setQueryHint(getString(R.string.search_for));
        searchView.setOnQueryTextListener(this);
        searchView.setOnSuggestionListener(this);

        if (mSuggestionsAdapter == null) {
            mSuggestionsAdapter = new SuggestionsAdapter(getSupportActionBar().getThemedContext(), null);

        }

        searchView.setSuggestionsAdapter(mSuggestionsAdapter);

        menu.add(R.string.search)
                .setIcon(R.drawable.abs__ic_search)
                .setActionView(searchView)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {

        currentPosition = itemPosition;

        Toast.makeText(this, "" + itemPosition + " " + itemId, Toast.LENGTH_SHORT).show();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
//        if (fragment.isHidden()) {
//            ft.show(fragment);
//            button.setText("Hide");
//        } else {
//            ft.hide(fragment);
//            button.setText("Show");
//        }
//        ft.commit();
        switch (itemPosition) {
            case 0:
                ft.show(controlsFragment);
                break;
            case 1:
                ft.hide(controlsFragment);
                break;
        }
        ft.commit();


        return false;
    }


    @Override
    public boolean onQueryTextSubmit(String query) {


        pushView(query);

        ContentValues contentValues = new ContentValues();
        contentValues.put(SuggestionColumns.TEXT.getName(), query.trim());
        mAsyncQueryHandler.startInsert(0, null, SuggestionObject.CONTENT_URI, contentValues);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Cursor cursor = getContentResolver().query(SuggestionObject.CONTENT_URI, SuggestionQuery.PROJECTION, SuggestionColumns.TEXT.getName() + " LIKE ?", new String[]{"%" + newText + "%"}, null);
        mSuggestionsAdapter.changeCursor(cursor);
        return false;
    }


    @Override
    public boolean onSuggestionSelect(int position) {
        return false;
    }

    @Override
    public boolean onSuggestionClick(int position) {
        String query = null;
        Cursor cursor = mSuggestionsAdapter.getCursor();
        if (cursor != null && cursor.moveToPosition(position)) {
            query = cursor.getString(cursor.getColumnIndex(SuggestionColumns.TEXT.getName()));
        }
        pushView(query);
        return false;
    }

    private void pushView(String query) {


        searchView.clearFocus();
        searchView.onActionViewCollapsed();

        switch (currentPosition) {
            case 0:

                AudioListFragment audioListFragment = (AudioListFragment) getSupportFragmentManager().findFragmentByTag(AudioListFragment.class.getSimpleName());
                if (audioListFragment == null) {
                    Fragment fragment = new AudioListFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(AudioListFragment.QUERY, query);
                    fragment.setArguments(bundle);
                    getNavigationController().pushView(this, R.id.main_frame, fragment, NavigationController.Transition.NO_EFFECT, NavigationController.Backstack.DO_NOT_ADD);
                } else {
                    audioListFragment.updateList(query);
                }

                mAsyncQueryHandler.startDelete(0, null, MusicObject.CONTENT_URI, null, null);
                break;
            case 1:

                VideoListFragment videoListFragment = (VideoListFragment) getSupportFragmentManager().findFragmentByTag(VideoListFragment.class.getSimpleName());
                if (videoListFragment == null) {
                    Fragment fragment = new VideoListFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(VideoListFragment.QUERY, query);
                    fragment.setArguments(bundle);
                    getNavigationController().pushView(this, R.id.main_frame, fragment, NavigationController.Transition.NO_EFFECT, NavigationController.Backstack.DO_NOT_ADD);
                } else {
                    videoListFragment.updateList(query);
                }

                mAsyncQueryHandler.startDelete(0, null, VideoObject.CONTENT_URI, null, null);
                break;
        }


    }


}
