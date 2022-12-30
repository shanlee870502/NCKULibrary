package edu.ncku.application.util;

import android.app.Activity;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.SSLContext;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class WebViewChecker extends WebViewClient{
    public Boolean isVerified = true;
    public Boolean deleteFragment = false;
    public WebView view;
    private String uri;
    private SSLContext sslContext;

    public WebViewChecker(WebView view, String uri, SSLContext sslContext){
        this.view = view;
        this.uri = uri;
        this.sslContext = sslContext;
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        deleteFragment = true;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // TODO Auto-generated method stub
        view.loadUrl(url);
        return true;
    }

    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request){
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
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        deleteFragment = true;
    }
}
