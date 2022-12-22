package edu.ncku.application.util;

import static edu.ncku.application.io.IOConstatnt.ISBN_SEARCH_URL_SSL;

import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class SSLChecker extends WebViewClient{
    private Boolean isVerified = true;
    public WebView view;


    public SSLChecker(WebView view){
        view = view;
    }

    public WebResourceResponse checkSsl(WebView view, String uri, SSLContext sslContext){
        URLConnection urlConnection = null;
        try {
            URL url = new URL(uri);
            urlConnection = url.openConnection();
            if(urlConnection instanceof HttpsURLConnection) {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) urlConnection;
                httpsURLConnection.setInstanceFollowRedirects(false);
                httpsURLConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                httpsURLConnection.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
                int respCode = httpsURLConnection.getResponseCode();
                if (respCode == 301 || respCode == 302) {
                    httpsURLConnection.disconnect();

                    return super.shouldInterceptRequest(view, uri);
                }
                if(respCode != 200){
                    httpsURLConnection.disconnect();

                    return super.shouldInterceptRequest(view, uri);
                }
            }
            InputStream is = urlConnection.getInputStream();
            String contentType = urlConnection.getContentType();
            String encoding = urlConnection.getContentEncoding();
            if (contentType != null) {
                String mimeType = contentType;

                if (contentType.contains(";")) {
                    mimeType = contentType.split(";")[0].trim();
                }

                return super.shouldInterceptRequest(view, uri);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if(urlConnection != null){
            if(urlConnection instanceof HttpsURLConnection){
                ((HttpsURLConnection)urlConnection).disconnect();
            }
        }
        isVerified = false;

        return new WebResourceResponse(null, null, null);
    }
}
