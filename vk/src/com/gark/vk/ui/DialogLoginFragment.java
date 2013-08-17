package com.gark.vk.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.gark.vk.R;
import com.gark.vk.model.VideoTypes;
import com.gark.vk.network.DirectVideoFilesReposeHandler;
import com.gark.vk.utils.Log;

import java.util.ArrayList;

/**
 * Created by Gark on 21.07.13.
 */
public class DialogLoginFragment extends DialogFragment implements DialogInterface.OnClickListener {


    public DialogLoginFragment() {
        Log.e("");
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.not_necessarily)
                .setMessage(R.string.login_to_vk)
                .setPositiveButton(R.string.enter, this)
                .setNegativeButton(R.string.cancel, this);
        return adb.create();
    }


    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case Dialog.BUTTON_POSITIVE:
                Intent intent = new Intent();
                intent.setClass(getActivity(), LoginActivity.class);
                startActivityForResult(intent, MainActivity1.REQUEST_LOGIN);
                break;

        }
    }
}
