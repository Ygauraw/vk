package com.gark.vk.ui;

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.actionbarsherlock.widget.SearchView;
import com.gark.vk.R;
import com.gark.vk.adapters.SuggestionsAdapter;
import com.gark.vk.db.MusicQuery;
import com.gark.vk.db.SuggestionColumns;
import com.gark.vk.db.SuggestionQuery;
import com.gark.vk.model.MusicObject;
import com.gark.vk.model.SuggestionObject;
import com.gark.vk.navigation.NavigationController;
import com.gark.vk.services.PlaybackService;
import com.gark.vk.utils.AnalyticsExceptionParser;
import com.gark.vk.utils.PlayerUtils;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.ExceptionReporter;
import com.viewpagerindicator.TitlePageIndicator;

public class MainActivity1 extends SherlockFragmentActivity implements SearchView.OnQueryTextListener, SearchView.OnSuggestionListener/*, LoaderManager.LoaderCallbacks<Cursor> */ {

    private NavigationController navigationController;
    private AsyncQueryHandler mAsyncQueryHandler;
    private SuggestionsAdapter mSuggestionsAdapter;
    private SearchView searchView;
    private ControlsFragment controlsFragment;
    private String[] titles;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private ViewPager viewPager;
    private View controlsFrame;
    private Animation animSlideIn;
    private Animation animSlideOut;

    private MyFragmentPagerAdapter fragmentPagerAdapter;
    private TitlePageIndicator mIndicator;
    public static final int SEARCH_TOKEN = 444;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);


        EasyTracker.getInstance().setContext(this);

        // Change uncaught exception parser...
        // Note: Checking uncaughtExceptionHandler type can be useful if clearing ga_trackingId during development to disable analytics - avoid NullPointerException.
        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (uncaughtExceptionHandler instanceof ExceptionReporter) {
            ExceptionReporter exceptionReporter = (ExceptionReporter) uncaughtExceptionHandler;
            exceptionReporter.setExceptionParser(new AnalyticsExceptionParser());
        }

        setContentView(R.layout.activity_main1);
        animSlideIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_left_out);
        animSlideOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_right_in);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        controlsFrame = findViewById(R.id.controls_frame);

        titles = new String[]{getString(R.string.music), getString(R.string.video)};

//        setTitle("");
        setSupportProgressBarIndeterminateVisibility(false);

        mAsyncQueryHandler = new AsyncQueryHandler(getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                super.onQueryComplete(token, cookie, cursor);
                mSuggestionsAdapter.swapCursor(cursor);
            }
        };

        navigationController = new NavigationController(this, null);

        if (savedInstanceState == null) {
            controlsFragment = new ControlsFragment();
            getNavigationController().pushView(this, R.id.controls_frame, controlsFragment, NavigationController.Transition.NO_EFFECT, NavigationController.Backstack.DO_NOT_ADD);
        }

        fragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(fragmentPagerAdapter);
        int currentPosition = PlayerUtils.getLastPosition(this);

        mIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        mIndicator.setOnPageChangeListener(onPageChangeListener);
        mIndicator.setViewPager(viewPager);

        viewPager.setCurrentItem(currentPosition);


//            Fragment fragment = new PopularListFragment();
//            getNavigationController().pushView(this, R.id.main_frame, fragment, NavigationController.Transition.NO_EFFECT, NavigationController.Backstack.DO_NOT_ADD);
//
//            controlsFragment = new ControlsFragment();
//            getNavigationController().pushView(this, R.id.controls_frame, controlsFragment, NavigationController.Transition.NO_EFFECT, NavigationController.Backstack.DO_NOT_ADD);

//            ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(this, R.array.search_list, R.layout.sherlock_spinner_item);
//            list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
//
//            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
//            getSupportActionBar().setListNavigationCallbacks(list, this);


//            list = ArrayAdapter.createFromResource(this, R.array.search_list, R.layout.sherlock_spinner_item);
//            list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
//            getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
//            getSupportActionBar().setListNavigationCallbacks(list, this);

//        }

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.yellow_bg)));


        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(getTitle());
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(getTitle());
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

//        mDrawerList

        String[] leftListContent = getResources().getStringArray(R.array.left_list);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, leftListContent));
        mDrawerList.setOnItemClickListener(onItemClickListener);


        if (mSuggestionsAdapter == null) {
            mSuggestionsAdapter = new SuggestionsAdapter(getSupportActionBar().getThemedContext(), null);
        }

//        getSupportLoaderManager().initLoader(SEARCH_TOKEN, Bundle.EMPTY, this);
    }

    final ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int i, float v, int i2) {

        }

        @Override
        public void onPageSelected(int i) {
            FragmentTransaction fmt = getSupportFragmentManager().beginTransaction();
            switch (i) {
                case 0:
                    controlsFrame.setAnimation(animSlideOut);
                    controlsFrame.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    controlsFrame.setAnimation(animSlideIn);
                    controlsFrame.setVisibility(View.GONE);
                    break;
            }
            fmt.commit();

        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    public NavigationController getNavigationController() {
        return navigationController;
    }


    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
//        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
//        menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                    mDrawerLayout.closeDrawer(mDrawerList);
                } else {
                    mDrawerLayout.openDrawer(mDrawerList);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Intent intent = new Intent(this, PlaybackService.class);
        intent.setAction(PlaybackService.NOTIFICATION_CLOSE_APPLICATION);
        startService(intent);
        EasyTracker.getInstance().activityStop(this); // Add this method.

        if (mSuggestionsAdapter != null) {
            mSuggestionsAdapter.swapCursor(null);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this); // Add this method.
    }


