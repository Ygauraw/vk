package com.gark.vk.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gark.vk.R;
import com.gark.vk.navigation.NavigationControllerFragment;

/**
 * Created by Artem on 08.07.13.
 */
public class LeftMenuFragment extends NavigationControllerFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.leftmenu, null);
        return view;
    }
}
