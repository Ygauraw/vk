package com.gark.vk.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.gark.vk.R;
import com.gark.vk.navigation.NavigationController;
import com.gark.vk.network.ApiHelper;
import com.the111min.android.api.response.ResponseReceiver;

public class MainActivity extends BaseActivity {

    private NavigationController navigationController;

    public MainActivity() {
        super(R.string.app_name);
    }

    public MainActivity(int titleRes) {
        super(titleRes);
    }

    // String uri =
    // "http://api.vk.com/oauth/authorize?client_id=3746605&redirect_uri=http://api.vk.com/blank.html&scope=nohttps&display=page&response_type=token";
    // String uri =
    // "https://oauth.vk.com/authorize?client_id=3746605&redirect_uri=http://api.vk.com/blank.html&scope=nohttps&display=page&v=5.0&response_type=token";

    // String get =
    // "http://oauth.vk.com/authorize?client_id=3746605&scope=audio,video,friends,offline,groups&redirect_uri=http://oauth.vk.com/blank.html&display=wap&response_type=token";
//	String get = "https://api.vk.com/method/audio.search.json?q=AC/DC%20-%20Highway%20to%20Hell&access_token=37176714256e377a408a2478728e7c06fc0586dcff56aed28d6b1dd1e3598f3af7e7a9268bb1b3ffd2c6d";

    // String uri =
    // "https://oauth.vk.com/authorize?client_id=3746605&response_type=token";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navigationController = new NavigationController(this, null);

        Fragment fragment = new AudioListFragment();
        getNavigationController().pushView(this, R.id.main_frame, fragment, NavigationController.Transition.NO_EFFECT, NavigationController.Backstack.DO_NOT_ADD);


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
}