//    public ViewPager getViewPager() {
//        return viewPager;
//    }

    @Override
    protected void onDestroy() {
        int currentPosition = viewPager.getCurrentItem();
        PlayerUtils.setLastPosition(this, currentPosition);
//        PlayerUtils.setLastQuery(this, searchView.getQuery().toString());

//        Toast.makeText(this, searchView.getQuery(), Toast.LENGTH_SHORT).show();

        super.onDestroy();
//        if (list != null) {
//            getSupportActionBar().setListNavigationCallbacks(list, null);
//        }
    }

    //    @Override
//    public boolean onMenuItemSelected(int featureId, MenuItem item) {
//
//        int itemId = item.getItemId();
//        switch (itemId) {
//            case android.R.id.home:
////                toggle();
//                break;
//        }
//        return super.onMenuItemSelected(featureId, item);
//    }


//    boolean needToCloseSearch = false;
//    final SearchView.OnCloseListener onCloseListener = new SearchView.OnCloseListener() {
//        @Override
//        public boolean onClose() {
//            needToCloseSearch = true;
//            Toast.makeText(MainActivity1.this, "close", Toast.LENGTH_SHORT).show();
//            invalidateOptionsMenu();
//            return false;
//        }
//    };

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        //Create the search view
        searchView = new SearchView(getSupportActionBar().getThemedContext());
        searchView.setQueryHint(getString(R.string.search_for));
        searchView.setOnQueryTextListener(this);
        searchView.setOnSuggestionListener(this);
//        searchView.setOnCloseListener(onCloseListener);

        searchView.setSuggestionsAdapter(mSuggestionsAdapter);


        menu.add(R.string.search)
                .setIcon(R.drawable.abs__ic_search)
                .setActionView(searchView)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
//        searchView.onActionViewExpanded();
//        searchView.setQuery(PlayerUtils.getLastQuery(this), false);

//        menu.add(0, 2, 0, R.string.search)
//                .setIcon(R.drawable.abs__ic_search)
//                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//
//        if (needToCloseSearch){
//            menu.findItem(2).setVisible(false);
//        }

        return true;
    }


//    @Override
//    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
//        currentPosition = itemPosition;
//
////        Toast.makeText(this, "" + itemPosition + " " + itemId, Toast.LENGTH_SHORT).show();
//        return true;
//    }


    @Override
    public boolean onQueryTextSubmit(String query) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(SuggestionColumns.TEXT.getName(), query.trim());
        mAsyncQueryHandler.startInsert(0, null, SuggestionObject.CONTENT_URI, contentValues);

        updateSearchMaskValue(query);
        searchView.clearFocus();

        return false;
    }


    @Override
    public boolean onQueryTextChange(String newText) {
        try {
            mAsyncQueryHandler.startQuery(0, null, SuggestionObject.CONTENT_URI, SuggestionQuery.PROJECTION, SuggestionColumns.TEXT.getName() + " LIKE ?", new String[]{"%" + newText + "%"}, null);
//            Cursor cursor = getContentResolver().query(SuggestionObject.CONTENT_URI, SuggestionQuery.PROJECTION, SuggestionColumns.TEXT.getName() + " LIKE ?", new String[]{"%" + newText + "%"}, null);
//            mSuggestionsAdapter.swapCursor(cursor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public boolean onSuggestionSelect(int position) {
        return false;
    }

    @Override
    public boolean onSuggestionClick(int position) {
        String query = null;
        try {
            Cursor cursor = mSuggestionsAdapter.getCursor();
            if (cursor != null && cursor.moveToPosition(position)) {
                query = cursor.getString(cursor.getColumnIndex(SuggestionColumns.TEXT.getName()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateSearchMaskValue(query);
        searchView.setQuery(query, false);
        searchView.clearFocus();

        return false;
    }


    private void updateSearchMaskValue(String filter) {

        switch (viewPager.getCurrentItem()) {
            case 0:
                PopularListFragment popularListFragment = (PopularListFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + fragmentPagerAdapter.getItemId(0));
                if (popularListFragment != null) {
                    popularListFragment.updateSearchFilter(filter);
                }
                break;
            case 1:
                VideoListFragment videoListFragment = (VideoListFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + viewPager.getId() + ":" + fragmentPagerAdapter.getItemId(1));
                if (videoListFragment != null) {
                    videoListFragment.updateSearchFilter(filter);
                }
                break;
        }


    }

    final AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

        String appName;

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 0:
                    getContentResolver().delete(SuggestionObject.CONTENT_URI, null, null);
                    Toast.makeText(MainActivity1.this, R.string.history_was_erased, Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    appName = "com.gark.vk";
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appName)));
                    }
                    break;
                case 2:
                    appName = "Modest Fish";
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://developer?id=" + appName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/developer?id=" + appName)));
                    }
                    break;
            }

        }
    };

//    @Override
//    public Loader<Cursor> onCreateLoader(int token, Bundle bundle) {
//        switch (token) {
//            case SEARCH_TOKEN:
//                return new CursorLoader(this, MusicObject.CONTENT_URI, MusicQuery.PROJECTION, null, null, MusicObject.DEFAULT_SORT);
//            default:
//                return null;
//        }
//
//    }
//
//    @Override
//    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
//        mSuggestionsAdapter.changeCursor(cursor);
//    }
//
//    @Override
//    public void onLoaderReset(Loader<Cursor> cursorLoader) {
//        mSuggestionsAdapter.swapCursor(null);
//    }


    public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;
            switch (i) {
                case 0:
                    fragment = new PopularListFragment();
                    break;
                case 1:
                    fragment = new VideoListFragment();
                    break;

            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }


}
