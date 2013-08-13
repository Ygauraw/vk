package com.the111min.android.api;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

/**
 * This class manages connection to server via POST, GET, PUT, DELETE methods.
 */
class HttpRequestSender {

    private static DefaultHttpClient sHttpClient;
    private static final int DEFAULT_MAX_CONNECTIONS = 30;
    private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;
    private static final int DEFAULT_MAX_RETRIES = 3;

    public static HttpResponse sendRequest(HttpRequestBase request) throws IOException, URISyntaxException {
        return getHttpClient().execute(request);
    }

    public static DefaultHttpClient getHttpClient() {
        if (sHttpClient == null) {

            final HttpParams httpParameters = new BasicHttpParams();

            int timeoutConnection = 30000;
            int timeoutSocket = 30000;


            ConnManagerParams.setTimeout(httpParameters, timeoutSocket);
            ConnManagerParams.setMaxConnectionsPerRoute(httpParameters, new ConnPerRouteBean(DEFAULT_MAX_CONNECTIONS));
            ConnManagerParams.setMaxTotalConnections(httpParameters, DEFAULT_MAX_CONNECTIONS);

            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            HttpConnectionParams.setTcpNoDelay(httpParameters, true);
            HttpConnectionParams.setSocketBufferSize(httpParameters, DEFAULT_SOCKET_BUFFER_SIZE);

            HttpProtocolParams.setVersion(httpParameters, HttpVersion.HTTP_1_1);

            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
            ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(httpParameters, schemeRegistry);

            sHttpClient = new DefaultHttpClient(cm, httpParameters);
            sHttpClient.setHttpRequestRetryHandler(new RetryHandler(DEFAULT_MAX_RETRIES));

            trustEveryone();

        }
        return sHttpClient;
    }

    private static void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }

}
