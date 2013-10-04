package com.gark.vknew.ui;

import com.gark.vknew.R;
import com.gark.vknew.utils.Constants;
import com.perm.kate.api.Auth;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class LoginActivity extends Activity {
    private static final String TAG = "Kate.LoginActivity";

    WebView webview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        webview = (WebView) findViewById(R.id.vkontakteview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.clearCache(true);

        //Чтобы получать уведомления об окончании загрузки страницы
        webview.setWebViewClient(new VkontakteWebViewClient());

        //otherwise CookieManager will fall with java.lang.IllegalStateException: CookieSyncManager::createInstance() needs to be called before CookieSyncManager::getInstance()
        CookieSyncManager.createInstance(this);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        String url = Auth.getUrl(Constants.API_ID, getSettings());
        webview.loadUrl(url);
    }

    public static String getSettings() {
        //буквы http://vk.com/developers.php?oid=-1&p=%D0%9F%D1%80%D0%B0%D0%B2%D0%B0_%D0%B4%D0%BE%D1%81%D1%82%D1%83%D0%BF%D0%B0_%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B9
        //Цифры http://vk.com/developers.php?oid=-1&p=%D0%9F%D1%80%D0%B0%D0%B2%D0%B0_%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D0%B9
        //1       notify        Пользователь разрешил отправлять ему уведомления.
        //2       friends       Доступ к друзьям.
        //4       photos        Доступ к фотографиям.
        //8       audio         Доступ к аудиозаписям.
        //16      video         Доступ к видеозаписям.
        //32      offers        Доступ к предложениям.
        //64      questions     Доступ к вопросам.
        //128     pages         Доступ к wiki-страницам.
        //256                   Добавление ссылки на приложение в меню слева.
        //512                   Добавление ссылки на приложение для быстрой публикации на стенах пользователей.
        //1024    status        Доступ к статусам пользователя.
        //2048    notes         Доступ заметкам пользователя.
        //4096    messages      Доступ к расширенным методам работы с сообщениями.
        //8192    wall          Доступ к обычным и расширенным методам работы со стеной.
        //65536   offline       offline
        //131072  docs          Доступ к документам пользователя.
        //262144  groups        Доступ к группам пользователя.
        //524288  notifications Доступ к оповещениям об ответах пользователю.
        //1048576 stats         Доступ к статистике групп и приложений пользователя, администратором которых он является.
        //        ads
        //        nohttps
        int settings = 8 + 16 + 65536;
        return Integer.toString(settings);
    }

    class VkontakteWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            parseUrl(url);
        }
    }

    private void parseUrl(String url) {
        try {
            if (url == null)
                return;
            Log.i(TAG, "url=" + url);
            if (url.startsWith(Auth.redirect_url)) {
                if (!url.contains("error=")) {
                    String[] auth = Auth.parseRedirectUrl(url);
                    Intent intent = new Intent();
                    intent.putExtra("token", auth[0]);
                    intent.putExtra("user_id", Long.parseLong(auth[1]));
                    setResult(Activity.RESULT_OK, intent);
                }
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}