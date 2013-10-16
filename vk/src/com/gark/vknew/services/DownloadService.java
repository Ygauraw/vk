package com.gark.vknew.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;


import com.gark.vknew.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
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


        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wl.acquire();

        String urlToDownload = intent.getStringExtra(URL);
        long id = intent.getLongExtra(ID, 0);
        String title = intent.getStringExtra(TITLE);

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(getString(R.string.video_downloading))
                .setContentText(getString(R.string.download_in_progress) + " " + title)
                .setSmallIcon(R.drawable.yellow_headphones_1);

        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        File path = null;

        try {


            URL url = new URL(urlToDownload);
            connection = (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);

            connection.connect();


            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                return;

            // this will be useful so that you can show a typical 0-100% progress bar
            int fileLength = connection.getContentLength();
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            String sPath = path.getAbsolutePath();

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(sPath + "/" + title);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;

                if (fileLength > 0) {
                    mBuilder.setProgress(100, (int) (total * 100 / fileLength), false);
                    mNotifyManager.notify(0, mBuilder.build());
                }

                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                wl.release();

                if (connection != null)
                    connection.disconnect();

                if (output != null)
                    output.close();
                if (input != null)
                    input.close();

            } catch (Exception e) {
            }
        }

        if (path != null) {
            Intent intentScanner = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intentScanner.setData(Uri.fromFile(path));
            sendBroadcast(intentScanner);
        }

//        try {
//            mBuilder.setContentText(getString(R.string.download_complete));
//            mNotifyManager.notify((int) id, mBuilder.build());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
