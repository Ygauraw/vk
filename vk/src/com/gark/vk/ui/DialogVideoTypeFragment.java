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
public class DialogVideoTypeFragment extends DialogFragment implements DialogInterface.OnClickListener {


    public static final int v720 = 720;
    public static final int v480 = 480;
    public static final int v360 = 360;
    public static final int v240 = 240;

    private String url;
    private DownloadManager dm;
    private String currentTitle;
    private Spinner qualitySpinner;
    private ArrayAdapter<String> dataAdapter;
    private ArrayList<String> arr = new ArrayList<String>();
    private LayoutInflater mInflater;
    private VideoTypes videoTypes;

    public DialogVideoTypeFragment() {
        Log.e("");
    }

    public DialogVideoTypeFragment(VideoTypes videoTypes, String currentTitle) {
        this.currentTitle = currentTitle;
        this.videoTypes = videoTypes;
        fillArray();
    }

    private void fillArray() {
        if (videoTypes.getVkVideo720() != null) {
            arr.add(String.valueOf(v720));
        }
        if (videoTypes.getVkVideo480() != null) {
            arr.add(String.valueOf(v480));
        }
        if (videoTypes.getVkVideo360() != null) {
            arr.add(String.valueOf(v360));
        }
        if (videoTypes.getVkVideo240() != null) {
            arr.add(String.valueOf(v240));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(DirectVideoFilesReposeHandler.VIDEO_TYPES, videoTypes);
        super.onSaveInstanceState(outState);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            videoTypes = savedInstanceState.getParcelable(DirectVideoFilesReposeHandler.VIDEO_TYPES);
            fillArray();
        }

        dm = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.video_types_dialog, null);
        qualitySpinner = (Spinner) view.findViewById(R.id.quality_spinner);
        dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, arr);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        qualitySpinner.setAdapter(dataAdapter);
        qualitySpinner.setOnItemSelectedListener(onItemSelectedListener);


        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.choose_quality)
                .setPositiveButton(R.string.play, this)
                .setNeutralButton(R.string.download, this)
                .setView(view);
        return adb.create();
    }

    AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            int resolutionValue = (Integer.valueOf(adapterView.getAdapter().getItem(i).toString()));
            switch (resolutionValue) {
                case v720:
                    url = videoTypes.getVkVideo720();
                    break;
                case v480:
                    url = videoTypes.getVkVideo480();
                    break;
                case v360:
                    url = videoTypes.getVkVideo360();
                    break;
                case v240:
                    url = videoTypes.getVkVideo240();
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

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
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(url), "video/*");
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case Dialog.BUTTON_NEUTRAL:
                try {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                    request.setAllowedOverRoaming(false);
                    request.setTitle(currentTitle);
                    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, currentTitle + ".mp4");
                    }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE | DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    }
                    dm.enqueue(request);
                    String downloadToastMessage = getActivity().getString(R.string.downloading_started, "", currentTitle);
                    Toast.makeText(getActivity(), downloadToastMessage, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), R.string.download_error, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
