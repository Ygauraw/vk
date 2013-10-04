package com.gark.vknew.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;


import com.gark.vknew.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;


/**
 * Created by Artem on 02.09.13.
 */
public class DownloadService extends IntentService {
    static public final int CONNECT_TIMEOUT = 10000;
    static public final int READ_TIMEOUT = 10000;

    public static final String URL = "url";
    public static final String ID = "id";
    public static final String TITLE = "title";

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String urlToDownload = intent.getStringExtra(URL);
        long id = intent.getLongExtra(ID, 0);
        String title = intent.getStringExtra(TITLE);

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(getString(R.string.video_downloading))
                .setContentText(getString(R.string.download_in_progress) + " " + title)
                .setSmallIcon(R.drawable.yellow_headphones_1);


        try {
            URL url = new URL(urlToDownload);
            URLConnection connection = url.openConnection();

            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);

            connection.connect();

            // this will be useful so that you can show a typical 0-100% progress bar
            int fileLength = connection.getContentLength();
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            String sPath = path.getAbsolutePath();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(sPath + "/" + title);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;

                mBuilder.setProgress(100, (int) (total * 100 / fileLength), false);
                mNotifyManager.notify(0, mBuilder.build());

                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mBuilder.setContentText(getString(R.string.download_complete));
            mNotifyManager.notify((int) id, mBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
