package edu.ncku.application.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.annotation.Nullable;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import edu.ncku.application.R;
import edu.ncku.application.io.IOConstatnt;
import edu.ncku.application.io.network.PinnedSSLContextFactory;
import edu.ncku.application.util.EnvChecker;
import edu.ncku.application.util.ITitleChangeListener;

/**
 * 開啟IR搜尋網頁，如果有關鍵字的參數則一併輸入，同時會檢查語言環境
 */
public class IRSearchFragment extends Fragment implements IOConstatnt{

    private static final String DEBUG_FLAG = IRSearchFragment.class.getName();

    //public static final String KEYWORD = "keyword";

    //private static final String SEARCH_URL = "http://m.lib.ncku.edu.tw/catalogs/KeywordSearch%s.php";
    //private static final String BIB_URL = "http://m.lib.ncku.edu.tw/catalogs/KeywordBibSearch.php?Keyword=%s&lan=%s";
    //20201123 Add titlechangerListener
    private ITitleChangeListener titleChangeListener; //標題變更的監聽介面(實體由MainActivity所控制)
    private String url;

    private InputStream input;
    private SSLContext sslContext;
    private Boolean isVerified = true;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment IRSearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static IRSearchFragment newInstance(String keyword) {
        IRSearchFragment fragment = new IRSearchFragment();
        Bundle args = new Bundle();
        args.putString(KEYWORD, keyword);
        fragment.setArguments(args);
        return fragment;
    }

    public static IRSearchFragment newInstance() {
        return new IRSearchFragment();
    }

    public IRSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // 使fragment驅動onCreateOptionsMenu

        if (getArguments() != null) {
            url = String.format(BIB_URL_SSL, getArguments().getString(KEYWORD), (EnvChecker.isLunarSetting())?"cht":"eng");
        } else {

            url = String.format(SEARCH_URL_SSL, (EnvChecker.isLunarSetting())?"":"_eng");

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 20201223 Clean the session cookies
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(new ValueCallback<Boolean>() {
                @Override
                public void onReceiveValue(Boolean aBoolean) {
                }
            });
        }
        else cookieManager.removeAllCookie();

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_web, container,
                false);

        WebView webView = (WebView) rootView.findViewById(R.id.irWebView);
        //20201123 設置允許混合加載內容
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        webView.getSettings().setJavaScriptEnabled(true);

        try {
            input = getActivity().getAssets().open("server.cer");
            sslContext = PinnedSSLContextFactory.getSSLContext(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        webView.setWebViewClient(new WebViewClient() {
            //20201123 實作ssl error handler，若發生錯誤則返回首頁
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.cancel();
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                builder.setView(inflater.inflate(R.layout.ssl_alertbox, null));
                builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteFragment();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                view.loadUrl(url);
                return true;
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

                return checkSsl(view, url);
            }

            private WebResourceResponse checkSsl(WebView view, String uri) {

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
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                builder.setView(inflater.inflate(R.layout.ssl_alertbox, null));
                builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteFragment();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        if(isVerified) {
            webView.loadUrl(url);
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            menu.findItem(R.id.settingMenuItem).setVisible(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    //20201123 To do delete title while clicking ok button in alert box
    /**********************************/
    /**
     * 註冊ITitleChangeListener介面的標題刪除方法
     *
     * @param activity
     **/
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            titleChangeListener = (ITitleChangeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ITitleChangeListener");
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        if(!isVerified){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            builder.setView(inflater.inflate(R.layout.ssl_alertbox, null));
            builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    deleteFragment();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        titleChangeListener = null;
    }
    private void deleteFragment(){
        titleChangeListener.deleteTitle();
    }
    /************************************************/
}
